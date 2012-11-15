package com.reqo.ironhold.search;

import org.kohsuke.args4j.Option;

public class IndexerOptions {
    @Option(name = "-client", usage = "client name", required = true)
    private String client;


    public String getClient() {
        return client;
    }

}
