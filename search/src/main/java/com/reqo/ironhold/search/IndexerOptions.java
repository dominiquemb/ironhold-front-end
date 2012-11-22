package com.reqo.ironhold.search;

import org.kohsuke.args4j.Option;

public class IndexerOptions {
    @Option(name = "-client", usage = "client name", required = true)
    private String client;

    @Option(name = "-batchSize", usage = "size of batch to process at a time", required = true)
    private int batchSize;

    @Option(name = "-threads", usage = "number of threads to use for concurrent processing of the batch", required = true)
    private int threads;


    public String getClient() {
        return client;
    }

    public int getBatchSize() {
        return batchSize;
    }
    
    public int getThreads() {
    	return threads;
    }

}
