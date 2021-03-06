package com.reqo.ironhold.storage.model.message;

import com.gs.collections.impl.set.mutable.UnifiedSet;
import com.reqo.ironhold.storage.model.MessageSourceTestModel;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.utils.ChecksumUtils;
import com.reqo.ironhold.web.domain.Recipient;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("unchecked")
public class EmlLoadTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private IMAPMessageSource source = MessageSourceTestModel
            .generateIMAPMessageSource();
    private Session session;


    @Before
    public void setUp() {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.mime.base64.ignoreerrors", "true");
        props.setProperty("mail.imap.partialfetch", "false");
        props.setProperty("mail.imaps.partialfetch", "false");
        session = Session.getInstance(props, null);
    }

    @Test
    @Ignore
    public void testMissingAttachment() throws Exception {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testMissingAttachment.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
        Assert.assertEquals(mimeMessage.getSentDate(),
                mailMessage.getMessageDate());
        Assert.assertEquals(1, mailMessage.getAttachments().length);
    }

    @Test
    public void testGetRawMessage1() throws Exception {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testMimeMessageWithHTML.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);
        File messageFile = new File(tempFolder.getRoot() + File.separator
                + "testGetRawMessage1.eml");
        OutputStream fos = new FileOutputStream(messageFile);

        fos.write(getRawMessage(mimeMessage).getBytes());
        fos.flush();
        fos.close();

        List<String> orioginalLines = Files.readAllLines(
                Paths.get(file.toURI()), Charset.defaultCharset());
        StringBuilder original = new StringBuilder();
        for (String line : orioginalLines) {
            original.append(line + "\n");
        }

        List<String> messageLines = Files.readAllLines(
                Paths.get(messageFile.toURI()), Charset.defaultCharset());
        StringBuilder message = new StringBuilder();
        for (String line : messageLines) {
            message.append(line + "\n");
        }

        Assert.assertEquals(original.toString(), message.toString());

    }

    @Test
    public void testGetRawMessage2() throws Exception {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testMimeMessageWithHTMLandAttachment.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);
        File messageFile = new File(tempFolder.getRoot() + File.separator
                + "testGetRawMessage2.eml");
        OutputStream fos = new FileOutputStream(messageFile);

        fos.write(getRawMessage(mimeMessage).getBytes());
        fos.flush();
        fos.close();

        List<String> orioginalLines = Files.readAllLines(
                Paths.get(file.toURI()), Charset.defaultCharset());
        StringBuilder original = new StringBuilder();
        for (String line : orioginalLines) {
            original.append(line + "\n");
        }

        List<String> messageLines = Files.readAllLines(
                Paths.get(messageFile.toURI()), Charset.defaultCharset());
        StringBuilder message = new StringBuilder();
        for (String line : messageLines) {
            message.append(line + "\n");
        }

        Assert.assertEquals(original.toString(), message.toString());

    }

    @Test
    public void testGetRawMessage3() throws Exception {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testMimeMessageWithImage.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);
        File messageFile = new File(tempFolder.getRoot() + File.separator
                + "testGetRawMessage3.eml");
        OutputStream fos = new FileOutputStream(messageFile);

        fos.write(getRawMessage(mimeMessage).getBytes());
        fos.flush();
        fos.close();

        List<String> orioginalLines = Files.readAllLines(
                Paths.get(file.toURI()), Charset.defaultCharset());
        StringBuilder original = new StringBuilder();
        for (String line : orioginalLines) {
            original.append(line + "\n");
        }

        List<String> messageLines = Files.readAllLines(
                Paths.get(messageFile.toURI()), Charset.defaultCharset());
        StringBuilder message = new StringBuilder();
        for (String line : messageLines) {
            message.append(line + "\n");
        }

        Assert.assertEquals(original.toString(), message.toString());

    }

    @Test
    public void testMimeMessageWithHTML() throws Exception {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testMimeMessageWithHTML.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
        Assert.assertEquals(mimeMessage.getSentDate(),
                mailMessage.getMessageDate());

        Assert.assertEquals(
                ((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
                mailMessage.getFrom().getAddress());
        Assert.assertEquals(
                ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
                mailMessage.getFrom().getName());

        Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
                mailMessage.getTo().length);
        if (mimeMessage.getRecipients(RecipientType.CC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.CC).length,
                    mailMessage.getCc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getAddress(),
                        mailMessage.getCc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getPersonal(),
                        mailMessage.getCc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getCc().length);
        }

        if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.BCC).length,
                    mailMessage.getBcc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getAddress(),
                        mailMessage.getBcc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getPersonal(),
                        mailMessage.getBcc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getBcc().length);
        }

        MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

        Assert.assertEquals(contents.getBodyPart(0).getContent().toString(),
                mailMessage.getBody());

        Assert.assertEquals(contents.getBodyPart(0).getContentType(),
                mailMessage.getBodyContentType());

        Assert.assertEquals(contents.getBodyPart(1).getContent().toString(),
                mailMessage.getBodyHTML());

        Assert.assertEquals(contents.getBodyPart(1).getContentType(),
                mailMessage.getBodyHTMLContentType());

        Assert.assertEquals(mimeMessage.getMessageID(),
                mailMessage.getMessageId());
    }

    @Test
    public void testMimeMessageWithHTMLFromString() throws Exception {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testMimeMessageWithHTML.eml"));
        InputStream is = new FileInputStream(file);

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        InputStream is2 = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(null, is2);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessageFromSource(sb.toString());

        Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
        Assert.assertEquals(mimeMessage.getSentDate(),
                mailMessage.getMessageDate());

        Assert.assertEquals(
                ((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
                mailMessage.getFrom().getAddress());
        Assert.assertEquals(
                ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
                mailMessage.getFrom().getName());

        Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
                mailMessage.getTo().length);
        if (mimeMessage.getRecipients(RecipientType.CC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.CC).length,
                    mailMessage.getCc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getAddress(),
                        mailMessage.getCc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getPersonal(),
                        mailMessage.getCc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getCc().length);
        }

        if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.BCC).length,
                    mailMessage.getBcc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getAddress(),
                        mailMessage.getBcc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getPersonal(),
                        mailMessage.getBcc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getBcc().length);
        }

        MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

        Assert.assertEquals(contents.getBodyPart(0).getContent().toString(),
                mailMessage.getBody());

        Assert.assertEquals(contents.getBodyPart(0).getContentType(),
                mailMessage.getBodyContentType());

        Assert.assertEquals(contents.getBodyPart(1).getContent().toString(),
                mailMessage.getBodyHTML());

        Assert.assertEquals(contents.getBodyPart(1).getContentType(),
                mailMessage.getBodyHTMLContentType());

        Assert.assertEquals(mimeMessage.getMessageID(),
                mailMessage.getMessageId());
    }

    private String getRawMessage(MimeMessage mimeMessage)
            throws MessagingException, IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        List<String> lines = Collections.list(mimeMessage.getAllHeaderLines());
        for (String line : lines) {
            os.write((line + "\n").getBytes());
        }
        os.write("\n".getBytes());
        InputStream rawStream = mimeMessage.getRawInputStream();
        int read = 0;
        byte[] bytes = new byte[1024];

        while ((read = rawStream.read(bytes)) != -1) {
            os.write(bytes, 0, read);
        }
        rawStream.close();

        return os.toString();
    }

    @Test
    public void testMimeMessageWithHTMLandAttachment() throws Exception {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testMimeMessageWithHTMLandAttachment.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);


        Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
        Assert.assertEquals(mimeMessage.getSentDate(),
                mailMessage.getMessageDate());

        Assert.assertEquals(
                ((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
                mailMessage.getFrom().getAddress());
        Assert.assertEquals(
                ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
                mailMessage.getFrom().getName());

        Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
                mailMessage.getTo().length);
        if (mimeMessage.getRecipients(RecipientType.CC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.CC).length,
                    mailMessage.getCc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getAddress(),
                        mailMessage.getCc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getPersonal(),
                        mailMessage.getCc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getCc().length);
        }

        if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.BCC).length,
                    mailMessage.getBcc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getAddress(),
                        mailMessage.getBcc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getPersonal(),
                        mailMessage.getBcc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getBcc().length);
        }

        MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

        Multipart part1 = (Multipart) contents.getBodyPart(0).getContent();

        Assert.assertEquals(part1.getBodyPart(0).getContent().toString(),
                mailMessage.getBody());

        Assert.assertEquals(part1.getBodyPart(0).getContentType(),
                mailMessage.getBodyContentType());

        Assert.assertEquals(part1.getBodyPart(1).getContent().toString(),
                mailMessage.getBodyHTML());

        Assert.assertEquals(part1.getBodyPart(1).getContentType(),
                mailMessage.getBodyHTMLContentType());

        InputStream part2 = (InputStream) contents.getBodyPart(1).getContent();
        Assert.assertEquals(contents.getBodyPart(1).getFileName(),
                mailMessage.getAttachments()[0].getFileName());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int bytesRead;
        while ((bytesRead = part2.read(buf)) != -1) {
            out.write(buf, 0, bytesRead);
        }
        out.close();

        Assert.assertEquals(Base64.encodeBase64String(out.toByteArray()),
                mailMessage.getAttachments()[0].getBody());

        File attachment = FileUtils.toFile(EmlLoadTest.class.getResource("/"
                + mailMessage.getAttachments()[0].getFileName()));

        String md5fromFile = ChecksumUtils.getMD5Checksum(attachment);
        OutputStream fos = new FileOutputStream(tempFolder.getRoot()
                + File.separator
                + mailMessage.getAttachments()[0].getFileName());

        InputStream inputStream = new ByteArrayInputStream(
                Base64.decodeBase64(mailMessage.getAttachments()[0].getBody()
                        .getBytes()));

        int read = 0;
        int total = read;
        byte[] bytes = new byte[1024];

        while ((read = inputStream.read(bytes)) != -1) {

            total += read;
            fos.write(bytes, 0, read);
        }
        inputStream.close();
        fos.flush();
        fos.close();

        String md5fromAttachment = ChecksumUtils.getMD5Checksum(new File(tempFolder.getRoot()
                + File.separator
                + mailMessage.getAttachments()[0].getFileName()));

        Assert.assertEquals(md5fromFile, md5fromAttachment);

        Assert.assertEquals(mimeMessage.getMessageID(),
                mailMessage.getMessageId());
    }

    @Test
    public void testMimeMessageWithImage() throws Exception {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testMimeMessageWithImage.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
        Assert.assertEquals(mimeMessage.getSentDate(),
                mailMessage.getMessageDate());

        Assert.assertEquals(
                ((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
                mailMessage.getFrom().getAddress());
        Assert.assertEquals(
                ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
                mailMessage.getFrom().getName());

        Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
                mailMessage.getTo().length);
        if (mimeMessage.getRecipients(RecipientType.CC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.CC).length,
                    mailMessage.getCc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getAddress(),
                        mailMessage.getCc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getPersonal(),
                        mailMessage.getCc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getCc().length);
        }

        if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.BCC).length,
                    mailMessage.getBcc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getAddress(),
                        mailMessage.getBcc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getPersonal(),
                        mailMessage.getBcc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getBcc().length);
        }

        MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

        Multipart part1 = (Multipart) contents.getBodyPart(0).getContent();

        Assert.assertEquals(part1.getBodyPart(0).getContent().toString(),
                mailMessage.getBody());

        Assert.assertEquals(part1.getBodyPart(0).getContentType(),
                mailMessage.getBodyContentType());

        Assert.assertEquals(part1.getBodyPart(1).getContent().toString(),
                mailMessage.getBodyHTML());

        Assert.assertEquals(part1.getBodyPart(1).getContentType(),
                mailMessage.getBodyHTMLContentType());

        InputStream part2 = (InputStream) contents.getBodyPart(1).getContent();
        Assert.assertEquals(contents.getBodyPart(1).getFileName(),
                mailMessage.getAttachments()[0].getFileName());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int bytesRead;
        while ((bytesRead = part2.read(buf)) != -1) {
            out.write(buf, 0, bytesRead);
        }
        out.close();

        Assert.assertEquals(Base64.encodeBase64String(out.toByteArray()),
                mailMessage.getAttachments()[0].getBody());

        File attachment = FileUtils.toFile(EmlLoadTest.class.getResource("/"
                + mailMessage.getAttachments()[0].getFileName()));

        String md5fromFile = ChecksumUtils.getMD5Checksum(attachment);
        OutputStream fos = new FileOutputStream(tempFolder.getRoot()
                + File.separator
                + mailMessage.getAttachments()[0].getFileName());

        InputStream inputStream = new ByteArrayInputStream(
                Base64.decodeBase64(mailMessage.getAttachments()[0].getBody()
                        .getBytes()));

        int read = 0;
        int total = read;
        byte[] bytes = new byte[1024];

        while ((read = inputStream.read(bytes)) != -1) {

            total += read;
            fos.write(bytes, 0, read);
        }
        inputStream.close();
        fos.flush();
        fos.close();

        String md5fromAttachment = ChecksumUtils.getMD5Checksum(new File(tempFolder.getRoot()
                + File.separator
                + mailMessage.getAttachments()[0].getFileName()));

        Assert.assertEquals(md5fromFile, md5fromAttachment);

        Assert.assertEquals(mimeMessage.getMessageID(),
                mailMessage.getMessageId());
    }

    @Test
    public void testJournalMimeMessage() throws Exception {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testJournalMimeMessage.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
        Assert.assertEquals(mimeMessage.getSentDate(),
                mailMessage.getMessageDate());

        Assert.assertEquals(
                ((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
                mailMessage.getFrom().getAddress());
        Assert.assertEquals(
                ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
                mailMessage.getFrom().getName());

        Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
                mailMessage.getTo().length);
        if (mimeMessage.getRecipients(RecipientType.CC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.CC).length,
                    mailMessage.getCc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getAddress(),
                        mailMessage.getCc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getPersonal(),
                        mailMessage.getCc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getCc().length);
        }

        if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.BCC).length,
                    mailMessage.getBcc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getAddress(),
                        mailMessage.getBcc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getPersonal(),
                        mailMessage.getBcc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getBcc().length);
        }

        MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

        MimeMessage internalMessage = (MimeMessage) contents.getBodyPart(0)
                .getContent();

        String internalContents = (String) internalMessage.getContent();

        Assert.assertEquals(internalContents, mailMessage.getBodyHTML());

        Assert.assertEquals(mimeMessage.getMessageID(),
                mailMessage.getMessageId());
    }

    @Test
    public void testInvalidAddress() throws Exception {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testInvalidAddress.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
        Assert.assertEquals(mimeMessage.getSentDate(),
                mailMessage.getMessageDate());

        Assert.assertEquals(
                ((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
                mailMessage.getFrom().getAddress());
        Assert.assertEquals(
                ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
                mailMessage.getFrom().getName());

        Assert.assertEquals(1, mailMessage.getTo().length);
        if (mimeMessage.getRecipients(RecipientType.CC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.CC).length,
                    mailMessage.getCc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getAddress(),
                        mailMessage.getCc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getPersonal(),
                        mailMessage.getCc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getCc().length);
        }

        if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.BCC).length,
                    mailMessage.getBcc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getAddress(),
                        mailMessage.getBcc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getPersonal(),
                        mailMessage.getBcc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getBcc().length);
        }

        MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

        MimeMessage internalMessage = (MimeMessage) contents.getBodyPart(1)
                .getContent();

        String internalContents = (String) internalMessage.getContent();

        Assert.assertEquals(internalContents, mailMessage.getBodyHTML());

        Assert.assertEquals(mimeMessage.getMessageID(),
                mailMessage.getMessageId());
    }

    @Test
    public void testMimeMessageWithOnBehalf() throws Exception {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testMimeMessageWithOnBehalf.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
        Assert.assertEquals(mimeMessage.getSentDate(),
                mailMessage.getMessageDate());


        Assert.assertEquals(
                ((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
                mailMessage.getFrom().getAddress());
        Assert.assertEquals(((InternetAddress) mimeMessage.getFrom()[0])
                .getPersonal() == null ? Recipient.getNameFromAddress(((InternetAddress) mimeMessage.getFrom()[0]).getAddress())
                : ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
                mailMessage.getFrom().getName());

        Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
                mailMessage.getTo().length);
        if (mimeMessage.getRecipients(RecipientType.CC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.CC).length,
                    mailMessage.getCc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getAddress(),
                        mailMessage.getCc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getPersonal(),
                        mailMessage.getCc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getCc().length);
        }

        if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.BCC).length,
                    mailMessage.getBcc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getAddress(),
                        mailMessage.getBcc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getPersonal(),
                        mailMessage.getBcc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getBcc().length);
        }

        MimeMultipart contents = (MimeMultipart) mimeMessage.getContent();

        MimeMessage embeddedMessage = (MimeMessage) contents.getBodyPart(1)
                .getContent();
        Assert.assertEquals(((MimeMultipart) embeddedMessage.getContent())
                .getBodyPart(1).getContent().toString(),
                mailMessage.getBodyHTML());

        Assert.assertEquals(mimeMessage.getMessageID(),
                mailMessage.getMessageId());
    }


    @Test
    public void testLowImportance() throws Exception {
        MimeMailMessage mailMessage = performBasicCheckout("/testLowImportance.eml");
        Assert.assertEquals(MimeMailMessage.IMPORTANCE_LOW, mailMessage.getImportance());
    }

    @Test
    public void testHighImportance() throws Exception {
        MimeMailMessage mailMessage = performBasicCheckout("/testHighImportance.eml");
        Assert.assertEquals(MimeMailMessage.IMPORTANCE_HIGH, mailMessage.getImportance());
    }

    @Test
    public void testMimeMessageBig() throws Exception {
        performBasicCheckout("/testMimeMessageBig.eml");
    }

    @Test
    public void testHeaders() throws Exception {
        MimeMailMessage message = performBasicCheckout("/testMimeMessageBig.eml");

        Assert.assertEquals(13, message.getHeaders().size());
        List<String> expected = UnifiedSet.newSetWith("MIME-Version", "Content-Type", "Subject", "To", "From", "Sender", "Message-ID", "Date", "X-MS-Journal-Report", "X-MS-Exchange-Organization-AuthSource", "X-MS-Exchange-Organization-AuthAs", "X-MS-Exchange-Organization-AuthMechanism", "Keywords").toSortedList();
        List<String> actual = UnifiedSet.newSet(message.getHeaders().keySet()).toSortedList();
        Assert.assertEquals(expected, actual);

    }

    @Test
    public void testUnsupportedEncodingException3D() throws Exception {
        performBasicCheckout("/testUnsupportedEncodingException3D.eml");
    }

    @Test
    public void testUnknownEncoding8dashBit() throws Exception {
        performBasicCheckout("/testUnknownEncoding8dashBit.eml");
    }

    @Test
    public void testUnknownEncodingQuote() throws Exception {
        performBasicCheckout("/testUnknownEncodingQuote.eml");
    }

    @Test
    public void testNullImage() throws Exception {
        performBasicCheckout("/testNullImage.eml");
    }

    @Test
    public void testBlankContent() throws Exception {
        performBasicCheckout("/testBlankContent.eml");
    }

    @Test
    public void testUnknownRecipient() throws Exception {
        performBasicCheckout("/testUnknownRecipient.eml");
    }


    @Test
    public void testNoDate() throws Exception {
        performBasicCheckout("/testNoDate.eml");
    }

    @Test
    public void testInfinite() throws Exception {
        performBasicCheckout("/testInfinite.eml");
    }


    @Test
    public void testCP1251() throws Exception {
        performBasicCheckout("/testCP-1251.eml");
    }

    @Test
    public void testUnknownEncoding() throws Exception {
        MimeMailMessage message = performBasicCheckout("/testUnknownEncoding.eml");
        System.out.println(message.getRawContents());
    }


    @Test
    public void testFromBadAddress() throws Exception {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testFromBadAddress.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        Assert.assertEquals("Paul Caruso", mailMessage.getFrom().getName());
    }

    @Test
    public void testProblematicAttachment() throws Exception {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource("/testProblematicAttachment.eml"));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
        Assert.assertEquals(mimeMessage.getSentDate(),
                mailMessage.getMessageDate());

        Assert.assertEquals(1, mailMessage.getAttachments().length);
        Assert.assertEquals("1 DNC.pdf", mailMessage.getAttachments()[0].getFileName());


    }

    private MimeMailMessage performBasicCheckout(String fileName) throws MessagingException, IOException {
        File file = FileUtils.toFile(EmlLoadTest.class
                .getResource(fileName));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(session, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);

        Assert.assertEquals(mimeMessage.getSubject(), mailMessage.getSubject());
        Assert.assertEquals(mimeMessage.getSentDate(),
                mailMessage.getMessageDate());

        if (mimeMessage.getFrom() != null) {
            Assert.assertEquals(
                    ((InternetAddress) mimeMessage.getFrom()[0]).getAddress(),
                    mailMessage.getFrom().getAddress());
            Assert.assertEquals(((InternetAddress) mimeMessage.getFrom()[0])
                    .getPersonal() == null ? Recipient.getNameFromAddress(((InternetAddress) mimeMessage.getFrom()[0]).getAddress())
                    : ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal(),
                    mailMessage.getFrom().getName());

        } else {
            Assert.assertEquals("unknown",
                    mailMessage.getFrom().getAddress());
            Assert.assertEquals("unknown",
                    mailMessage.getFrom().getName());
        }




        Assert.assertEquals(mimeMessage.getRecipients(RecipientType.TO).length,
                mailMessage.getTo().length);
        if (mimeMessage.getRecipients(RecipientType.CC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.CC).length,
                    mailMessage.getCc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.CC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getAddress(),
                        mailMessage.getCc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getPersonal() == null ? Recipient.getNameFromAddress(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getAddress()) : ((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.CC)[i]).getPersonal(),
                        mailMessage.getCc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getCc().length);
        }

        if (mimeMessage.getRecipients(RecipientType.BCC) != null) {

            Assert.assertEquals(
                    mimeMessage.getRecipients(RecipientType.BCC).length,
                    mailMessage.getBcc().length);

            for (int i = 0; i < mimeMessage.getRecipients(RecipientType.BCC).length; i++) {
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getAddress(),
                        mailMessage.getBcc()[i].getAddress());
                Assert.assertEquals(((InternetAddress) mimeMessage
                        .getRecipients(RecipientType.BCC)[i]).getPersonal(),
                        mailMessage.getBcc()[i].getName());
            }
        } else {
            Assert.assertEquals(0, mailMessage.getBcc().length);
        }

        Assert.assertNotNull(mailMessage.getPartition());
        Assert.assertNotNull(mailMessage.getSubPartition());
        return mailMessage;
    }
}
