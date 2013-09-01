package com.reqo.ironhold.reader.eml;

import org.kohsuke.args4j.Option;

public class FileReaderOptions {

    @Option(name = "-client", usage = "client name", required = true)
    private String client;

    @Option(name = "-dir", usage = "path to directory with eml files", required = true)
    private String dir;

    @Option(name = "-encrypt", usage = "encrypt date", required = false)
    private boolean encrypt;

    public String getClient() {
        return client;
    }

    public String getDir() {
        return dir;
    }

    public boolean isEncrypt() {
        return encrypt;
    }
}
