package org.vafer.jmx;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public final class JmxQuery implements Iterable<JmxQuery.JmxBean> {

    private final JMXConnector connector;
    private final MBeanServerConnection connection;
    private final Collection<JmxBean> mbeans;

    public interface JmxAttribute {

        public ObjectName getBeanName();
        public String getAttributeName();
        public Object getAttributeValue() throws InstanceNotFoundException, IOException, AttributeNotFoundException, ReflectionException, MBeanException;

    }

    public final class JmxBean implements Iterable<JmxQuery.JmxAttribute> {

        private final Collection<JmxAttribute> attributes;

        public JmxBean(ObjectInstance mbean) throws IntrospectionException, InstanceNotFoundException, IOException, ReflectionException {
            final ObjectName mbeanName = mbean.getObjectName();
            final MBeanInfo mbeanInfo = connection.getMBeanInfo(mbeanName);

            final Collection<JmxAttribute> attributes = new ArrayList<JmxAttribute>();
            for (final MBeanAttributeInfo attribute : mbeanInfo.getAttributes()) {
                if (attribute.isReadable()) {
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
            }
            this.attributes = attributes;
        }

        public Iterator<JmxAttribute> iterator() {
            return attributes.iterator();
        }
    }

    public JmxQuery(final String url, final String username, final String password, final Set<String> expressions) throws IOException, MalformedObjectNameException, IntrospectionException, InstanceNotFoundException, ReflectionException {
        Map<String, Object> environment = new HashMap<String, Object>();
        if (username != null && username.length() != 0 && password != null && password.length() != 0) {
            String[] credent = new String[] {username, password};
            environment.put(javax.management.remote.JMXConnector.CREDENTIALS, credent);
        }
        // if (ssl) {
        //     environment.put(Context.SECURITY_PROTOCOL, "ssl");
        //     SslRMIClientSocketFactory clientSocketFactory = new SslRMIClientSocketFactory();
        //     environment.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, clientSocketFactory);
        //     environment.put("com.sun.jndi.rmi.factory.socket", clientSocketFactory);
        // }

        System.out.println("# connecting to " + username + ":" + password + "@" + url);
        this.connector = JMXConnectorFactory.connect(new JMXServiceURL(url), environment);
        this.connection = connector.getMBeanServerConnection();

        final Collection<JmxBean> mbeans = new ArrayList<JmxBean>();
        for(String expression : expressions) {
            System.out.println("# querying expression " + expression);
            for(ObjectInstance mbean : connection.queryMBeans(new ObjectName(expression), null)) {
                System.out.println("# adding mbean " + mbean.toString());
                mbeans.add(new JmxBean(mbean));
            }
        }
        this.mbeans = mbeans;
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
