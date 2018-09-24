package org.vafer.jmx;

import java.util.Set;

import org.vafer.jmx.pipe.JmxPipe;

public final class Exporter {

    public void output(Server server, Set<String> queries, JmxPipe output) throws Exception {
        output.open();
        JmxQuery query = null;
        try {
            query = new JmxQuery(
                    String.format("service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi", server.getHost(), server.getPort()),
                    queries
            );
            for (JmxQuery.JmxBean bean : query) {
                for (JmxQuery.JmxAttribute attribute : bean) {
                    output.output(server.getId(), attribute);
                }
            }
        } finally {
            if (query != null) {
                query.close();
            }
            output.close();
        }
    }

}
