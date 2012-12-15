package com.reqo.ironhold.storage.model;

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class PSTMessageSource extends MessageSource {
	private String pstFileName;
	private String folder;
	private long size;
	private Date fileTimestamp;

	public PSTMessageSource() {

	}

	public PSTMessageSource(String pstFileName, String folder, long size,
			Date fileTimestamp, Date loadTimestamp, String hostname) {
		super(hostname, loadTimestamp);
		this.pstFileName = pstFileName;
		this.folder = folder;
		this.size = size;
		this.fileTimestamp = fileTimestamp;
	}

	public String getPstFileName() {
		return pstFileName;
	}

	public void setPstFileName(String pstFileName) {
		this.pstFileName = pstFileName;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Date getFileTimestamp() {
		return fileTimestamp;
	}

	public void setFileTimestamp(Date fileTimestamp) {
		this.fileTimestamp = fileTimestamp;
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

	public static boolean sameAs(PSTMessageSource existingSource,
			PSTMessageSource source) {
		if (existingSource.getPstFileName().equals(source.getPstFileName())
				&& existingSource.getFolder().equals(source.getFolder())) {
			return true;
		}
		return false;
	}

}
