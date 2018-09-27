package org.vafer.jmx.pipe;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.vafer.jmx.JmxQuery;

public interface JmxPipe {

    void open() throws IOException;

    void output(String url, JmxQuery.JmxAttribute metric) throws IOException, InstanceNotFoundException,
            AttributeNotFoundException, ReflectionException, MBeanException;

    void close() throws IOException;

}
