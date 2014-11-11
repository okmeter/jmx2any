package org.vafer.jmx;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class Main {

    @Parameter(names = "-config", description = "path to config file", required = true)
    private String configPath = "/etc/jmx2any.yml";

    @Parameter(names = "-agent", description = "run agent")
    private boolean agent = false;

    private void run() throws Exception {
        if (agent) {
            new Agent(configPath).start();
            // just running the agent until the jvm is terminated
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            String s;
            while ((s = br.readLine()) != null && s.length() != 0) {
                System.out.println(s);
            }
            System.exit(0);
        } else {
            Exporter exporter = new Exporter();
            Exporter.Config config = exporter.load(configPath);
            exporter.output(config);
        }
    }

    public static void main(String[] args) throws Exception {
        Main m = new Main();
        JCommander cli = new JCommander(m);
        try {
            cli.parse(args);
        } catch(Exception e) {
            cli.usage();
            System.exit(1);
        }
        m.run();
    }
}
