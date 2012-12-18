package com.reqo.ironhold.storage.model;

import java.util.Arrays;

import javax.mail.Header;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class IMAPMessageSource extends MessageSource {
	private String imapSource;
	private String username;
	private int imapPort;
	private String protocol;
	private Header[] journalHeaders = new Header[0];
	
	public IMAPMessageSource() {
		super();
	}
	
	
	public void addHeader(Header header) {
		Header[] copy = Arrays.copyOf(journalHeaders, journalHeaders.length + 1);
		copy[journalHeaders.length] = header;
		journalHeaders = copy;

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

	public Header[] getJournalHeaders() {
		return journalHeaders;
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
