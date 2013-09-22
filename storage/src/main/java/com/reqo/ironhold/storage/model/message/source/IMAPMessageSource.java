package com.reqo.ironhold.storage.model.message.source;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.IOException;


@JsonIgnoreProperties(ignoreUnknown = true)
public class IMAPMessageSource extends MessageSource {

    private String imapSource;
    private String username;
    private int imapPort;
    private String protocol;
    private String folder;


    public IMAPMessageSource() {
        super();
    }

    public IMAPMessageSource(String messageId, String imapSource, String username, int imapPort, String protocol, String folder) {
        super(messageId);
        this.imapSource = imapSource;
        this.username = username;
        this.imapPort = imapPort;
        this.protocol = protocol;
        this.folder = folder;
    }

    public String serialize() throws IOException {
        return mapper.writeValueAsString(this);
    }

    public IMAPMessageSource deserialize(String source) throws IOException {
        return mapper.readValue(source, IMAPMessageSource.class);
    }

    public String getDescription() {
        StringBuffer sb = new StringBuffer();
        sb.append(protocol);
        sb.append("://");
        sb.append(username);
        sb.append("@");
        sb.append(imapSource);
        sb.append(":");
        sb.append(imapPort);
        sb.append("/");
        sb.append(folder);
        return sb.toString();
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

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
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
                && existingSource.getUsername().equals(source.getUsername())
                && existingSource.getFolder().equals(source.getFolder())
                ) {

            return true;
        }
        return false;
    }

}
