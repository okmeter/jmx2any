package org.vafer.jmx;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import org.vafer.jmx.output.ConsoleOutput;
import org.vafer.jmx.pipe.ConverterPipe;
import org.vafer.jmx.pipe.JmxPipe;

public final class Main {

    @Parameter(names = "-config", description = "path to config file", required = true)
    private static String configPath = "/etc/jmx2any.yml";

    @Parameter(names = "-agent", description = "run agent")
    private static boolean agent = false;

    public static void main(String[] args) throws Exception {
        Main m = new Main();
        JCommander cli = new JCommander(m);
        try {
            cli.parse(args);
        } catch(Exception e) {
            cli.usage();
            System.exit(1);
        }

        // connections mess workaround
        // shit happens if we monitoring several apps in containers with "same" rmi host:port
        // so we configure sun.rmi.transport.tcp.TCPChannel.reaper to clear connections cache immediately
        System.setProperty("sun.rmi.transport.connectionTimeout", "1");

        JmxPipe output = new ConverterPipe(new ConsoleOutput(), "");
        try {
            final Config config = Config.load(configPath);
            long repeatDelay = config.repeatDelay;
            if (!agent) {
                repeatDelay = 0;
            }
            for (Map.Entry<Server, Set<String>> sq: config.queries.entrySet()) {
                if (sq.getValue() == null || sq.getValue().isEmpty()) {
                    throw new IllegalArgumentException("Query can't be null or empty, " + sq.getKey());
                }
                Worker worker = new Worker(sq.getKey(), sq.getValue(), output, repeatDelay);
                worker.start();
            }
        } catch (Exception e) {
            System.err.println("jmx2any: " + e);
            System.exit(1);
        }

    }

}
