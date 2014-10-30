package org.vafer.jmx.output;

import org.vafer.jmx.formatter.DefaultFormatter;

import javax.management.ObjectName;
import java.io.IOException;

public final class ConsoleOutput implements Output {
    private long time;

    public void open() throws IOException {
        time = System.currentTimeMillis() / 1000;
    }

    public void output(String url, String key, Number value) throws IOException {
        System.out.println(String.format("%d %s %s = %s",
            time,
            url,
            key,
            value.toString()
        ));
    }
    public void close() throws IOException {
    }

}
