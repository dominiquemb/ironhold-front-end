package com.reqo.ironhold.storage.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class MessageSource {
	private Date loadTimestamp;
	private String hostname;

	public MessageSource() {
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			this.hostname = addr.getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			hostname = "unknown";
		}
		this.loadTimestamp = new Date();

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
