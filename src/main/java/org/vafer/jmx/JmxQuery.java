package org.vafer.jmx;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.*;

public final class JmxQuery implements Iterable<JmxQuery.JmxBean> {

    private final JMXConnector connector;
    private final MBeanServerConnection connection;
    private final Collection<JmxBean> mbeans;

    public interface JmxAttribute {

        ObjectName getBeanName();

        String getAttributeName();

        Object getAttributeValue() throws InstanceNotFoundException, IOException, AttributeNotFoundException,
                ReflectionException, MBeanException;

    }

    public final class JmxBean implements Iterable<JmxQuery.JmxAttribute> {

        private final Collection<JmxAttribute> attributes;

        private JmxBean(ObjectInstance mbean, Set<String> attrNames) throws IntrospectionException, InstanceNotFoundException, IOException, ReflectionException {
            final ObjectName mbeanName = mbean.getObjectName();
            final MBeanInfo mbeanInfo = connection.getMBeanInfo(mbeanName);

            final Collection<JmxAttribute> attributes = new ArrayList<JmxAttribute>();
            for (final MBeanAttributeInfo attribute : mbeanInfo.getAttributes()) {
                if (!attribute.isReadable()) {
                    continue;
                }
                if (attrNames.size() != 0 && !attrNames.contains(attribute.getName())) {
                    continue;
                }
                attributes.add(new JmxAttribute() {
                    private Object value;

                    public ObjectName getBeanName() {
                        return mbeanName;
                    }

                    public String getAttributeName() {
                        return attribute.getName();
                    }

                    public Object getAttributeValue() throws InstanceNotFoundException, IOException, AttributeNotFoundException, ReflectionException, MBeanException {
                        if (value == null) {
                            // System.out.println("> reading " + this.getAttributeName());
                            value = connection.getAttribute(mbeanName, attribute.getName());
                        }
                        return value;
                    }
                });
            }
            this.attributes = attributes;
        }

        public Iterator<JmxAttribute> iterator() {
            return attributes.iterator();
        }
    }

    public JmxQuery(final String url, final Set<String> expressions) throws IOException, MalformedObjectNameException, IntrospectionException, InstanceNotFoundException, ReflectionException {
        synchronized (JmxQuery.class) {
            this.connector = JMXConnectorFactory.connect(new JMXServiceURL(url));
            this.connection = connector.getMBeanServerConnection();

            final Collection<JmxBean> mbeans = new ArrayList<JmxBean>();
            final Set<String> attrNames = new HashSet<String>();
            for (String expression : expressions) {
                String[] parts = expression.split(";", 2);
                if (parts.length > 1 && parts[1].length() > 0) {
                    for (String attr : parts[1].split(",")) {
                        attrNames.add(attr);
                    }
                }
                for (ObjectInstance mbean : connection.queryMBeans(new ObjectName(parts[0]), null)) {
                    mbeans.add(new JmxBean(mbean, attrNames));
                }
            }
            this.mbeans = mbeans;
        }
    }

    public Iterator<JmxBean> iterator() {
        return mbeans.iterator();
    }

    public void close() throws IOException {
        if (connector != null) {
            connector.close();
        }
    }
}
