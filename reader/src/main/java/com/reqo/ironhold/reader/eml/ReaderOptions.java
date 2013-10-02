package com.reqo.ironhold.reader.eml;

import org.kohsuke.args4j.Option;

public class ReaderOptions {

    @Option(name = "-client", usage = "client name", required = true)
    private String client;

    @Option(name = "-hostname", usage = "imap server", required = true)
    private String hostname;

    @Option(name = "-port", usage = "imap port", required = true)
    private int port;

    @Option(name = "-username", usage = "username to login as", required = true)
    private String username;

    @Option(name = "-password", usage = "password", required = true)
    private String password;

    @Option(name = "-protocol", usage = "protocol e.g. (imaps/imap)", required = true)
    private String protocol;

    @Option(name = "-batchSize", usage = "number of messages to retrieve at a time", required = true)
    private int batchSize;

    @Option(name = "-expunge", usage = "whether to expunge messages from server", required = false)
    private boolean expunge;

    @Option(name = "-timeout", usage = "IMAP command timeout", required = false)
    private int timeout = 60000;

    @Option(name = "-encrypt", usage = "encrypt data", required = false)
    private boolean encrypt;

    @Option(name = "-testMode", usage = "whether to process anything", required = false)
    private boolean testMode;

    @Option(name = "-folderMatch", usage = "string that folder must match", required = false)
    private String folderMatch;

    @Option(name = "-folderNotMatch", usage = "string that folder must not match", required = false)
    private String folderNotMatch;


    public boolean isEncrypt() {
        return encrypt;
    }

    public boolean isExpunge() {
        return expunge;
    }

    public String getClient() {
        return client;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public String getFolderMatch() {
        return folderMatch;
    }

    public String getFolderNotMatch() {
        return folderNotMatch;
    }
}
