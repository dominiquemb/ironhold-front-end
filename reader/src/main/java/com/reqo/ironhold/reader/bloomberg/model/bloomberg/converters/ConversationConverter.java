package com.reqo.ironhold.reader.bloomberg.model.bloomberg.converters;

import com.reqo.ironhold.reader.bloomberg.model.dscl.DisclaimerType;
import com.reqo.ironhold.reader.bloomberg.model.ib.*;
import com.reqo.ironhold.reader.bloomberg.model.ib.Attachment;
import com.reqo.ironhold.reader.bloomberg.model.ib.Message;
import com.reqo.ironhold.reader.bloomberg.model.msg.*;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.search.MessageTypeEnum;
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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;

/**
 * User: ilya
 * Date: 9/2/13
 * Time: 6:30 AM
 */
public class ConversationConverter {
    private static Logger logger = Logger.getLogger(ConversationConverter.class);

    public static MimeMailMessage convert(Conversation conversation, DisclaimerType disclaimer, String attachmentsZip) throws EmailException, IOException, MessagingException {


        MultiPartEmail email = new MultiPartEmail();
        Collection<InternetAddress> to = new HashSet<>();

        StringBuilder sb = new StringBuilder();

        for (Object o : conversation.getAttachmentOrInviteOrParticipantEntered()) {
            if (o instanceof com.reqo.ironhold.reader.bloomberg.model.ib.Attachment) {
                com.reqo.ironhold.reader.bloomberg.model.ib.Attachment attachment = (com.reqo.ironhold.reader.bloomberg.model.ib.Attachment) o;
                String fileName = attachment.getFileName().getContent().get(0);
                sb.append(String.format("[%s] %s <%s> attached %s\n",attachment.getDateTime().getContent().get(0), getName(attachment.getUser()), getEmail(attachment.getUser()), fileName));
                email.addPart(sb.toString(), "text/plain");
                sb = new StringBuilder();

                InputStream contents = getAttachmentAsStream(attachmentsZip, attachment.getFileID().getContent().get(0));
                email.attach(new ByteArrayDataSource(contents, "application/octet-stream"), fileName, fileName);
            } else if (o instanceof Invite) {
                Invite invite = (Invite) o;
                sb.append(String.format("[%s] %s <%s> invited %s <%s>\n", invite.getDateTime().getContent().get(0), getName(invite.getInviter()), getEmail(invite.getInviter()), getName(invite.getInvitee()), getEmail(invite.getInvitee())));
            } else if (o instanceof ParticipantEntered) {
                ParticipantEntered participantEntered = (ParticipantEntered) o;
                sb.append(String.format("[%s] %s <%s> joined\n", participantEntered.getDateTime().getContent().get(0), getName(participantEntered.getUser()), getEmail(participantEntered.getUser())));

                to.add(new InternetAddress(getEmail(participantEntered.getUser()), getName(participantEntered.getUser())));
                addHeaders(email, "X-Participant-" + to.size(), participantEntered.getUser());
            } else if (o instanceof ParticipantLeft) {
                ParticipantLeft participantLeft = (ParticipantLeft) o;
                sb.append(String.format("[%s] %s <%s> left\n", participantLeft.getDateTime().getContent().get(0), getName(participantLeft.getUser()), getEmail(participantLeft.getUser())));
            } else if (o instanceof com.reqo.ironhold.reader.bloomberg.model.ib.Message) {
                com.reqo.ironhold.reader.bloomberg.model.ib.Message message = (com.reqo.ironhold.reader.bloomberg.model.ib.Message) o;
                sb.append(String.format("[%s] %s <%s>: %s\n", message.getDateTime().getContent().get(0), getName(message.getUser()), getEmail(message.getUser()), StringUtils.join(message.getContent().getContent(), "\t\n")));
            } else if (o instanceof History) {
                History history = (History) o;
                sb.append(String.format("[%s] %s <%s> history started\n", history.getDateTime().getContent().get(0), getName(history.getUser()), getEmail(history.getUser())));
            } else if (o instanceof SystemMessage) {
                SystemMessage systemMessage = (SystemMessage) o;
                sb.append(String.format("[%s] SYSTEM: %s\n", systemMessage.getDateTime().getContent().get(0), StringUtils.join(systemMessage.getContent().getContent(), "\t\n")));
            }

        }

        email.addPart(sb.toString(), "text/plain");


        if (to.size() > 0) email.setTo(to);

        email.setSubject(String.format("Bloomberg Chat %s - %s", conversation.getStartTime().getContent().get(0), conversation.getEndTime().getContent().get(0)));


        email.setFrom("bloombergchat@bloomberg.net");
        email.setSentDate(new Date(1000 * Long.parseLong(conversation.getStartTimeUTC().getContent().get(0))));
        email.addHeader("X-IronHoldMessageType", MessageTypeEnum.BLOOMBERG_CHAT.name());
        String hostname = java.net.InetAddress.getLocalHost().getHostName();
        email.setHostName(hostname);
        email.buildMimeMessage();


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        email.getMimeMessage().writeTo(baos);
        String rawContents = baos.toString();

        String messageId = conversation.getRoomID().getContent().get(0);
        rawContents = rawContents.replaceFirst(email.getMimeMessage().getMessageID(), Matcher.quoteReplacement(messageId));

        MimeMailMessage mimeMailMessage = new MimeMailMessage();
        mimeMailMessage.loadMimeMessageFromSource(rawContents);


        return mimeMailMessage;
    }


