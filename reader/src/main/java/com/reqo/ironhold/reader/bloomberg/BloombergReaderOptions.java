package com.reqo.ironhold.reader.bloomberg;

import org.kohsuke.args4j.Option;

import org.kohsuke.args4j.Option;

public class BloombergReaderOptions {

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

    @Option(name = "-encrypt", usage = "encrypt data", required = false)
    private boolean encrypt;

    @Option(name = "-subdir", usage = "subdirectory", required = false)
    private String subdir;

    @Option(name = "-manifest", usage = "manifest file", required = true)
    private String manifest;

    public String getManifest() {
        return manifest;
    }

    public String getSubdir() {
        return subdir;
    }


    public boolean isEncrypt() {
        return encrypt;
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

}
