package com.reqo.ironhold.storage.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.common.Base64;

@SuppressWarnings("serial")
public class IMAPMailMessage implements Serializable {
	private static Logger logger = Logger.getLogger(IMAPMailMessage.class);

	private String messageId;
	private Recipient from;
	private List<Recipient> to = new ArrayList<Recipient>();
	private List<Recipient> cc = new ArrayList<Recipient>();
	private List<Recipient> bcc = new ArrayList<Recipient>();
	private List<Header> headers = new ArrayList<Header>();
	

	private String subject;
	private Date messageDate;
	
	private String body;

	public IMAPMailMessage() {
	}

	public IMAPMailMessage(Message message) throws Exception {
		this();

		this.messageId = message.getHeader("Message-ID")[0];
		this.messageDate = message.getReceivedDate();

		Enumeration<Header> allHeaders = message.getAllHeaders();

		while (allHeaders.hasMoreElements()) {
			Header header = allHeaders.nextElement();
			headers.add(header);
		}
		
		this.from = new Recipient(message.getFrom()[0]);
		for (Address address : message.getRecipients(RecipientType.TO)) {
			this.to.add(new Recipient(address));
		}
		for (Address address : message.getRecipients(RecipientType.CC)) {
			this.cc.add(new Recipient(address));
		}
		for (Address address : message.getRecipients(RecipientType.BCC)) {
			this.bcc.add(new Recipient(address));
		}
		this.subject = message.getSubject();
		
		handleMessage(message);
		
	}


	private void handleMessage(Message message) throws IOException,
			MessagingException {
		Object content = message.getContent();
		if (content instanceof String) {
			this.body += (String) content;
		} else if (content instanceof Multipart) {
			Multipart mp = (Multipart) content;
			handleMultipart(mp);
		}
	}

	public void handleMultipart(Multipart mp) throws MessagingException,
			IOException {
		for (int i = 0; i < mp.getCount(); i++) {
			BodyPart bp = mp.getBodyPart(i);
			Object content = bp.getContent();
			if (content instanceof String) {
				this.body += (String) content;
			} else if (content instanceof Message) {
				Message message = (Message) content;
				handleMessage(message);
			} else if (content instanceof Multipart) {
				Multipart mp2 = (Multipart) content;
				handleMultipart(mp2);
			}
		}
	}

	private static ObjectMapper mapper = new ObjectMapper();

	public static String serializeMailMessage(IMAPMailMessage message)
			throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(message);
	}

	public static IMAPMailMessage deserializeMailMessage(String json)
			throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, IMAPMailMessage.class);
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public Recipient getFrom() {
		return from;
	}

	public void setFrom(Recipient from) {
		this.from = from;
	}

	public List<Recipient> getTo() {
		return to;
	}

	public void setTo(List<Recipient> to) {
		this.to = to;
	}

	public List<Recipient> getCc() {
		return cc;
	}

	public void setCc(List<Recipient> cc) {
		this.cc = cc;
	}

	public List<Recipient> getBcc() {
		return bcc;
	}

	public void setBcc(List<Recipient> bcc) {
		this.bcc = bcc;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Date getMessageDate() {
		return messageDate;
	}

	public void setMessageDate(Date messageDate) {
		this.messageDate = messageDate;
	}

	public List<Header> getHeaders() {
		return headers;
	}

	public void setHeaders(List<Header> headers) {
		this.headers = headers;
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
