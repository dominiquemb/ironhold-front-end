package com.reqo.ironhold.storage.model.message;

import com.pff.PSTAttachment;
import com.pff.PSTException;
import com.pff.PSTMessage;
import com.pff.PSTRecipient;
import com.reqo.ironhold.storage.model.IHasMessageId;
import com.reqo.ironhold.storage.model.IPartitioned;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.elasticsearch.common.Base64;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unchecked"})
public class MimeMailMessage implements IHasMessageId, IPartitioned, ISubPartitioned {
    public static final String IMPORTANCE_HIGH = "high";
    public static final String IMPORTANCE_LOW = "low";

    private static final int BUFFER_SIZE = 20000;

    private static Logger logger = Logger.getLogger(MimeMailMessage.class);
    protected SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    protected SimpleDateFormat dayMonthFormat = new SimpleDateFormat("MMdd");


    // Derived fields

    private Recipient from;

    private Recipient[] to = new Recipient[0];

    private Recipient[] cc = new Recipient[0];

    private Recipient[] bcc = new Recipient[0];

    private String subject = StringUtils.EMPTY;

    private Date messageDate;

    private String body = StringUtils.EMPTY;

    private String bodyHTML = StringUtils.EMPTY;

    private int size;

    private String bodyHTMLContentType;

    private String bodyContentType;

    private Attachment[] attachments = new Attachment[0];

    private String rawContents;

    private boolean hasAttachments;

    private String importance;

