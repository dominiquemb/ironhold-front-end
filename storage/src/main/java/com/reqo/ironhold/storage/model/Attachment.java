package com.reqo.ironhold.storage.model;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


@SuppressWarnings("serial")
public class Attachment implements Serializable {
	private String fileName;
	private String body;
	private boolean indexed = false;
	
	public Attachment() {
		
	}

	public Attachment(String fileName, String body) {
		super();
		this.fileName = fileName;
		this.body = body;
	}

	private static ObjectMapper mapper = new ObjectMapper();

	public static String toJSON(Attachment message) throws JsonGenerationException,
			JsonMappingException, IOException {
		return mapper.writeValueAsString(message);
	}

	public static Attachment fromJSON(String json) throws JsonParseException,
			JsonMappingException, IOException {
		return mapper.readValue(json, Attachment.class);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public boolean isIndexed() {
		return indexed;
	}

	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
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
