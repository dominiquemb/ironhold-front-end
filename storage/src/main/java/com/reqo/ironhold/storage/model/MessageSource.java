package com.reqo.ironhold.storage.model;

import java.util.Date;

public abstract class MessageSource {
	private Date loadTimestamp;
	private String hostname;

	public MessageSource() {

	}

	public MessageSource(String hostname, Date loadTimestamp) {
		this.hostname = hostname;
		this.loadTimestamp = loadTimestamp;
	}

	public Date getLoadTimestamp() {
		return loadTimestamp;
	}

	public void setLoadTimestamp(Date loadTimestamp) {
		this.loadTimestamp = loadTimestamp;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
}
