package com.reqo.ironhold.web.domain;

import com.gs.collections.api.block.function.Function;
import com.reqo.ironhold.web.domain.interfaces.IHasMessageId;
import com.reqo.ironhold.web.domain.interfaces.IPartitioned;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditLogMessage implements IHasMessageId, IPartitioned {
    private ObjectMapper mapper = new ObjectMapper();

    public static Function<AuditLogMessage, Comparable> SORT_BY_CONTEXT = new Function<AuditLogMessage, Comparable>() {
        @Override
        public Comparable valueOf(AuditLogMessage auditLogMessage) {
            return auditLogMessage.getContext();
        }
    };

    protected SimpleDateFormat yearFormat = new SimpleDateFormat("YYYY");

    private String messageId;
    private AuditActionEnum action;
    private String context;
    private String host;
    private Date timestamp;
    private LoginUser loginUser;

    public AuditLogMessage() {
        super();
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
                false);
        try {
            this.host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            this.host = "unknown";
        }
        this.timestamp = new Date();
    }

    public AuditLogMessage(LoginUser loginUser, AuditActionEnum action, String messageId) {
        this(loginUser, action, messageId, StringUtils.EMPTY);
    }

    public AuditLogMessage(LoginUser loginUser, AuditActionEnum action, String messageId, String context) {
        super();
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
                false);
        this.messageId = messageId;
        this.action = action;
        this.loginUser = loginUser;
        this.context = context;

        try {
            this.host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            this.host = "unknown";
        }
        this.timestamp = new Date();

    }

    public String serialize() {
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AuditLogMessage deserialize(String source) {
        try {
            return mapper.readValue(source, AuditLogMessage.class);
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public AuditActionEnum getAction() {
        return action;
    }

    public void setAction(AuditActionEnum action) {
        this.action = action;
    }

    public LoginUser getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(LoginUser loginUser) {
        this.loginUser = loginUser;
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
    public String getPartition() {
        return yearFormat.format(this.getTimestamp());
    }
}