    private String messageId;

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
        loadMimeMessage(mimeMessage, true);
    }

    public static MimeMailMessage getMimeMailMessage(PSTMessage originalPSTMessage) throws IOException, MessagingException, PSTException, EmailException {
        MimeMessage mimeMessage;
        if (isJournaledMessage(originalPSTMessage)) {
            mimeMessage = getMimeMessage(originalPSTMessage.getAttachment(0).getEmbeddedPSTMessage());
        } else {
            mimeMessage = getMimeMessage(originalPSTMessage);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mimeMessage.writeTo(baos);
        String rawContents = baos.toString();

        String messageId = originalPSTMessage.getInternetMessageId();
        if (messageId.length() < 10) {
            logger.warn("Found messageId that is too short, replacing with surrogate id: " + messageId);
            messageId = UUID.randomUUID().toString() + "-generated";

        }
        rawContents = rawContents.replaceFirst(mimeMessage.getMessageID(), Matcher.quoteReplacement(messageId));

        MimeMailMessage mimeMailMessage = new MimeMailMessage();
        mimeMailMessage.loadMimeMessageFromSource(rawContents);

        return mimeMailMessage;
    }

    private static boolean isJournaledMessage(PSTMessage originalPSTMessage) throws PSTException, IOException {
        if (originalPSTMessage.getNumberOfAttachments() == 1 && originalPSTMessage.getAttachment(0).getEmbeddedPSTMessage() != null) {
            return true;
        } else {
            return false;
        }
    }

    public static MimeMessage getMimeMessage(PSTMessage originalPSTMessage) throws EmailException, PSTException, IOException, MessagingException {
        HtmlEmail email = new HtmlEmail();
        if (originalPSTMessage.getPriority() == PSTMessage.IMPORTANCE_HIGH) {
            email.addHeader("Importance", IMPORTANCE_HIGH);
        }

        extractRecipients(email, originalPSTMessage);

        if (originalPSTMessage.getBody().trim().length() > 0) {
            email.setMsg(originalPSTMessage.getBody());
        } else {
            email.setMsg(" ");
        }
        if (originalPSTMessage.getBodyHTML().trim().length() > 0) {
            email.setHtmlMsg(originalPSTMessage.getBodyHTML());
        } else if (originalPSTMessage.getRTFBody().trim().length() > 0) {
            JEditorPane p = new JEditorPane();
            p.setContentType("text/rtf");
            EditorKit kitRtf = p.getEditorKitForContentType("text/rtf");
            try {
                kitRtf.read(new ByteArrayInputStream(originalPSTMessage.getRTFBody().trim().getBytes()), p.getDocument(), 0);
                kitRtf = null;
                EditorKit kitHtml = p.getEditorKitForContentType("text/html");
                Writer writer = new StringWriter();
                kitHtml.write(writer, p.getDocument(), 0, p.getDocument().getLength());
                email.setHtmlMsg(writer.toString());
            } catch (BadLocationException e) {
                logger.warn(e);
            }
        }


        try {
            email.setFrom(originalPSTMessage.getSenderEmailAddress(), originalPSTMessage.getSenderName());
        } catch (EmailException e) {
            try {
                email.setFrom(originalPSTMessage.getSentRepresentingEmailAddress(), originalPSTMessage.getSentRepresentingName());
            } catch (EmailException e2) {
                email.setFrom("unknown@unknown", originalPSTMessage.getSenderName());
            }
        }
        email.setSentDate(originalPSTMessage.getMessageDeliveryTime());
        email.setSubject(originalPSTMessage.getSubject());

        try {
            for (int i = 0; i < originalPSTMessage.getNumberOfAttachments(); i++) {
                try {
                    PSTAttachment attachment = originalPSTMessage
                            .getAttachment(i);

                    if (attachment.getEmbeddedPSTMessage() != null) {
                        MimeMessage embeddedMessage = MimeMailMessage.getMimeMessage(attachment.getEmbeddedPSTMessage());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        embeddedMessage.writeTo(baos);
                        String rawContents = baos.toString().replaceFirst(embeddedMessage.getMessageID(), Matcher.quoteReplacement(attachment.getEmbeddedPSTMessage().getInternetMessageId()));

                        email.attach(new ByteArrayDataSource(rawContents.getBytes(), "message/rfc822"), embeddedMessage.getSubject() + ".eml", embeddedMessage.getSubject());
                        if (email.getToAddresses().size() == 0 && email.getCcAddresses().size() == 0 && email.getBccAddresses().size() == 0) {
                            logger.warn("Found 0 recipients and embedded email message, extracting recipients from embedded message");
                            extractRecipients(email, attachment.getEmbeddedPSTMessage());

                        }
                    } else {
                        String fileName = attachment.getLongFilename();
                        if (fileName.isEmpty()) {
                            fileName = attachment.getFilename();
                        }

                        if (attachment.getAttachmentContentDisposition() != null && attachment.getAttachmentContentDisposition().trim().length() > 0) {
                            email.attach(new ByteArrayDataSource(attachment.getFileInputStream(), attachment.getMimeTag()), fileName, attachment.getDisplayName(), attachment.getAttachmentContentDisposition());
                        } else {
                            email.attach(new ByteArrayDataSource(attachment.getFileInputStream(), attachment.getMimeTag()), fileName, attachment.getDisplayName());
                        }
                    }

                } catch (Exception e1) {
                    logger.warn(e1);
                }

            }
        } catch (Exception e2) {
            logger.warn(e2);
        }

        if (email.getToAddresses().size() == 0 && email.getCcAddresses().size() == 0 && email.getBccAddresses().size() == 0) {
            email.addTo("undisclosed-recipients:;", "undisclosed-recipients:;");

        }
        String hostname = java.net.InetAddress.getLocalHost().getHostName();
        email.setHostName(hostname);
        email.buildMimeMessage();
        return email.getMimeMessage();

    }

    private static void extractRecipients(HtmlEmail email, PSTMessage message) throws PSTException, IOException, EmailException {

        try {
            for (int i = 0; i < message.getNumberOfRecipients(); i++) {
                try {
                    PSTRecipient recipient = message.getRecipient(i);
                    switch (recipient.getRecipientType()) {
                        case PSTMessage.RECIPIENT_TYPE_TO:
                            try {
                                email.addTo(recipient.getSmtpAddress(), recipient.getDisplayName());
                            } catch (EmailException e) {
                                email.addTo("unknown@unknown", recipient.getDisplayName());
                            }

                            break;
                        case PSTMessage.RECIPIENT_TYPE_CC:
                            try {
                                email.addCc(recipient.getSmtpAddress(), recipient.getDisplayName());
                            } catch (EmailException e) {
                                email.addCc("unknown@unknown", recipient.getDisplayName());
                            }

                            break;
                    }
                } catch (Exception e) {
                    logger.warn(e);
                }

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            if (message.getDisplayTo().trim().length() > 0) {
                for (String displayTo : message.getDisplayTo().split(";")) {
                    email.addTo("unknown@unknown", displayTo);
                }
            }

            if (message.getDisplayCC().trim().length() > 0) {
                for (String displayCc : message.getDisplayCC().split(";")) {
                    email.addCc("unknown@unknown", displayCc);
                }
            }
        }

        if (message.getDisplayBCC().trim().length() > 0) {
            for (String displayBcc : message.getDisplayBCC().split(";")) {
                email.addBcc("unknown@unknown", displayBcc);
            }
        }

    }

    public void loadMimeMessage(MimeMessage mimeMessage,
                                boolean processAttachments) throws MessagingException, IOException {
        long started = System.currentTimeMillis();
        try {
            this.messageId = mimeMessage.getMessageID();

            populateRawContents(mimeMessage);

            this.messageDate = mimeMessage.getSentDate();
            this.size = rawContents.getBytes().length;
            if (mimeMessage.getHeader("Importance") != null) {
                this.importance = mimeMessage.getHeader("Importance")[0];
            }

            InternetAddress internetAddress;
            try {
                internetAddress = (InternetAddress) mimeMessage.getFrom()[0];
                this.from = new Recipient(internetAddress.getPersonal(),
                        internetAddress.getAddress());

            } catch (AddressException e) {
                this.from = new Recipient(mimeMessage.getHeader("From")[0],
                        mimeMessage.getHeader("From")[0]);
            }

            try {
                internetAddress = (InternetAddress) mimeMessage.getFrom()[0];
                if (internetAddress != null) {
                    this.setFrom(new Recipient(internetAddress.getPersonal(),
                            internetAddress.getAddress()));
                }

            } catch (AddressException e) {
                // ignore
            }

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

            try {
                handleMessage(mimeMessage, processAttachments);
            } catch (UnsupportedEncodingException e) {
                if (e.getMessage().startsWith("3D")) {
                    String fixedRawContents = this.getRawContents()
                            .replaceAll("=3D", "=").replaceAll("3D\"", "\"")
                            .replaceAll("\"3D", "\"");
                    reset();
                    loadMimeMessageFromSource(fixedRawContents);
                } else if (e.getMessage().startsWith("\"")) {
                    //charset="\"Windows-1252\""
                    Pattern p = Pattern.compile("Content-Type: (.+); charset=\"\\\\\"(.+)\\\\\"\"");
                    Matcher m = p.matcher(this.getRawContents());
                    if (m.find()) {
                        // replace first number with "number" and second number with the first
                        String fixedRawContents = m.replaceFirst("Content-Type: $1; charset=\"$2\"");
                        reset();
                        loadMimeMessageFromSource(fixedRawContents);

                    } else {
                        throw e;
                    }

                } else {
                    throw e;
                }
            } catch (IOException e) {
                if (e.getMessage().equals("Unknown encoding: 8-bit")) {
                    String fixedRawContents = this.getRawContents()
                            .replaceAll("Content-Transfer-Encoding: 8-bit", "Content-Transfer-Encoding: 8bit");
                    reset();
                    loadMimeMessageFromSource(fixedRawContents);
                } else {
                    throw e;
                }
            } catch (NullPointerException e) {
                Pattern p = Pattern.compile("Content-Type: (.+); name=\\\"\\\"");
                Matcher m = p.matcher(this.getRawContents());
                if (m.find()) {
                    String fixedRawContents = m.replaceFirst("Content-Type: $1; name=\"unknown\"");
                    reset();
                    loadMimeMessageFromSource(fixedRawContents);

                } else {
                    throw e;
                }
            }

        } finally {
            long finished = System.currentTimeMillis();
            logger.info("loadMimeMessage in " + (finished - started) + "ms");
        }

    }

    private void reset() {
        from = null;
        to = new Recipient[0];
        cc = new Recipient[0];
        bcc = new Recipient[0];
        subject = StringUtils.EMPTY;
        messageDate = null;
        body = StringUtils.EMPTY;
        bodyHTML = StringUtils.EMPTY;
        size = 0;
        bodyHTMLContentType = null;
        bodyContentType = null;

        rawContents = null;
        hasAttachments = false;

        this.attachments = new Attachment[0];
    }

    private void populateRawContents(MimeMessage mimeMessage)
            throws IOException, MessagingException {
        long started = System.currentTimeMillis();
        int bufferCount = 0;
        try {
            logger.debug("populateRawContents - starting");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            List<String> lines = Collections.list(mimeMessage
                    .getAllHeaderLines());
            for (String line : lines) {
                os.write((line + "\n").getBytes());
            }
            os.write("\n".getBytes());
            logger.debug("populateRawContents - headers done");
            InputStream rawStream = mimeMessage.getRawInputStream();
            logger.debug("populateRawContents - recieved input stream");
            int read = 0;
            byte[] bytes = new byte[BUFFER_SIZE];

            while ((read = rawStream.read(bytes)) != -1) {
                os.write(bytes, 0, read);
                bufferCount++;
                logger.debug("populateRawContents - recieved buffer "
                        + bufferCount);
            }
            os.flush();
            os.close();

            logger.debug("populateRawContents - finished reading");
            rawStream.close();
            logger.debug("populateRawContents - closed stream");
            this.setRawContents(os.toString());
        } finally {
            long finished = System.currentTimeMillis();
            logger.info("populateRawContents (" + bufferCount + " buffers) in "
                    + (finished - started) + "ms");
        }
    }

    private void handleMessage(Message message, boolean processAttachments)
            throws IOException, MessagingException {
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
                handleMultipart(mp, processAttachments);
            }
        } catch (IOException e) {
            if (!e.getMessage().equals("No content")) {
                throw e;
            }
        } finally {
            long finished = System.currentTimeMillis();
            logger.info("handleMessage in " + (finished - started) + "ms");
        }
    }

    public void handleMultipart(Multipart mp, boolean processAttachments)
            throws MessagingException, IOException {
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
                this.setHasAttachments(true);
                if (processAttachments) {
                    InputStream attachmentStream = (InputStream) content;
                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                    String filename = bp.getFileName();

                    byte[] buf = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = attachmentStream.read(buf)) != -1) {
                        out.write(buf, 0, bytesRead);
                    }
                    out.flush();
                    out.close();

                    addAttachment(new Attachment(bp.getSize(), this.getMessageDate(),
                            this.getMessageDate(), filename, Base64.encodeBytes(out
                            .toByteArray()), bp.getContentType(),
                            bp.getDisposition()));
                    attachmentStream.close();
                }
            } else if (content instanceof Message) {
                Message message = (Message) content;
                handleMessage(message, processAttachments);
            } else if (content instanceof Multipart) {
                Multipart mp2 = (Multipart) content;
                handleMultipart(mp2, processAttachments);
            }
        }
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


    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public boolean isHasAttachments() {
        return hasAttachments;
    }

    public void setHasAttachments(boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    @Override
    @JsonIgnore
    public String getPartition() {
        return yearFormat.format(this.getMessageDate());
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    @JsonIgnore
    public String getCheckSum() throws NoSuchAlgorithmException {
        MessageDigest complete = MessageDigest.getInstance("MD5");
        complete.update(getRawContents().getBytes());

        return Base64.encodeBytes(complete.digest());
    }


    @Override
    public String getSubPartition() {
        return dayMonthFormat.format(this.getMessageDate());
    }
}
