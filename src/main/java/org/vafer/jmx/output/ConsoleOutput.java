package org.vafer.jmx.output;

import java.io.IOException;

public final class ConsoleOutput {
    private long time;

    public void open() throws IOException {
        time = System.currentTimeMillis() / 1000;
    }

    public void output(String url, String key, String value) throws IOException {
        System.out.println(String.format("%d %s %s = %s",
            time,
            url,
            key,
            value
        ));
    }
    public void close() throws IOException {
    }

}
