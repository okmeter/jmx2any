package org.vafer.jmx;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public final class Main {

    @Parameter(names = "-config", description = "path to config file", required = true)
    private String configPath = "/etc/jmx2any.yml";

    @Parameter(names = "-agent", description = "run agent")
    private boolean agent = false;

    private void run() throws Exception {
        if (agent) {
            new Agent(configPath).start();
            // just running the agent until the jvm is terminated
            while(true) {
                Thread.sleep(5*1000);
            }
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
