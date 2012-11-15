package com.reqo.ironhold.watcher;

import org.kohsuke.args4j.Option;

public class WatcherOptions {
    @Option(name = "-client", usage = "client name", required = true)
    private String client;

    @Option(name = "-in", usage = "location of inbound directory", required = true)
    private String in;

    @Option(name = "-queue", usage = "location of queue directory", required = true)
    private String queue;

    @Option(name = "-out", usage = "location of processed directory", required = true)
    private String out;

    public String getClient() {
        return client;
    }

    public String getOut() {
        return out;
    }

    public String getIn() {
        return in;
    }

    public String getQueue() {
        return queue;
    }
}
