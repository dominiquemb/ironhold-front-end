package com.reqo.ironhold.storage.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ImapMailMessage implements Serializable {
    /*private static Logger logger = Logger.getLogger(ImapMailMessage.class);

        private String messageId;
        private Recipient from;
        private List<Recipient> to = new ArrayList<Recipient>();
        private List<Recipient> cc = new ArrayList<Recipient>();
        private List<Recipient> bcc = new ArrayList<Recipient>();
        private String subject;
        private Date recievedDate;
        private List<Attachment> attachments = new ArrayList<Attachment>();
        private String body;
        private boolean indexed = false;
        private List<String> sources = new ArrayList<String>();
        private Date storedDate;

        public ImapMailMessage() {
        }

        public ImapMailMessage(IStorageService storageService, PSTMessage pstMessage,
                String fileName) throws Exception {
            this();

            this.getSources().add(fileName);
            this.messageId = pstMessage.getInternetMessageId();
            this.setRecievedDate(pstMessage.getMessageDeliveryTime());
            this.from = new Recipient(pstMessage.getSenderName(),
                    pstMessage.getSenderEmailAddress(), pstMessage.getSenderEmailAddress());
            try {
                for (int i = 0; i < pstMessage.getNumberOfRecipients(); i++) {
                    PSTRecipient recipient = pstMessage.getRecipient(i);
                    if (recipient.getRecipientType() == PSTRecipient.MAPI_TO) {
                        to.add(new Recipient(recipient.getDisplayName(), recipient
                                .getEmailAddress(), recipient.getSmtpAddress()));
                    }
                    if (recipient.getRecipientType() == PSTRecipient.MAPI_CC) {
                        cc.add(new Recipient(recipient.getDisplayName(), recipient
                                .getEmailAddress(), recipient.getSmtpAddress()));
                    }
                    if (recipient.getRecipientType() == PSTRecipient.MAPI_BCC) {
                        bcc.add(new Recipient(recipient.getDisplayName(), recipient
                                .getEmailAddress(), recipient.getSmtpAddress()));
                    }
                }
            } catch (IndexOutOfBoundsException ignore) {
                LogMessage warningMessage = new LogMessage(LogLevel.Warning,
                        messageId, ignore.getMessage());

                storageService.log(warningMessage);
            }
            this.subject = pstMessage.getSubject();
            this.body = pstMessage.getBody();
            int numberOfAttachments = pstMessage.getNumberOfAttachments();
            for (int x = 0; x < numberOfAttachments; x++) {
                try {
                    PSTAttachment attach = pstMessage.getAttachment(x);
                    InputStream attachmentStream = attach.getFileInputStream();
                    // both long and short filenames can be used for attachments
                    String filename = attach.getLongFilename();
                    if (filename.isEmpty()) {
                        filename = attach.getFilename();
                    }
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

                    this.attachments.add(new Attachment(filename, Base64
                            .encodeBase64String(out.toByteArray())));
                    attachmentStream.close();
                } catch (Exception e) {
                    LogMessage warningMessage = new LogMessage(LogLevel.Warning,
                            messageId, "Failed to process attachment: " + e.getMessage());

                    storageService.log(warningMessage);
                }
            }
        }

        public ImapMailMessage(Message message) throws MessagingException, IOException {
            this();

            this.messageId = message.getHeader("Message-Id")[0];
            this.setRecievedDate(message.getReceivedDate());
            this.from = new Recipient(message.getFrom()[0].toString(), message.getFrom()[0].toString(),
            message.getFrom()[0].toString());

            for (Address recipient : message.getRecipients(RecipientType.TO)) {
                to.add(new Recipient(recipient.toString(), recipient.toString(),
                        recipient.toString()));
            }

            for (Address recipient : message.getRecipients(RecipientType.CC)) {
                cc.add(new Recipient(recipient.toString(), recipient.toString(),
                        recipient.toString()));
            }

            for (Address recipient : message.getRecipients(RecipientType.BCC)) {
                bcc.add(new Recipient(recipient.toString(), recipient.toString(),
                        recipient.toString()));
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
                } else if (content instanceof InputStream) {
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

                    this.attachments.add(new Attachment(filename, Base64
                            .encodeBase64String(out.toByteArray())));
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

        private static ObjectMapper mapper = new ObjectMapper();

        public static String serializeMailMessage(ImapMailMessage message)
                throws JsonGenerationException, JsonMappingException, IOException {
            return mapper.writeValueAsString(message);
        }

        public static ImapMailMessage deserializeMailMessage(String json) throws JsonParseException,
                JsonMappingException, IOException {
            return mapper.readValue(json, ImapMailMessage.class);
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

        public Date getRecievedDate() {
            return recievedDate;
        }

        public void setRecievedDate(Date recievedDate) {
            this.recievedDate = recievedDate;
        }

        public List<Attachment> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<Attachment> attachments) {
            this.attachments = attachments;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public boolean isIndexed() {
            return indexed;
        }

        public void setIndexed(boolean indexed) {
            this.indexed = indexed;
        }

        public List<String> getSources() {
            return sources;
        }

        public void setSources(List<String> sources) {
            this.sources = sources;
        }

        public Date getStoredDate() {
            return storedDate;
        }

        public void setStoredDate(Date storedDate) {
            this.storedDate = storedDate;
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

    */
}
