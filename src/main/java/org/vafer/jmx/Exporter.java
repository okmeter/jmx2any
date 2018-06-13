package org.vafer.jmx;

import org.vafer.jmx.output.ConsoleOutput;
import org.vafer.jmx.pipe.ConverterPipe;
import org.vafer.jmx.pipe.JmxPipe;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.util.*;

public final class Exporter {

    public static class Config {

        public final Map<String, Set<String>> queries;
        public final JmxPipe output;
        public final long initialDelay;
        public final long repeatDelay;

        public Config(Map<String, Set<String>> queries, JmxPipe output, int initialDelay, int repeatDelay) {
            this.queries = queries;
            this.output = output;
            this.initialDelay = initialDelay;
            this.repeatDelay = repeatDelay;
        }
    }

    private int parseTimespan(String s) {
        int unit = 0;
        if (s.endsWith("ms")) {
            unit = 1;
        } else if (s.endsWith("s")) {
            unit = 1000;
        } else if (s.endsWith("m")) {
            unit = 60*1000;
        }
        int n = Integer.parseInt(s.replaceAll("[^0-9]", ""));
        return n * unit;
    }
    
    public Config load(String configfile) throws Exception {
        Map<String, Set<String>> queriesByServer = new TreeMap<String, Set<String>>();
        Map<String,?> configMap = (Map) new Yaml().load(new FileInputStream(configfile));

        int interval = parseTimespan(String.valueOf(configMap.get("interval")));

        if (interval <= 0) {
            throw new Exception("Please specify a interval in s or ms");
        }
        
        ArrayList<Map<String, ?>> servers = (ArrayList) configMap.get("servers");
        for(Map<String, ?> serverConfig : servers) {
            Set<String> queries = flattenAsStringSet(serverConfig, "queries");
            String server = (String) serverConfig.get("server");
            queriesByServer.put(server, queries);
        }

        return new Config(queriesByServer, new ConverterPipe(new ConsoleOutput(), ""), 0, interval);
    }

    public void output(Config config) throws Exception {
        Map<String, Set<String>> queries = config.queries;
        JmxPipe output = config.output;

        output.open();
        for(String serverConnStr : config.queries.keySet()) {
            
            String username = "", password = "", server = serverConnStr;;

            String[] parts = serverConnStr.split("@", 2);
            if (parts.length > 1) {
                server = parts[1];
                
                String[] up = parts[0].split(":", 2);
                username = up[0];
                if (up.length > 1) {
                    password = up[1];
                }
            }

            System.out.println("server " + server);

            JmxQuery query = new JmxQuery(
                String.format("service:jmx:rmi:///jndi/rmi://%s/jmxrmi", server),
                username,
                password,
                queries.get(serverConnStr)
            );
            for(JmxQuery.JmxBean bean : query) {
                for(JmxQuery.JmxAttribute attribute : bean) {
                    output.output(server, attribute);
                }
            }
            query.close();
        }
        output.close();
    }

    private static Set<String> flattenAsStringSet(Map map, String key) {
        Set<String> result = new TreeSet<String>();
        Object report = map.get(key);
        if (report instanceof Collection) {
            for(Object r : (Collection) report) {
                result.add(String.valueOf(r));
            }
        } else {
            result.add(String.valueOf(report));
        }
        return result;
    }
}
