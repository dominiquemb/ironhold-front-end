package com.reqo.ironhold.reader.notification;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class EmailNotification {
    private static Logger logger = Logger.getLogger(EmailNotification.class);


    private static boolean enabled = true;
    private static String hostname = null;

    private static String mailServer;

    static {
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Setting hostname to unknown", e);
            hostname = "unknown";
        }

        try {
            Properties prop = new Properties();
            prop.load(EmailNotification.class.getResourceAsStream("email.properties"));

            mailServer = prop.getProperty("mailserver");
        } catch (IOException e) {
            logger.warn("Failed to set email server", e);
            mailServer = "127.0.0.1";
        }

    }

    public static void sendSystemNotification(String subject, String body) {
        if (enabled) {
            // sendSystemNotification the email
            try {

                HtmlEmail email = new HtmlEmail();
                email.setHostName(mailServer);
                email.addTo("admins@ironhold.net", "admins@ironhold.net");
                email.setFrom("admins@ironhold.net", hostname);
                email.setSubject(subject);

                // set the html message
                email.setHtmlMsg(body);

                // set the alternative message
                email.setTextMsg("Your email client does not support HTML messages");

                email.send();

                logger.info(String.format("Sending notification:\n%s\n%s\n", subject, body));
            } catch (EmailException e) {
                logger.warn(e);
            }
        } else {
            logger.info(String.format("Sending notification:\n%s\n%s\n", subject, body));
            logger.info("Notification has been disabled");
        }
    }

    public static void disableNotification() {
        enabled = false;
    }
}
