package com.reqo.ironhold.storage.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import com.pff.PSTAttachment;
import com.pff.PSTException;
import com.pff.PSTMessage;
import com.reqo.ironhold.storage.model.mixin.PSTMessageMixin;

public class MailMessage {
	private static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.getSerializationConfig().addMixInAnnotations(PSTMessage.class,
				PSTMessageMixin.class);
		mapper.enableDefaultTyping();
		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
				false);
	}

	private ArchivedPSTMessage pstMessage;
	private Message liveMessage;

	private boolean indexed = false;
	private Date storedDate;
	private String messageId;
	private MessageSource[] sources;

	public MailMessage() {

	}

	public MailMessage(PSTMessage originalPSTMessage, PSTMessageSource source)
			throws JsonParseException, JsonMappingException,
			JsonGenerationException, IOException, PSTException {

		this.pstMessage = mapper
				.readValue(mapper.writeValueAsString(originalPSTMessage),
						ArchivedPSTMessage.class);
		for (int i = 0; i < originalPSTMessage.getNumberOfAttachments(); i++) {
			PSTAttachment attachment = originalPSTMessage.getAttachment(i);
			
			String fileName = attachment.getLongFilename();
			if (fileName.isEmpty()) {
				fileName = attachment.getFilename();
			}
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int bufferSize = 8176;
			byte[] buffer = new byte[bufferSize];
			InputStream stream = attachment.getFileInputStream();
			int count = stream.read(buffer);
			
			while (count == bufferSize) {
				out.write(buffer);
				count = stream.read(buffer);
			}
			byte[] endBuffer = new byte[count];
			System.arraycopy(buffer,  0,  endBuffer,  0,  count);
			out.write(buffer);
			
			this.pstMessage.addAttachment(new Attachment(attachment.getSize(), attachment.getCreationTime(), attachment.getModificationTime(), attachment.getFilename(), Base64.encodeBase64String(out.toByteArray())));
		}
		this.setMessageId(originalPSTMessage.getInternetMessageId());
		sources = new MessageSource[] { source };
	}

	public MailMessage(Message liveMessage, MessageSource source)
			throws MessagingException {
		this.liveMessage = liveMessage;
		this.setMessageId(liveMessage.getHeader("Message-Id")[0]);
		sources = new MessageSource[] { source };
	}

	public void addSource(MessageSource source) {
		MessageSource[] copy = Arrays.copyOf(sources, sources.length + 1);
		copy[sources.length] = source;
		sources = copy;

	}

	public static String toJSON(MailMessage message)
			throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(message);
	}

	public static MailMessage fromJSON(String json) throws JsonParseException,
			JsonMappingException, IOException {
		return mapper.readValue(json, MailMessage.class);
	}

	public ArchivedPSTMessage getPstMessage() {
		return pstMessage;
	}

	public void setPstMessage(ArchivedPSTMessage pstMessage) {
		this.pstMessage = pstMessage;
	}

	public Message getLiveMessage() {
		return liveMessage;
	}

	public void setLiveMessage(Message liveMessage) {
		this.liveMessage = liveMessage;
	}

	public boolean isIndexed() {
		return indexed;
	}

	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
	}

	public MessageSource[] getSources() {
		return sources;
	}

	public Date getStoredDate() {
		return storedDate;
	}

	public void setStoredDate(Date storedDate) {
		this.storedDate = storedDate;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
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
