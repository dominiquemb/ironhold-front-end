package com.reqo.ironhold.storage.model.message.source;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.IOException;


@JsonIgnoreProperties(ignoreUnknown = true)
public class BloombergSource extends MessageSource {

    private String ftpHostname;
    private String username;
    private int port;
    private String date;


    public BloombergSource() {
        super();
    }

    public BloombergSource(String messageId, String ftpHostname, String username, int port, String date) {
        super(messageId);
        this.username = username;
        this.ftpHostname = ftpHostname;
        this.port = port;
        this.date = date;
    }

    public String serialize() throws IOException {
        return mapper.writeValueAsString(this);
    }

    public BloombergSource deserialize(String source) throws IOException {
        return mapper.readValue(source, BloombergSource.class);
    }

    public String getDescription() {
        StringBuffer sb = new StringBuffer();
        sb.append(username);
        sb.append("@");
        sb.append(ftpHostname);
        sb.append(":");
        sb.append(port);
        sb.append("/");
        sb.append(date);
        return sb.toString();
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFtpHostname() {
        return ftpHostname;
    }

    public void setFtpHostname(String ftpHostname) {
        this.ftpHostname = ftpHostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public static boolean sameAs(BloombergSource existingSource,
                                 BloombergSource source) {
        if (existingSource.getHostname().equals(source.getHostname())
                && existingSource.getPort() == source.getPort()
                && existingSource.getHostname()
                .equals(source.getHostname())
                && existingSource.getFtpHostname()
                .equals(source.getFtpHostname())
                && existingSource.getLoadTimestamp().equals(
                existingSource.getLoadTimestamp())
                && existingSource.getDate().equals(source.getDate())
                && existingSource.getUsername().equals(source.getUsername())
                ) {

            return true;
        }
        return false;
    }

}
