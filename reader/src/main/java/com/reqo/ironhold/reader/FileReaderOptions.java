package com.reqo.ironhold.reader;

import org.kohsuke.args4j.Option;

public class FileReaderOptions {

	@Option(name = "-client", usage = "client name", required = true)
	private String client;

	@Option(name = "-emlFile", usage = "eml file", required = true)
	private String emlFile;

	public String getClient() {
		return client;
	}

	public String getEmlFile() {
		return emlFile;
	}

}
