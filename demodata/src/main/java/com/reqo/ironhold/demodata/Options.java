package com.reqo.ironhold.demodata;

import org.kohsuke.args4j.Option;

public class Options {
    @Option(name = "-client", usage = "client name", required = true)
    private String client;

    @Option(name = "-number", usage = "number of messages to generate", required = true)
    private int number;

    @Option(name = "-sleep", usage = "number of milliseconds to sleep between batches", required = true)
    private int sleep;

    @Option(name = "-interval", usage = "size of batch", required = true)
    private int interval;



    public String getClient() {
        return client;
    }


	public int getNumber() {
		return number;
	}

    public int getSleep() {
        return sleep;
    }

    public int getInterval() {
        return interval;
    }
}
