package com.reqo.ironhold.storage.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;

public abstract class MessageSource {
	private Date loadTimestamp;
	private String hostname;
	private static Logger logger = Logger.getLogger(MessageSource.class);
	
	public MessageSource() {
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			this.hostname = addr.getHostName();
		} catch (UnknownHostException e) {
			logger.warn("Failed to resolve host, setting to unknown", e);
			hostname = "unknown";
		}
		this.loadTimestamp = new Date();

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

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public boolean equals(Object rhs) {
		return EqualsBuilder.reflectionEquals(this, rhs);

	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}
