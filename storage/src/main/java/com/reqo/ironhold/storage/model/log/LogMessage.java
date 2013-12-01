package com.reqo.ironhold.storage.model.log;

import com.reqo.ironhold.web.domain.interfaces.IHasMessageId;
import com.reqo.ironhold.web.domain.interfaces.IPartitioned;
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
public class LogMessage implements IHasMessageId, IPartitioned {
    private ObjectMapper mapper = new ObjectMapper();
    protected SimpleDateFormat yearFormat = new SimpleDateFormat("YYYY");

    private String messageId;
	private String host;
	private Date timestamp;
	private String message;
	private LogLevel level;

	public LogMessage() throws UnknownHostException {
		super();
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
                false);
		this.host = InetAddress.getLocalHost().getHostName();
		this.timestamp = new Date();
	}

	public LogMessage(LogLevel level, String messageId, String message) throws UnknownHostException {
		super();
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
                false);
		this.messageId = messageId;
		this.message = message;
		this.level = level;
		
		this.host = InetAddress.getLocalHost().getHostName();
		this.timestamp = new Date();

	}

    public String serialize() throws IOException {
        return mapper.writeValueAsString(this);
    }

    public LogMessage deserialize(String source) throws IOException {
        return mapper.readValue(source, LogMessage.class);
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LogLevel getLevel() {
		return level;
	}

	public void setLevel(LogLevel level) {
		this.level = level;
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
