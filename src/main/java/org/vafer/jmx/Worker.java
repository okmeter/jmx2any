package org.vafer.jmx;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.vafer.jmx.pipe.JmxPipe;
import org.vafer.jmx.util.NativeUtil;

public class Worker extends Thread {
    private final Server server;
    private final ScheduledThreadPoolExecutor executor;
    private final Set<String> queries;
    private final JmxPipe output;
    private final long repeatDelay;

    public Worker(final Server server, Set<String> queries, JmxPipe output, long repeatDelay) {
        this.server = server;
        this.executor = new ScheduledThreadPoolExecutor(1);
        this.executor.setThreadFactory(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("jmx2any-" + server);
                return t;
            }
        });
        this.queries = queries;
        this.output = output;
        this.repeatDelay = repeatDelay;
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    @Override
    public void run() {
        if (server.getNsPath() != null) {
            try {
                NativeUtil.setns(server.getNsPath());
            } catch (IOException e) {
                throw new RuntimeException("Couldn't start worker for server " + server, e);
            }
        }

        final Exporter exporter = new Exporter();
        if (repeatDelay <= 0) {
            runOnce(exporter);
        } else {
            executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    runOnce(exporter);
                }
            }, 0, repeatDelay, TimeUnit.MILLISECONDS);
        }
    }

    private void runOnce(Exporter exporter) {
        try {
            exporter.output(server, queries, output);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        System.out.println("-");
    }

}
