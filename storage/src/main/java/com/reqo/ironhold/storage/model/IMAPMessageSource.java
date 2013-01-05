package com.reqo.ironhold.storage.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class IMAPMessageSource extends MessageSource {
	private String imapSource;
	private String username;
	private int imapPort;
	private String protocol;

	public IMAPMessageSource() {
		super();
	}

	public String getImapSource() {
		return imapSource;
	}

	public void setImapSource(String imapSource) {
		this.imapSource = imapSource;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getImapPort() {
		return imapPort;
	}

	public void setImapPort(int imapPort) {
		this.imapPort = imapPort;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
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

	public static boolean sameAs(IMAPMessageSource existingSource,
			IMAPMessageSource source) {
		if (existingSource.getHostname().equals(source.getHostname())
				&& existingSource.getImapPort() == source.getImapPort()
				&& existingSource.getImapSource()
						.equals(source.getImapSource())
				&& existingSource.getLoadTimestamp().equals(
						existingSource.getLoadTimestamp())
				&& existingSource.getProtocol().equals(source.getProtocol())
				&& existingSource.getUsername().equals(source.getUsername())) {

			return true;
		}
		return false;
	}

}
