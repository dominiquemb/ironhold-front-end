package com.reqo.ironhold.reader.eml;

import org.kohsuke.args4j.Option;

public class FileReaderOptions {

    @Option(name = "-client", usage = "client name", required = true)
    private String client;

    @Option(name = "-emlFile", usage = "eml file", required = true)
    private String emlFile;

    @Option(name = "-encrypt", usage = "encrypt date", required = false)
    private boolean encrypt;

    public String getClient() {
        return client;
    }

    public String getEmlFile() {
        return emlFile;
    }

    public boolean isEncrypt() {
        return encrypt;
    }
}
