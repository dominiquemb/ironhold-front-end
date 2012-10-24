package com.reqo.ironhold.storage.model;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

@SuppressWarnings("serial")
public class LogMessage implements Serializable {
	private String messageId;
	private String host;
	private Date timestamp;
	private String message;
	private LogLevel level;

	public LogMessage() throws UnknownHostException {
		super();
		
		this.host = InetAddress.getLocalHost().getHostName();
		this.timestamp = new Date();
	}

	public LogMessage(LogLevel level, String messageId, String message) throws UnknownHostException {
		super();
		this.messageId = messageId;
		this.message = message;
		this.level = level;
		
		this.host = InetAddress.getLocalHost().getHostName();
		this.timestamp = new Date();

	}

	private static ObjectMapper mapper = new ObjectMapper();

	public static String toJSON(LogMessage message)
			throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(message);
	}

	public static LogMessage fromJSON(String json) throws JsonParseException,
			JsonMappingException, IOException {
		return mapper.readValue(json, LogMessage.class);
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LogLevel getLevel() {
		return level;
	}

	public void setLevel(LogLevel level) {
		this.level = level;
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
