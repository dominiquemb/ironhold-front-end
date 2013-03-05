package com.reqo.ironhold.exporter;

import org.kohsuke.args4j.Option;

public class Options {
    @Option(name = "-client", usage = "client name", required = true)
    private String client;

    @Option(name = "-data", usage = "location of output directory", required = true)
    private String data;

    @Option(name = "-batchSize", usage = "batch size", required = true)
    private int batchSize;

    @Option(name = "-max", usage = "max messages to backup", required = false)
    private int max;

    @Option(name = "-compression", usage = "compression algorithm", required = true)
    private String compression;

    @Option(name = "-recoveryFile", usage = "location of recovery file", required = true)
    private String recoveryFile;

    public String getClient() {
        return client;
    }

    public String getData() {
        return data;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public String getCompression() {
        return compression;
    }

    public int getMax() {
        return max;
    }

    public String getRecoveryFile() {
        return recoveryFile;
    }

}
