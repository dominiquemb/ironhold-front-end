package com.reqo.ironhold.storage.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.elasticsearch.common.Base64;

import com.pff.PSTAppointment;
import com.pff.PSTAttachment;
import com.pff.PSTException;
import com.pff.PSTMessage;
import com.pff.PSTRecipient;
import com.reqo.ironhold.storage.model.mixin.CompressedAttachmentMixin;
import com.reqo.ironhold.storage.model.mixin.CompressedPSTMessageMixin;
import com.reqo.ironhold.storage.model.mixin.PSTAppointmentMixin;
import com.reqo.ironhold.storage.model.mixin.PSTMessageMixin;

@JsonIgnoreProperties("attachments")
public class MailMessage {
	private static ObjectMapper mapper = new ObjectMapper();
	private static ObjectMapper compressedMapper = new ObjectMapper();

	static {
		compressedMapper.getSerializationConfig().addMixInAnnotations(
				ArchivedPSTMessage.class, CompressedPSTMessageMixin.class);

		compressedMapper.getSerializationConfig().addMixInAnnotations(
				Attachment.class, CompressedAttachmentMixin.class);

		compressedMapper.getDeserializationConfig().addMixInAnnotations(
				ArchivedPSTMessage.class, CompressedPSTMessageMixin.class);

		compressedMapper.getDeserializationConfig().addMixInAnnotations(
				Attachment.class, CompressedAttachmentMixin.class);

		compressedMapper.enableDefaultTyping();
		compressedMapper.configure(
				SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
	}
	static {
		mapper.getSerializationConfig().addMixInAnnotations(PSTMessage.class,
				PSTMessageMixin.class);
		mapper.getSerializationConfig().addMixInAnnotations(
				PSTAppointment.class, PSTAppointmentMixin.class);
		mapper.enableDefaultTyping();
		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
				false);
	}

	private ArchivedPSTMessage pstMessage;
	private IMAPMailMessage imapMailMessage;

	public IMAPMailMessage getImapMailMessage() {
		return imapMailMessage;
	}

	public void setImapMailMessage(IMAPMailMessage imapMailMessage) {
		this.imapMailMessage = imapMailMessage;
	}

	private IndexStatus indexed = IndexStatus.NOT_INDEXED;
	private Date storedDate;
	private String messageId;
	private MessageSource[] sources;

	private Attachment[] attachments = new Attachment[0];

	private boolean pstPartialFailure = false;
	private String pstObjectType;

	public MailMessage() {

	}

	public MailMessage(Message message, IMAPMessageSource source) throws Exception {
		this.imapMailMessage = new IMAPMailMessage(message);
		
		addSource(source);

		handleMessage(message);
	}