    private static void addHeaders(Email email, String headerPrefix, User userInfo) {
        if (userInfo.getFirstName().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-FirstName", userInfo.getFirstName().getContent().get(0));

        if (userInfo.getLastName().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-LastName", userInfo.getLastName().getContent().get(0));

        if (userInfo.getFirmNumber() != null && userInfo.getFirmNumber().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-FirmNumber", userInfo.getFirmNumber().getContent().get(0));

        if (userInfo.getAccountNumber() != null && userInfo.getAccountNumber().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-AccountNumber", userInfo.getAccountNumber().getContent().get(0));

        if (userInfo.getCorporateEmailAddress().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-CorporateEmailAddress", userInfo.getCorporateEmailAddress().getContent().get(0));

        if (userInfo.getClientID1() != null && userInfo.getClientID1().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-ClientID1", userInfo.getClientID1().getContent().get(0));

        if (userInfo.getClientID2() != null && userInfo.getClientID2().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-ClientID2", userInfo.getClientID2().getContent().get(0));

        if (userInfo.getCompanyName() != null && userInfo.getCompanyName().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-CompanyName", userInfo.getCompanyName().getContent().get(0));

        if (userInfo.getEmailAddress() != null && userInfo.getEmailAddress().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-EmailAddress", userInfo.getEmailAddress().getContent().get(0));

        if (userInfo.getLoginName() != null && userInfo.getLoginName().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-LoginName", userInfo.getLoginName().getContent().get(0));

        if (userInfo.getUUID() != null && userInfo.getUUID().getContent().size() > 0)
            email.addHeader(headerPrefix + "-UserInfo-UUID", userInfo.getUUID().getContent().get(0));

    }


    private static String getEmail(User userInfo) {
        if (userInfo.getEmailAddress().getContent() != null && userInfo.getEmailAddress().getContent().size() != 0) {
            return userInfo.getEmailAddress().getContent().get(0);
        }

        if (userInfo.getCorporateEmailAddress().getContent() != null && userInfo.getCorporateEmailAddress().getContent().size() != 0) {
            return userInfo.getCorporateEmailAddress().getContent().get(0);
        }

        return StringUtils.EMPTY;

    }

    private static String getEmail(Inviter userInfo) {
        if (userInfo.getEmailAddress().getContent() != null && userInfo.getEmailAddress().getContent().size() != 0) {
            return userInfo.getEmailAddress().getContent().get(0);
        }

        if (userInfo.getCorporateEmailAddress().getContent() != null && userInfo.getCorporateEmailAddress().getContent().size() != 0) {
            return userInfo.getCorporateEmailAddress().getContent().get(0);
        }

        return StringUtils.EMPTY;
    }

    private static String getEmail(Invitee userInfo) {
        if (userInfo.getEmailAddress().getContent() != null && userInfo.getEmailAddress().getContent().size() != 0) {
            return userInfo.getEmailAddress().getContent().get(0);
        }

        if (userInfo.getCorporateEmailAddress().getContent() != null && userInfo.getCorporateEmailAddress().getContent().size() != 0) {
            return userInfo.getCorporateEmailAddress().getContent().get(0);
        }

        return StringUtils.EMPTY;
    }



    private static String getName(User userInfo) {
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

    private static String getName(Inviter userInfo) {
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

    private static String getName(Invitee userInfo) {
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

    private static InputStream getAttachmentAsStream(String attachmentsZip, String name) throws FileSystemException {
        String dateSuffix = attachmentsZip.replaceAll(".*\\.att\\.", "").replaceAll("\\.tar\\.gz", "");
        FileSystemManager fsManager = VFS.getManager();
        String pathToFile = "tgz:" + attachmentsZip + "!/bloomberg_attachments_" + dateSuffix + "/" + name;
        logger.info("Attempting to resolve " + pathToFile);
        FileSystemOptions opts = new FileSystemOptions();
        FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
        FileObject file = fsManager.resolveFile(pathToFile, opts);
        return file.getContent().getInputStream();
    }

}
