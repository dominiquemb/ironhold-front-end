package com.reqo.ironhold.importer.watcher;

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

    @Option(name = "-q", usage = "location of quarantine directory", required = true)
    private String quarantine;

    @Option(name = "-encrypt", usage = "encrypt data", required = false)
    private boolean encrypt;

    public boolean isEncrypt() {
        return encrypt;
    }

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

    public String getQuarantine() {
        return quarantine;
    }
}