	public MailMessage(PSTMessage originalPSTMessage, PSTMessageSource source)
			throws JsonParseException, JsonMappingException,
			JsonGenerationException, IOException, PSTException {

		this.pstObjectType = originalPSTMessage.getClass().getSimpleName();
		this.pstMessage = mapper.readValue(
				mapper.writeValueAsString(originalPSTMessage),
				ArchivedPSTMessage.class);

		try {
			for (int i = 0; i < originalPSTMessage.getNumberOfRecipients(); i++) {
				try {
					PSTRecipient recipient = originalPSTMessage.getRecipient(i);
					switch (recipient.getRecipientType()) {
					case PSTMessage.RECIPIENT_TYPE_TO:
						pstMessage.addTo(new Recipient(recipient
								.getDisplayName(), recipient.getSmtpAddress()));
						break;
					case PSTMessage.RECIPIENT_TYPE_CC:
						pstMessage.addCc(new Recipient(recipient
								.getDisplayName(), recipient.getSmtpAddress()));
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					this.pstPartialFailure = true;
				}

			}
		} catch (ArrayIndexOutOfBoundsException e) {
			for (String displayTo : pstMessage.getDisplayTo().split(";")) {
				pstMessage.addTo(new Recipient(displayTo, null));
			}

			for (String displayCc : pstMessage.getDisplayCC().split(";")) {
				pstMessage.addCc(new Recipient(displayCc, null));
			}

		}

		for (String displayBcc : pstMessage.getDisplayBCC().split(";")) {
			pstMessage.addBcc(new Recipient(displayBcc, null));
		}

		try {
			for (int i = 0; i < originalPSTMessage.getNumberOfAttachments(); i++) {
				try {
					PSTAttachment attachment = originalPSTMessage
							.getAttachment(i);

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
					if (count > 0) {
						byte[] endBuffer = new byte[count];
						System.arraycopy(buffer, 0, endBuffer, 0, count);
					}
					out.write(buffer);

					this.addAttachment(new Attachment(attachment.getSize(),
							attachment.getCreationTime(), attachment
									.getModificationTime(), attachment
									.getFilename(), Base64.encodeBytes(out
									.toByteArray())));
				} catch (Exception e) {
					e.printStackTrace();
					this.pstPartialFailure = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.pstPartialFailure = true;
		}
		this.setMessageId(originalPSTMessage.getInternetMessageId());

		addSource(source);
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

	private void handleMessage(Message message) throws IOException,
			MessagingException {
		Object content = message.getContent();
		if (content instanceof Multipart) {
			Multipart mp = (Multipart) content;
			handleMultipart(mp);
		}
	}

	public void handleMultipart(Multipart mp) throws MessagingException,
			IOException {
		for (int i = 0; i < mp.getCount(); i++) {
			BodyPart bp = mp.getBodyPart(i);
			Object content = bp.getContent();
			if (content instanceof InputStream) {
				InputStream attachmentStream = (InputStream) content;
				String filename = bp.getFileName();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				// 8176 is the block size used internally and should give the
				// best
				// performance
				int bufferSize = 8176;
				byte[] buffer = new byte[bufferSize];
				int count = attachmentStream.read(buffer);
				while (count == bufferSize) {
					out.write(buffer);
					count = attachmentStream.read(buffer);
				}
				byte[] endBuffer = new byte[count];
				System.arraycopy(buffer, 0, endBuffer, 0, count);
				out.write(buffer);

				addAttachment(new Attachment(bp.getSize(), new Date(), new Date(), filename, Base64.encodeBytes(out.toByteArray())));
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

	public static String serializeAttachments(Attachment[] attachments)
			throws IOException {
		return mapper.writeValueAsString(attachments);
	}

	public static String serializeCompressedAttachments(Attachment[] attachments)
			throws JsonGenerationException, JsonMappingException, IOException {
		return compressedMapper.writeValueAsString(attachments);
	}

	public static String serializeMailMessage(MailMessage message)
			throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(message);
	}

	public static String serializeCompressedMailMessage(MailMessage message)
			throws JsonGenerationException, JsonMappingException, IOException {

		return compressedMapper.writeValueAsString(message);
	}

	public static MailMessage deserializeCompressedMailMessage(String json)
			throws JsonGenerationException, JsonMappingException, IOException {

		return compressedMapper.readValue(json, MailMessage.class);
	}

	public static Attachment[] deserializeAttachments(String json)
			throws IOException {
		return mapper.readValue(json, Attachment[].class);
	}

	public static Attachment[] deserializeCompressedAttachments(String json)
			throws IOException {
		return compressedMapper.readValue(json, Attachment[].class);
	}

	public static MailMessage deserializeMailMessage(String json)
			throws IOException {
		return mapper.readValue(json, MailMessage.class);
	}

	public void addAttachment(Attachment attachment) {
		Attachment[] copy = Arrays.copyOf(attachments, attachments.length + 1);
		copy[attachments.length] = attachment;
		attachments = copy;

	}

	public Attachment[] getAttachments() {
		return attachments;
	}

	public void setAttachments(Attachment[] attachments) {
		this.attachments = attachments;
	}

	public void removeAttachments() {
		this.attachments = new Attachment[0];
	}

	public ArchivedPSTMessage getPstMessage() {
		return pstMessage;
	}

	public void setPstMessage(ArchivedPSTMessage pstMessage) {
		this.pstMessage = pstMessage;
	}

	public IndexStatus getIndexed() {
		return indexed;
	}

	public void setIndexed(IndexStatus indexed) {
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

	public boolean isPstPartialFailure() {
		return pstPartialFailure;
	}

	public void setPstPartialFailure(boolean pstPartialFailure) {
		this.pstPartialFailure = pstPartialFailure;
	}

	public String getPstObjectType() {
		return pstObjectType;
	}

	public void setPstObjectType(String pstObjectType) {
		this.pstObjectType = pstObjectType;
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
