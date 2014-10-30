package org.vafer.jmx.pipe;

import org.vafer.jmx.JmxQuery;
import org.vafer.jmx.formatter.DefaultFormatter;
import org.vafer.jmx.output.Enums;
import org.vafer.jmx.output.Output;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

public final class ConverterPipe implements JmxPipe {

    private final static DefaultFormatter formatter = new DefaultFormatter();

    private final Output output;
    private final String prefix;

    public ConverterPipe(Output output, String prefix) {
        this.output = output;
        this.prefix = prefix;
    }

    public void open() throws IOException {
        output.open();
    }

    public void output(String url, JmxQuery.JmxAttribute metric) throws IOException, InstanceNotFoundException, AttributeNotFoundException, ReflectionException, MBeanException {
        final String attribute = formatter.attributeName(metric.getBeanName(), metric.getAttributeName());
        try {
            final Object value = metric.getAttributeValue();
            flatten(url, prefix + attribute, value);
        } catch(Exception e) {
            System.err.println(String.format("Failed to read attribute %s [%s]", attribute, e.getMessage()));
        }
    }

    private void flatten(String url, String attribute, Object value) throws IOException {
        if (value instanceof Number) {

            output.output(url, attribute, (Number) value);

        } else if (value instanceof Set) {

            final Set set = (Set) value;
            for(Object entry : set) {
                flatten(url, attribute + "___" + entry, 1);
            }

        } else if (value instanceof List) {

            final List list = (List)value;
            for(int i = 0; i<list.size(); i++) {
                flatten(url, attribute + "___" + i, list.get(i));
            }

        } else if (value instanceof Map) {

            final Map<?,?> map = (Map<?,?>) value;
            for(Map.Entry<?, ?> entry : map.entrySet()) {
                flatten(url, attribute + "___" + entry.getKey(), entry.getValue());
            }

        } else if (value instanceof CompositeData) {

            CompositeData composite = (CompositeData) value;
            CompositeType type = composite.getCompositeType();
            TreeSet<String> keysSet = new TreeSet<String>(type.keySet());
            for(String key : keysSet) {
                flatten(url, attribute + "___" + key, composite.get(key));
            }

        }
    }

    public void close() throws IOException {
        output.close();
    }
}
