package com.reqo.ironhold.reader.bloomberg.model.bloomberg.converters;

import com.reqo.ironhold.reader.bloomberg.model.dscl.DisclaimerType;
import com.reqo.ironhold.reader.bloomberg.model.msg.Attachment;
import com.reqo.ironhold.reader.bloomberg.model.msg.Message;
import com.reqo.ironhold.reader.bloomberg.model.msg.Recipient;
import com.reqo.ironhold.reader.bloomberg.model.msg.UserInfo;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.search.MessageTypeEnum;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.*;
import org.apache.commons.vfs.*;
import org.apache.commons.vfs.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.log4j.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;

/**
 * User: ilya
 * Date: 9/2/13
 * Time: 6:30 AM
 */
public class MessageConverter {
    private static Logger logger = Logger.getLogger(MessageConverter.class);

    public static MimeMailMessage convert(Message message, DisclaimerType disclaimer, String attachmentsZip) throws EmailException, IOException, MessagingException {


        Email email = containsHtml(message) || message.getAttachment().size() > 0 ? new HtmlEmail() : new MultiPartEmail();
        email.setFrom(getEmailFromUserInfo(message.getSender().getUserInfo()), getNameFromUserInfo(message.getSender().getUserInfo()));

        Collection<InternetAddress> to = new ArrayList<>();
        Collection<InternetAddress> cc = new ArrayList<>();
        Collection<InternetAddress> bcc = new ArrayList<>();

        for (Recipient recipient : message.getRecipient()) {
            InternetAddress a = new InternetAddress(getEmailFromUserInfo(recipient.getUserInfo()), getNameFromUserInfo(recipient.getUserInfo()));


            switch (recipient.getDeliveryType()) {
                case "TO":
                    to.add(a);
                    addHeaders(email, "X-Recipient-" + recipient.getDeliveryType() + "-" + to.size(), recipient.getUserInfo());
                    break;
                case "CC":
                    cc.add(a);
                    addHeaders(email, "X-Recipient-" + recipient.getDeliveryType() + "-" + cc.size(), recipient.getUserInfo());
                    break;
                case "BCC":
                    bcc.add(a);
                    addHeaders(email, "X-Recipient-" + recipient.getDeliveryType() + "-" + bcc.size(), recipient.getUserInfo());
                    break;
            }
        }

        if (to.size() > 0) email.setTo(to);
        if (cc.size() > 0) email.setCc(cc);
        if (bcc.size() > 0) email.setBcc(bcc);

        email.setSubject(getSubject(message));
        if (email instanceof HtmlEmail) {

            ((HtmlEmail) email).setTextMsg(getBody(message, disclaimer));
            String html = getHTMLBody(message, attachmentsZip);
            if (!html.equalsIgnoreCase(StringUtils.EMPTY)) {
                ((HtmlEmail) email).setHtmlMsg(html);
            }
        } else {
            email.setMsg(getBody(message, disclaimer));

        }

        processAttachments(email, message, attachmentsZip);
        email.setSentDate(new Date(1000 * Long.parseLong(message.getMsgTimeUTC().getContent().get(0))));
        email.addHeader("X-IronHoldMessageType", MessageTypeEnum.BLOOMBERG_MESSAGE.name());
        email.addHeader("X-MsgID", message.getMsgID().getContent().get(0));
        email.addHeader("X-MsgTime", message.getMsgTime().getContent().get(0));
        email.addHeader("X-MsgTimeUTC", message.getMsgTimeUTC().getContent().get(0));
        email.addHeader("X-MsgLang", message.getMsgLang().getContent().get(0));

        if (message.getMsgType() != null && message.getMsgType().getContent().size() > 0)
            email.addHeader("X-MsgType", message.getMsgType().getContent().get(0));

        if (message.getOnBehalfOf() != null && message.getOnBehalfOf().getUserInfo() != null)
            addHeaders(email, "X-OnBeHalfOf-", message.getOnBehalfOf().getUserInfo());

        if (message.getSharedMessenger() != null && message.getSharedMessenger().getUserInfo() != null)
            addHeaders(email, "X-SharedMessenger-", message.getSharedMessenger().getUserInfo());

        if (message.getOrigSender() != null && message.getOrigSender().getUserInfo() != null)
            addHeaders(email, "X-OrigSender-", message.getOrigSender().getUserInfo());

        if (message.getGreeting() != null && message.getGreeting().getContent().size() > 0)
            email.addHeader("X-Greeting", message.getGreeting().getContent().get(0).replaceAll("(\\r|\\n)", ""));

        if (disclaimer != null) {
            email.addHeader("X-DisclaimerReference", disclaimer.getDisclaimerReference());
            email.addHeader("X-DisclaimerText", disclaimer.getDisclaimerText().replaceAll("(\\r|\\n)", ""));
        }

        addHeaders(email, "X-Sender", message.getSender().getUserInfo());

        String hostname = java.net.InetAddress.getLocalHost().getHostName();
        email.setHostName(hostname);
        email.buildMimeMessage();


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        email.getMimeMessage().writeTo(baos);
        String rawContents = baos.toString();

        String messageId = message.getMsgID().getContent().get(0);
        rawContents = rawContents.replaceFirst(email.getMimeMessage().getMessageID(), Matcher.quoteReplacement(messageId));

        MimeMailMessage mimeMailMessage = new MimeMailMessage();
        mimeMailMessage.loadMimeMessageFromSource(rawContents);


        return mimeMailMessage;
    }

