package com.reqo.ironhold.search.model;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.jsoup.Jsoup;

import com.pff.PSTMessage;
import com.reqo.ironhold.storage.model.ArchivedPSTMessage;
import com.reqo.ironhold.storage.model.Attachment;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.Recipient;
import com.reqo.ironhold.storage.model.mixin.PSTMessageMixin;

public class IndexedMailMessage {
	private static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.getSerializationConfig().addMixInAnnotations(PSTMessage.class,
				PSTMessageMixin.class);
		mapper.enableDefaultTyping();
		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
				false);
	}

	private String messageId;
	private String subject;
	private Date messageDate;
	private Recipient sender;
	private Recipient[] to;
	private Recipient[] cc;
	private Recipient[] bcc;
	private long size;
	private String body;
	private Attachment[] attachments;

	public IndexedMailMessage() {

	}

	public IndexedMailMessage(MailMessage mailMessage) {
		if (mailMessage.getPstMessage() != null) {
			load(mailMessage.getPstMessage());
		}

		attachments = mailMessage.getAttachments();
	}

	private void load(ArchivedPSTMessage pstMessage) {
		messageId = pstMessage.getInternetMessageId();
		subject = pstMessage.getSubject();
		messageDate = pstMessage.getMessageDeliveryTime();
		sender = new Recipient(pstMessage.getSenderName(),
				pstMessage.getSenderEmailAddress());

		to = pstMessage.getTo();
		cc = pstMessage.getCc();
		bcc = pstMessage.getBcc();

		size = pstMessage.getMessageSize();

		if (pstMessage.getBody().trim().length() != 0) {
			body = pstMessage.getBody();
		} else if (pstMessage.getRtfbody().trim().length() != 0) {
			body = pstMessage.getRtfbody();
		} else if (pstMessage.getBodyHTML().trim().length() != 0) {
			body = Jsoup.parse(pstMessage.getBodyHTML()).text();
		} else {
			body = StringUtils.EMPTY;
		}

	}

	public static String toJSON(IndexedMailMessage message)
			throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(message);
	}

	public static IndexedMailMessage fromJSON(String json)
			throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, IndexedMailMessage.class);
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
		this.to = to;
	}

	public Recipient[] getCc() {
		return cc;
	}

	public void setCc(Recipient[] cc) {
		this.cc = cc;
	}

	public Recipient[] getBcc() {
		return bcc;
	}

	public void setBcc(Recipient[] bcc) {
		this.bcc = bcc;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Attachment[] getAttachments() {
		return attachments;
	}

	public void setAttachments(Attachment[] attachments) {
		this.attachments = attachments;
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
