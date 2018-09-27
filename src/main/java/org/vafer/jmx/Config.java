package org.vafer.jmx;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.vafer.jmx.util.NativeUtil;
import org.yaml.snakeyaml.Yaml;

public class Config {

    public final Map<Server, Set<String>> queries;
    public final long initialDelay;
    public final long repeatDelay;

    public Config(Map<Server, Set<String>> queries, int initialDelay, int repeatDelay) {
        this.queries = queries;
        this.initialDelay = initialDelay;
        this.repeatDelay = repeatDelay;
    }

    public static Config load(String configfile) throws Exception {
        Map<Server, Set<String>> queriesByServer = new LinkedHashMap<Server, Set<String>>();
        Map<String,?> configMap = (Map) new Yaml().load(new FileInputStream(configfile));

        int interval = parseTimespan(String.valueOf(configMap.get("interval")));

        if (interval <= 0) {
            throw new Exception("Please specify a interval in s or ms");
        }

        ArrayList<Map<String, ?>> servers = (ArrayList) configMap.get("servers");
        for(Map<String, ?> serverConfig : servers) {
            String hostPort = (String) serverConfig.get("server");
            String nsPath = (String) serverConfig.get("ns");
            if (nsPath != null && nsPath.trim().isEmpty()) {
                nsPath = null;
            }
            Server server = new Server(hostPort, nsPath);
            Set<String> queries = flattenAsStringSet(serverConfig, "queries");
            queriesByServer.put(server, queries);
        }

        return new Config(queriesByServer, 0, interval);
    }

    private static int parseTimespan(String s) {
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
