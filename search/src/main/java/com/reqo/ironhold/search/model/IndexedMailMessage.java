package com.reqo.ironhold.search.model;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jsoup.Jsoup;

import com.pff.PSTMessage;
import com.reqo.ironhold.storage.model.ArchivedPSTMessage;
import com.reqo.ironhold.storage.model.Attachment;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.MimeMailMessage;
import com.reqo.ironhold.storage.model.Recipient;
import com.reqo.ironhold.storage.model.mixin.PSTMessageMixin;

public class IndexedMailMessage {
	private static Logger logger = Logger.getLogger(IndexedMailMessage.class);

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

	@JsonIgnore
	private IndexedObjectType type;

	public IndexedMailMessage() {

	}

	public IndexedMailMessage(MailMessage mailMessage) {
		messageId = mailMessage.getMessageId();
		load(mailMessage.getPstMessage());

		attachments = mailMessage.getAttachments();
		this.setType(IndexedObjectType.PST_MESSAGE);
	}

	public IndexedMailMessage(MimeMailMessage mimeMessage) {
		messageId = mimeMessage.getMessageId();
		load(mimeMessage);

		attachments = mimeMessage.getAttachments();
		this.setType(IndexedObjectType.MIME_MESSAGE);
	}

	private void load(MimeMailMessage imapMailMessage) {
		logger.info("Loading imap message");
		subject = imapMailMessage.getSubject();
		messageDate = imapMailMessage.getMessageDate();
		sender = imapMailMessage.getFrom();

		to = imapMailMessage.getTo();
		cc = imapMailMessage.getCc();
		bcc = imapMailMessage.getBcc();

		size = imapMailMessage.getSize();

		if (imapMailMessage.getBodyHTML().trim().length() != 0) {
			body = Jsoup.parse(imapMailMessage.getBodyHTML()).text();
		} else if (imapMailMessage.getBody().trim().length() != 0) {
			body = imapMailMessage.getBody();
		} else {
			body = StringUtils.EMPTY;
		}
		logger.info("Done loading imap message");

	}

	private void load(ArchivedPSTMessage pstMessage) {
		logger.info("Loading pst message");
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
		logger.info("Done loading pst message");

	}

	public static String toJSON(IndexedMailMessage message)
			throws JsonGenerationException, JsonMappingException, IOException {
		String result = null;
		logger.info("Starting toJSON serialization");
		try {
			result = mapper.writeValueAsString(message);
		} finally {
			logger.info("Finished toJSON serialization " + result.length()
					+ " bytes");
		}

		return result;
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
		this.attachments = attachments.clone();
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

	public IndexedObjectType getType() {
		return type;
	}

	public void setType(IndexedObjectType type) {
		this.type = type;
	}
}
