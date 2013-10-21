package com.reqo.ironhold.demodata;

import org.kohsuke.args4j.Option;

public class Options {
    @Option(name = "-client", usage = "client name", required = true)
    private String client;

    @Option(name = "-number", usage = "number of messages to generate", required = true)
    private int number;




    public String getClient() {
        return client;
    }


	public int getNumber() {
		return number;
	}


}