    private static void processAttachments(Email email, Message message, String attachmentsZip) throws IOException, EmailException {
        for (Attachment attachment : message.getAttachment()) {
            if (attachment.getFileName() != null && attachment.getFileName().getContent().size() > 0 && !attachment.getFileName().getContent().get(0).equalsIgnoreCase("alt_body.html")) {
                String fileName = attachment.getFileName().getContent().get(0);

                InputStream contents = getAttachmentAsStream(attachmentsZip, attachment.getFileID().getContent().get(0));
                ((HtmlEmail) email).attach(new ByteArrayDataSource(contents, "application/octet-stream"), fileName, fileName, EmailAttachment.ATTACHMENT);
            }
        }
    }

    private static InputStream getAttachmentAsStream(String attachmentsZip, String name) throws FileSystemException {
        String dateSuffix = attachmentsZip.replaceAll(".*\\.att\\.", "").replaceAll("\\.tar\\.gz", "");
        FileSystemManager fsManager = VFS.getManager();
        String pathToFile = "tgz:" + attachmentsZip + "!/bloomberg_attachments_" + dateSuffix + "/" + name;
        logger.info("Attempting to resolve " + pathToFile);
        FileSystemOptions opts = new FileSystemOptions();
        FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
        FtpFileSystemConfigBuilder.getInstance().setDataTimeout(opts, null);
        FileObject file = fsManager.resolveFile(pathToFile, opts);
        return file.getContent().getInputStream();
    }

    private static String getHTMLBody(Message message, String attachmentsZip) throws IOException {
        if (message.getAttachment() != null) {
            for (Attachment attachment : message.getAttachment()) {
                if (attachment.getFileName() != null && attachment.getFileName().getContent().size() > 0 && attachment.getFileName().getContent().get(0).equalsIgnoreCase("alt_body.html")) {
                    return getAttachmentAsString(attachmentsZip, attachment.getFileID().getContent().get(0));
                }
            }
        }
        return StringUtils.EMPTY;
    }

    private static String getAttachmentAsString(String attachmentsZip, String name) throws IOException {
        String dateSuffix = attachmentsZip.replaceAll(".*\\.att\\.", "").replaceAll("\\.tar\\.gz", "");
        FileSystemManager fsManager = VFS.getManager();
        String pathToFile = "tgz:" + attachmentsZip + "!/bloomberg_attachments_" + dateSuffix + "/" + name;
        logger.info("Attempting to resolve " + pathToFile);
        FileSystemOptions opts = new FileSystemOptions();
        FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
        FtpFileSystemConfigBuilder.getInstance().setDataTimeout(opts, null);
        FileObject file = fsManager.resolveFile(pathToFile, opts);
        return StringUtils.join(IOUtils.readLines(file.getContent().getInputStream()), "\n");
    }

