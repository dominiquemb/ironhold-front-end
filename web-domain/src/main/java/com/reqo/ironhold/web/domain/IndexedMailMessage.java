package com.reqo.ironhold.web.domain;

import com.reqo.ironhold.web.domain.interfaces.IPartitioned;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

public class IndexedMailMessage implements IPartitioned {
    private static Logger logger = Logger.getLogger(IndexedMailMessage.class);

    private static final ObjectMapper mapper = new ObjectMapper();
    /**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1000;

    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a gigabyte.
     */
    public static final long ONE_GB = ONE_KB * ONE_MB;

    private String messageId;
    private String subject;
    private Date messageDate;
    private String year;
    private String monthDay;
    private Recipient sender;
    private Recipient[] to;
    private Recipient[] cc;
    private Recipient[] bcc;
    private long size;
    private String sizeDescription;
    private String body;
    private String importance;
    private IndexedAttachment[] attachments;
    private String[] sources;
    private String messageType;


    public IndexedMailMessage() {
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
                false);
    }


    public void addSource(String sourceId) {
        if (sources == null) {
            sources = new String[]{sourceId};
        } else {
            String[] copy = Arrays.copyOf(sources, sources.length + 1);
            copy[sources.length] = sourceId;
            sources = copy;
        }

    }

    public String[] getSources() {
        return sources;
    }

    public void setSources(String[] sources) {
        this.sources = sources;
    }

    public String serialize() {
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static IndexedMailMessage deserialize(String json) {
        try {
            return mapper.readValue(json, IndexedMailMessage.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(Date messageDate) {
        this.messageDate = messageDate;
    }

    public Recipient getSender() {
        return sender;
    }

    public void setSender(Recipient sender) {
        this.sender = sender;
    }

    public Recipient[] getTo() {
        return to;
    }

    public void setTo(Recipient[] to) {
        this.to = to.clone();
    }

    public Recipient[] getCc() {
        return cc;
    }

    public void setCc(Recipient[] cc) {
        this.cc = cc.clone();
    }

    public Recipient[] getBcc() {
        return bcc;
    }

    public void setBcc(Recipient[] bcc) {
        this.bcc = bcc.clone();
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSizeDescription() {
        String displaySize;

        if (size / ONE_GB > 0) {
            displaySize = String.valueOf(size / ONE_GB) + " GB";
        } else if (size / ONE_MB > 0) {
            displaySize = String.valueOf(size / ONE_MB) + " MB";
        } else if (size / ONE_KB > 0) {
            displaySize = String.valueOf(size / ONE_KB) + " KB";
        } else {
            displaySize = String.valueOf(size) + " bytes";
        }
        return displaySize;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonthDay() {
        return monthDay;
    }

    public void setMonthDay(String monthDay) {
        this.monthDay = monthDay;
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

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;

    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public IndexedAttachment[] getAttachments() {
        return attachments;
    }

    public void setAttachments(IndexedAttachment[] attachments) {
        this.attachments = attachments;
    }

    @Override
    @JsonIgnore
    public String getPartition() {
        return year;
    }
}
