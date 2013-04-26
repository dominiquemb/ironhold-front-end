package com.reqo.ironhold.storage.model.message.source;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;
import java.util.Date;


@JsonIgnoreProperties(ignoreUnknown = true)
public class PSTMessageSource extends MessageSource {
    private String pstFileName;
    private String folder;
    @JsonDeserialize(contentAs = Long.class)
    private Long size;
    private Date fileTimestamp;

    public PSTMessageSource() {
        super();
    }

    public PSTMessageSource(String messageId, String pstFileName, String folder, long size,
                            Date fileTimestamp) {
        super(messageId);
        this.pstFileName = pstFileName;
        this.folder = folder;
        this.size = size;
        this.fileTimestamp = fileTimestamp;
    }

    public String serialize() throws IOException {
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
                false);
        mapper.enableDefaultTyping();
        return mapper.writeValueAsString(this);
    }

    public PSTMessageSource deserialize(String source) throws IOException {
        return mapper.readValue(source, PSTMessageSource.class);
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

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
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
