package com.reqo.ironhold.storage.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
import org.elasticsearch.common.Base64;

import com.reqo.ironhold.storage.model.mixin.CompressedAttachmentMixin;
import com.reqo.ironhold.storage.model.mixin.CompressedIMAPMailMessage;
import com.reqo.ironhold.storage.model.mixin.CompressedPSTMessageMixin;

@SuppressWarnings("serial")
public class MimeMailMessage implements Serializable {
	private static Logger logger = Logger.getLogger(MimeMailMessage.class);

	private static ObjectMapper mapper = new ObjectMapper();
	private static ObjectMapper compressedMapper = new ObjectMapper();

	static {
		compressedMapper.getSerializationConfig().addMixInAnnotations(
				Attachment.class, CompressedAttachmentMixin.class);

		compressedMapper.getDeserializationConfig().addMixInAnnotations(
				Attachment.class, CompressedAttachmentMixin.class);

		compressedMapper.enableDefaultTyping();
		compressedMapper.configure(
				SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
	}
	static {
		mapper.enableDefaultTyping();
		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
				false);
	}

	// Derived fields
	@JsonIgnore
	private Recipient from;
	@JsonIgnore
	private Recipient[] to = new Recipient[0];
	@JsonIgnore
	private Recipient[] cc = new Recipient[0];
	@JsonIgnore
	private Recipient[] bcc = new Recipient[0];
	@JsonIgnore
	private String subject = StringUtils.EMPTY;
	@JsonIgnore
	private Date messageDate;
	@JsonIgnore
	private String body = StringUtils.EMPTY;
	@JsonIgnore
	private String bodyHTML = StringUtils.EMPTY;
	@JsonIgnore
	private int size;
	@JsonIgnore
	private String bodyHTMLContentType;
	@JsonIgnore
	private String bodyContentType;
	@JsonIgnore
	private Attachment[] attachments = new Attachment[0];
	@JsonIgnore
	private String rawContents;

	private IndexStatus indexed = IndexStatus.NOT_INDEXED;
	private Date storedDate;
	private String messageId;
	private MessageSource[] sources;

	public MimeMailMessage() {
	}

	public void loadMimeMessageFromSource(String source)
			throws MessagingException, IOException {
		InputStream is = new ByteArrayInputStream(source.getBytes());
		MimeMessage mimeMessage = new MimeMessage(null, is);

		loadMimeMessage(mimeMessage);
	}

	public void loadMimeMessage(MimeMessage mimeMessage)
			throws MessagingException, IOException {
		long started = System.currentTimeMillis();
		try {
			this.messageId = mimeMessage.getMessageID();

			populateRawContents(mimeMessage);

			this.messageDate = mimeMessage.getSentDate();
			this.size = rawContents.getBytes().length;

			InternetAddress internetAddress = (InternetAddress) mimeMessage
					.getFrom()[0];

			this.from = new Recipient(internetAddress.getPersonal(),
					internetAddress.getAddress());
			try {
				if (mimeMessage.getRecipients(RecipientType.TO) != null) {
					for (Address address : mimeMessage
							.getRecipients(RecipientType.TO)) {
						internetAddress = (InternetAddress) address;
						addTo(new Recipient(internetAddress.getPersonal(),
								internetAddress.getAddress()));
					}
				}
			} catch (AddressException e) {
				for (String headerTo : mimeMessage.getHeader("TO")) {
					addTo(new Recipient(headerTo, headerTo));
				}

			}
			try {

				if (mimeMessage.getRecipients(RecipientType.CC) != null) {
					for (Address address : mimeMessage
							.getRecipients(RecipientType.CC)) {
						internetAddress = (InternetAddress) address;
						addCc(new Recipient(internetAddress.getPersonal(),
								internetAddress.getAddress()));
					}
				}
			} catch (AddressException e) {
				for (String headerCc : mimeMessage.getHeader("CC")) {
					addCc(new Recipient(headerCc, headerCc));
				}

			}
			try {
				if (mimeMessage.getRecipients(RecipientType.BCC) != null) {
					for (Address address : mimeMessage
							.getRecipients(RecipientType.BCC)) {
						internetAddress = (InternetAddress) address;
						addBcc(new Recipient(internetAddress.getPersonal(),
								internetAddress.getAddress()));
					}
				}

			} catch (AddressException e) {
				for (String headerBcc : mimeMessage.getHeader("BCC")) {
					addBcc(new Recipient(headerBcc, headerBcc));
				}

			}
			this.subject = mimeMessage.getSubject();

			handleMessage(mimeMessage);

		} finally {
			long finished = System.currentTimeMillis();
			logger.info("loadMimeMessage in " + (finished - started) + "ms");
		}

	}

	public void addSource(MessageSource source) {
		if (sources == null) {
			sources = new MessageSource[] { source };
		} else {
			MessageSource[] copy = Arrays.copyOf(sources, sources.length + 1);
			copy[sources.length] = source;
			sources = copy;
		}
	}

