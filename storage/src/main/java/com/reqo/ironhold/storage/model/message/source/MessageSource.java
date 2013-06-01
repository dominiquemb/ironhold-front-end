package com.reqo.ironhold.storage.model.message.source;

import com.reqo.ironhold.storage.model.IHasMessageId;
import com.reqo.ironhold.storage.model.IPartitioned;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;

public abstract class MessageSource implements IHasMessageId, IPartitioned {
    @JsonIgnore
    protected ObjectMapper mapper = new ObjectMapper();

    private String id;
    private String messageId;
    private String partition;
    private Date loadTimestamp;
    private String hostname;
    private static Logger logger = Logger.getLogger(MessageSource.class);

    public MessageSource() {
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
                false);
        mapper.enableDefaultTyping();


        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
            this.hostname = addr.getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Failed to resolve host, setting to unknown", e);
            hostname = "unknown";
        }
        this.loadTimestamp = new Date();

        this.id = UUID.randomUUID().toString();
    }

    public MessageSource(String messageId) {
        this();
        this.messageId = messageId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getLoadTimestamp() {
        return loadTimestamp;
    }

    public void setLoadTimestamp(Date loadTimestamp) {
        this.loadTimestamp = loadTimestamp;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    @JsonIgnore
    public String getPartition() {
        return partition;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }
}
