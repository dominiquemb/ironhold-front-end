package com.reqo.ironhold.importer;

import java.io.File;

import org.kohsuke.args4j.Option;

class PSTImporterOptions {
	@Option(name = "-file", usage = "location of pst file to import", required = true)
	private File file;

	@Option(name = "-client", usage = "name of the client", required = true)
	private String client;

	public File getFile() {
		return file;
	}

	public String getClient() {
		return client;
	}

}