	private void populateRawContents(MimeMessage mimeMessage)
			throws IOException, MessagingException {
		long started = System.currentTimeMillis();
		int bufferCount = 0;
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			List<String> lines = Collections.list(mimeMessage
					.getAllHeaderLines());
			for (String line : lines) {
				os.write((line + "\n").getBytes());
			}
			os.write("\n".getBytes());
			InputStream rawStream = mimeMessage.getRawInputStream();
			int read = 0;
			byte[] bytes = new byte[4096	];

			while ((read = rawStream.read(bytes)) != -1) {
				os.write(bytes, 0, read);
				bufferCount++;
			}
			rawStream.close();

			this.setRawContents(os.toString());
		} finally {
			long finished = System.currentTimeMillis();
			logger.info("populateRawContents (" + bufferCount +" buffers) in " + (finished - started) + "ms");
		}
	}

	private void handleMessage(Message message) throws IOException,
			MessagingException {
		long started = System.currentTimeMillis();
		try {
			Object content = message.getContent();
			if (content instanceof String) {
				if (message.getContentType().startsWith("text/html")) {
					this.bodyHTML += (String) content;
					this.setBodyHTMLContentType(message.getContentType());
				} else if (message.getContentType().startsWith("text/plain")) {
					this.body += (String) content;
					this.setBodyContentType(message.getContentType());
				}
			} else if (content instanceof Multipart) {
				Multipart mp = (Multipart) content;
				handleMultipart(mp);
			}
		} finally {
			long finished = System.currentTimeMillis();
			logger.info("handleMessage in " + (finished - started) + "ms");
		}
	}

	public void handleMultipart(Multipart mp) throws MessagingException,
			IOException {
		for (int i = 0; i < mp.getCount(); i++) {
			BodyPart bp = mp.getBodyPart(i);
			Object content = bp.getContent();

			if (content instanceof String) {
				if (bp.getContentType().startsWith("text/html")) {
					this.bodyHTML += (String) content;
					this.setBodyHTMLContentType(bp.getContentType());
				} else if (bp.getContentType().startsWith("text/plain")) {
					this.body += (String) content;
					this.setBodyContentType(bp.getContentType());
				}
			} else if (content instanceof InputStream) {
				InputStream attachmentStream = (InputStream) content;
				ByteArrayOutputStream out = new ByteArrayOutputStream();

				String filename = bp.getFileName();

				byte[] buf = new byte[4096];
				int bytesRead;
				while ((bytesRead = attachmentStream.read(buf)) != -1) {
					out.write(buf, 0, bytesRead);
				}

				addAttachment(new Attachment(bp.getSize(), new Date(),
						new Date(), filename, Base64.encodeBytes(out
								.toByteArray()), bp.getContentType(),
						bp.getDisposition()));
				bp.getContentType();
				bp.getDisposition();
				attachmentStream.close();
			} else if (content instanceof Message) {
				Message message = (Message) content;
				handleMessage(message);
			} else if (content instanceof Multipart) {
				Multipart mp2 = (Multipart) content;
				handleMultipart(mp2);
			}
		}
	}

	public static String serialize(MimeMailMessage message)
			throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(message);
	}

	public static MimeMailMessage deserialize(String json)
			throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, MimeMailMessage.class);
	}

	public static String serializeAttachments(Attachment[] attachments)
			throws IOException {
		return mapper.writeValueAsString(attachments);
	}

	public static String serializeCompressedAttachments(Attachment[] attachments)
			throws JsonGenerationException, JsonMappingException, IOException {
		return compressedMapper.writeValueAsString(attachments);
	}

	public void addAttachment(Attachment attachment) {
		Attachment[] copy = Arrays.copyOf(attachments, attachments.length + 1);
		copy[attachments.length] = attachment;
		attachments = copy;

	}

	public void removeAttachments() {
		this.attachments = new Attachment[0];
	}

	public Attachment[] getAttachments() {
		return attachments;
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

	public String getBodyHTML() {
		return bodyHTML;
	}

	public void setBodyHTML(String bodyHTML) {
		this.bodyHTML = bodyHTML;
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

	public String getBodyContentType() {
		return bodyContentType;
	}

	public void setBodyContentType(String bodyContentType) {
		this.bodyContentType = bodyContentType;
	}

	public String getBodyHTMLContentType() {
		return bodyHTMLContentType;
	}

	public void setBodyHTMLContentType(String bodyHTMLContentType) {
		this.bodyHTMLContentType = bodyHTMLContentType;
	}

	public String getRawContents() {
		return rawContents;
	}

	public void setRawContents(String rawContents) {
		this.rawContents = rawContents;
	}

	public IndexStatus getIndexed() {
		return indexed;
	}

	public void setIndexed(IndexStatus indexed) {
		this.indexed = indexed;
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

	public MessageSource[] getSources() {
		return sources;
	}

	public void setSources(MessageSource[] sources) {
		this.sources = sources;
	}

}
