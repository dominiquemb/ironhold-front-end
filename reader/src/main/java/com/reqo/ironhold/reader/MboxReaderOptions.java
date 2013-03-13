package com.reqo.ironhold.reader;

import org.kohsuke.args4j.Option;

/**
 * User: ilya
 * Date: 3/12/13
 * Time: 1:22 PM
 */
public class MboxReaderOptions {
    @Option(name = "-client", usage = "client name", required = true)
    private String client;

    @Option(name = "-filePath", usage = "path to mbox file", required = true)
    private String filePath;

    @Option(name = "-batchSize", usage = "number of messages to retrieve at a time", required = true)
    private int batchSize;

    @Option(name = "-expunge", usage = "whether to expunge messages from server", required = false)
    private boolean expunge;


    public String getClient() {
        return client;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public boolean getExpunge() {
        return expunge;
    }
}
