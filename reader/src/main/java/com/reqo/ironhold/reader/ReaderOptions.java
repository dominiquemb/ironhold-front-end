package com.reqo.ironhold.reader;

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

	public boolean getExpunge() {
		return expunge;
	}


}
