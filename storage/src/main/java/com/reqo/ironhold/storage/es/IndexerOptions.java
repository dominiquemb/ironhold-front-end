package com.reqo.ironhold.storage.es;

import org.kohsuke.args4j.Option;

public class IndexerOptions {
    @Option(name = "-client", usage = "client name", required = true)
    private String client;

    @Option(name = "-batchSize", usage = "size of batch to process at a time", required = true)
    private int batchSize;


    public String getClient() {
        return client;
    }

    public int getBatchSize() {
        return batchSize;
    }

}