    private static boolean containsHtml(Message message) {
        if (message.getAttachment() != null) {
            for (Attachment attachment : message.getAttachment()) {
                if (attachment.getFileName() != null && attachment.getFileName().getContent().size() > 0 && attachment.getFileName().getContent().get(0).equalsIgnoreCase("alt_body.html")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void addHeaders(Email email, String headerPrefix, UserInfo userInfo) {
        if (userInfo.getFirstName().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-FirstName", userInfo.getFirstName().getContent().get(0));

        if (userInfo.getLastName().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-LastName", userInfo.getLastName().getContent().get(0));

        if (userInfo.getFirmNumber() != null && userInfo.getFirmNumber().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-FirmNumber", userInfo.getFirmNumber().getContent().get(0));

        if (userInfo.getAccountName().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-AccountName", userInfo.getAccountName().getContent().get(0));

        if (userInfo.getAccountNumber() != null && userInfo.getAccountNumber().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-AccountNumber", userInfo.getAccountNumber().getContent().get(0));

        if (userInfo.getBloombergUUID() != null && userInfo.getBloombergUUID().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-BloombergUUID", userInfo.getBloombergUUID().getContent().get(0));

        if (userInfo.getBloombergEmailAddress().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-BloombergEmailAddress", userInfo.getBloombergEmailAddress().getContent().get(0));

        if (userInfo.getCorporateEmailAddress().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-CorporateEmailAddress", userInfo.getCorporateEmailAddress().getContent().get(0));

        if (userInfo.getClientID1() != null && userInfo.getClientID1().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-ClientID1", userInfo.getClientID1().getContent().get(0));

        if (userInfo.getClientID2() != null && userInfo.getClientID2().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-ClientID2", userInfo.getClientID2().getContent().get(0));

    }

    private static String getBody(Message message, DisclaimerType disclaimerType) {
        String body = "";
        if (message.getGreeting() != null && message.getGreeting().getContent() != null && message.getGreeting().getContent().size() != 0) {
            body += "Greeting: " + message.getGreeting().getContent().get(0).replaceAll("(\\r|\\n)", "") + "\n";
        }

        if (message.getMsgBody() != null && message.getMsgBody().getContent() != null && message.getMsgBody().getContent().size() != 0) {
            body += message.getMsgBody().getContent().get(0);
        }

        if (disclaimerType != null) {
            body += "\nDisclaimer: " + disclaimerType.getDisclaimerText().replaceAll("(\\r|\\n)", "");
        }

        return body;
    }

    private static String getSubject(Message message) {
        if (message.getSubject() != null && message.getSubject().getContent() != null && message.getSubject().getContent().size() != 0) {
            return message.getSubject().getContent().get(0).replaceAll("(\\r|\\n)", "");
        }


        return StringUtils.EMPTY;
    }

    private static String getEmailFromUserInfo(UserInfo userInfo) {
        if (userInfo.getCorporateEmailAddress().getContent() != null && userInfo.getCorporateEmailAddress().getContent().size() != 0) {
            return userInfo.getCorporateEmailAddress().getContent().get(0);
        }
        if (userInfo.getBloombergEmailAddress().getContent() != null && userInfo.getBloombergEmailAddress().getContent().size() != 0) {
            return userInfo.getBloombergEmailAddress().getContent().get(0);
        }

        return StringUtils.EMPTY;

    }

    private static String getNameFromUserInfo(UserInfo userInfo) {
        String firstName = "";
        if (userInfo.getFirstName().getContent() != null && userInfo.getFirstName().getContent().size() != 0) {
            firstName = userInfo.getFirstName().getContent().get(0);
        }
        String lastName = "";
        if (userInfo.getLastName().getContent() != null && userInfo.getLastName().getContent().size() != 0) {
            lastName = userInfo.getLastName().getContent().get(0);
        }

        return firstName + " " + lastName;
    }

}
