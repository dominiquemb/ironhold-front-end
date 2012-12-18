package com.reqo.ironhold.storage.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

@SuppressWarnings("serial")
public class IMAPMailMessage implements Serializable {
	private static Logger logger = Logger.getLogger(IMAPMailMessage.class);

	private Recipient from;

	private Recipient[] to = new Recipient[0];
	private Recipient[] cc = new Recipient[0];
	private Recipient[] bcc = new Recipient[0];

	private IMAPHeader[] headers = new IMAPHeader[0];

	private String subject = StringUtils.EMPTY;
	private Date messageDate;

	private String body = StringUtils.EMPTY;

	private int size;


	public IMAPMailMessage() {
	}

	public IMAPMailMessage(Message message) throws Exception {
		this();

		this.messageDate = message.getReceivedDate();
		this.size = message.getSize();

		@SuppressWarnings("unchecked")
		Enumeration<Header> allHeaders = message.getAllHeaders();

		while (allHeaders.hasMoreElements()) {
			Header header = allHeaders.nextElement();
			addHeader(new IMAPHeader(header));
		}

		InternetAddress internetAddress = (InternetAddress) message.getFrom()[0];

		this.from = new Recipient(internetAddress.getPersonal(),
				internetAddress.getAddress());
		if (message.getRecipients(RecipientType.TO) != null) {
			for (Address address : message.getRecipients(RecipientType.TO)) {
				internetAddress = (InternetAddress) address;
				addTo(new Recipient(internetAddress.getPersonal(),
						internetAddress.getAddress()));
			}
		}
		if (message.getRecipients(RecipientType.CC) != null) {
			for (Address address : message.getRecipients(RecipientType.CC)) {
				internetAddress = (InternetAddress) address;
				addCc(new Recipient(internetAddress.getPersonal(),
						internetAddress.getAddress()));
			}
		}
		if (message.getRecipients(RecipientType.BCC) != null) {
			for (Address address : message.getRecipients(RecipientType.BCC)) {
				internetAddress = (InternetAddress) address;
				addBcc(new Recipient(internetAddress.getPersonal(),
						internetAddress.getAddress()));
			}
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

	public void addHeader(IMAPHeader header) {
		IMAPHeader[] copy = Arrays.copyOf(headers, headers.length + 1);
		copy[headers.length] = header;
		headers = copy;

	}

	public void addTo(Recipient recipient) {
		Recipient[] copy = Arrays.copyOf(to, to.length + 1);
		copy[to.length] = recipient;
		to = copy;

	}

	public void addCc(Recipient recipient) {
		Recipient[] copy = Arrays.copyOf(cc, cc.length + 1);
		copy[cc.length] = recipient;
		cc = copy;

	}

	public void addBcc(Recipient recipient) {
		Recipient[] copy = Arrays.copyOf(bcc, bcc.length + 1);
		copy[bcc.length] = recipient;
		bcc = copy;

	}

	public Recipient[] getTo() {
		return to;
	}

	public Recipient[] getCc() {
		return cc;
	}

	public Recipient[] getBcc() {
		return bcc;
	}

	public Recipient getFrom() {
		return from;
	}

	public void setFrom(Recipient from) {
		this.from = from;
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

	public IMAPHeader[] getHeaders() {
		return headers;
	}


	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
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
