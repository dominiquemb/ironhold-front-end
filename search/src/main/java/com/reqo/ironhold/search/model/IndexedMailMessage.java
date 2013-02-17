package com.reqo.ironhold.search.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

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
	private String year;
	private Recipient sender;
	private Recipient realSender;
	private Recipient[] to;
	private Recipient[] cc;
	private Recipient[] bcc;
	private long size;
	private String body;
	private IndexedAttachment[] attachments;
	
	@JsonIgnore
	private IndexedObjectType type;
	
	@JsonIgnore
	private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

	public IndexedMailMessage() {

	}

	public IndexedMailMessage(MailMessage mailMessage) {
		messageId = mailMessage.getMessageId();
		load(mailMessage.getPstMessage());

		attachments = IndexedAttachment.fromArray(mailMessage.getAttachments());
		this.setType(IndexedObjectType.PST_MESSAGE);
	}

	public IndexedMailMessage(MimeMailMessage mimeMessage) {
		messageId = mimeMessage.getMessageId();
		load(mimeMessage);

		attachments = IndexedAttachment.fromArray(mimeMessage.getAttachments());
		this.setType(IndexedObjectType.MIME_MESSAGE);
	}

	private void load(MimeMailMessage imapMailMessage) {
		logger.debug("Loading imap message");
		subject = imapMailMessage.getSubject();
		messageDate = imapMailMessage.getMessageDate();
		year = yearFormat.format(messageDate);
		sender = Recipient.normalize(imapMailMessage.getFrom());
		to = Recipient.normalize(imapMailMessage.getTo());
		cc = Recipient.normalize(imapMailMessage.getCc());
		bcc = Recipient.normalize(imapMailMessage.getBcc());

		size = imapMailMessage.getSize();

		if (imapMailMessage.getBodyHTML().trim().length() != 0) {
			Document html = Jsoup.parse(imapMailMessage.getBodyHTML());
			StringWriter buffer = new StringWriter();
			PrintWriter writer = new PrintWriter(buffer);
			
			for (Node node : html.childNodes()) {
				parseHtml(writer, node);
			}
			body = buffer.toString();
		} else if (imapMailMessage.getBody().trim().length() != 0) {
			body = imapMailMessage.getBody();
		} else {
			body = StringUtils.EMPTY;
		}
		logger.debug("Done loading imap message");

	}

	private void parseHtml(PrintWriter writer, Node node) {
		if (node instanceof TextNode) {
			writer.println(((TextNode) node).text());
		} else if (node instanceof Element) {
			for (Node subNode : ((Element)node).childNodes()) {
				parseHtml(writer, subNode);
			}
		}
	}

	private void load(ArchivedPSTMessage pstMessage) {
		logger.debug("Loading pst message");
		messageId = pstMessage.getInternetMessageId();
		subject = pstMessage.getSubject();
		messageDate = pstMessage.getMessageDeliveryTime();
		year = yearFormat.format(messageDate);
		sender = Recipient.normalize(new Recipient(pstMessage.getSenderName(),
				pstMessage.getSenderEmailAddress()));

		to = Recipient.normalize(pstMessage.getTo());
		cc = Recipient.normalize(pstMessage.getCc());
		bcc = Recipient.normalize(pstMessage.getBcc());

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
		logger.debug("Done loading pst message");

	}

	public static String toJSON(IndexedMailMessage message)
			throws JsonGenerationException, JsonMappingException, IOException {
		String result = null;
		logger.debug("Starting toJSON serialization");
		try {
			result = mapper.writeValueAsString(message);
		} finally {
			logger.debug("Finished toJSON serialization " + result.length()
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
		this.attachments = IndexedAttachment.fromArray(attachments);
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public IndexedObjectType getType() {
		return type;
	}

	public void setType(IndexedObjectType type) {
		this.type = type;
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
