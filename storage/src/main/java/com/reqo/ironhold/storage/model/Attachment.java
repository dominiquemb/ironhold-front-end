package com.reqo.ironhold.storage.model;

import java.util.Date;

public class Attachment {
	private int size;
	private Date creationTime;
	private Date modificationTime;
	private String fileName;
	private String body;
	
	public Attachment() {
		
	}

	public Attachment(int size, Date creationTime, Date modificationTime,
			String fileName, String body) {
		super();
		this.size = size;
		this.creationTime = creationTime;
		this.modificationTime = modificationTime;
		this.fileName = fileName;
		this.body = body;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getModificationTime() {
		return modificationTime;
	}

	public void setModificationTime(Date modificationTime) {
		this.modificationTime = modificationTime;
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
	
	

}
