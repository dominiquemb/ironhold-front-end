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
	private IMAPHeader[] journalHeaders = new IMAPHeader[0];
	
	public IMAPMessageSource() {
		super();
	}
	
	
	public void addHeader(IMAPHeader header) {
		IMAPHeader[] copy = Arrays.copyOf(journalHeaders, journalHeaders.length + 1);
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

	public IMAPHeader[] getJournalHeaders() {
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


	public static boolean sameAs(IMAPMessageSource existingSource,
			IMAPMessageSource source) {
		if (existingSource.getHostname().equals(source.getHostname())
				&& existingSource.getImapPort() == source.getImapPort()
				&& existingSource.getImapSource().equals(source.getImapSource())
				&& existingSource.getLoadTimestamp().equals(existingSource.getLoadTimestamp()) 
				&& existingSource.getProtocol().equals(source.getProtocol())
				&& existingSource.getUsername().equals(source.getUsername())
				&& existingSource.getJournalHeaders().length == source.getJournalHeaders().length) {
			
			for (int i = 0; i<existingSource.getJournalHeaders().length; i++) {
				if (!existingSource.getJournalHeaders()[i].getName().equals(source.getJournalHeaders()[i].getName()) ||
					!existingSource.getJournalHeaders()[i].getValue().equals(source.getJournalHeaders()[i].getValue())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
}
