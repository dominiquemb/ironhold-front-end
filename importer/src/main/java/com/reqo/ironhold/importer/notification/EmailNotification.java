package com.reqo.ironhold.importer.notification;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;

public class EmailNotification {
	private static Logger logger = Logger.getLogger(EmailNotification.class);

	
	private static boolean enabled = true;
	private static String hostname = null;

	static {
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			hostname = "unknown";
		}
	}

	public static void send(String subject, String body) {
		if (enabled) {
			// send the email
			try {

				HtmlEmail email = new HtmlEmail();
				email.setHostName("10.65.0.78");
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
				// TODO Auto-generated catch block
				e.printStackTrace();
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
