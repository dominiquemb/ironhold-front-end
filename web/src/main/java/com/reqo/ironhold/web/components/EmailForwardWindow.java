package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.*;
import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * User: ilya
 * Date: 4/17/13
 * Time: 8:15 AM
 */
public class EmailForwardWindow extends Window {
    private static Logger logger = Logger.getLogger(EmailForwardWindow.class);

    private final String id;
    private final String message;
    private final EmailForwardWindow
            window;


    private static String mailServer;

    static {
        try {
            Properties prop = new Properties();
            prop.load(EmailForwardWindow.class.getResourceAsStream("email.properties"));

            mailServer = prop.getProperty("mailserver");
        } catch (IOException e) {
            logger.warn("Failed to set email server", e);
            mailServer = "127.0.0.1";
        }

    }

    public EmailForwardWindow(final LoginUser loginUser, String id, final String message) {
        super("Forward message");
        this.id = id;
        this.message = message;
        this.window = this;
        setHeight("190px");
        setWidth("400px");
        setResizable(false);
        VerticalLayout layout = new VerticalLayout();
        this.setContent(layout);
        layout.setMargin(true);
        layout.setSpacing(true);
        final TextField address = new TextField("Addresses (separated by commas):");

        address.setWidth("300px");
        final CheckBox toMe = new CheckBox("To me");
        toMe.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Boolean checked = (Boolean) event.getProperty().getValue();
                if (checked) {
                    address.setValue(loginUser.getMainRecipient().getAddress());
                }
            }
        });

        address.addTextChangeListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {
                toMe.setValue(Boolean.FALSE);
            }
        });

        HorizontalLayout hl = new HorizontalLayout();
        Button forward = new Button("Forward");
        Button cancel = new Button("Cancel");
        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                window.close();
            }
        });

        forward.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (address.getValue().trim().length() > 0) {
                    try {
                        MimeMailMessage m = new MimeMailMessage();
                        m.loadMimeMessageFromSource(message);

                        HtmlEmail email = new HtmlEmail();
                        email.setHostName(mailServer);
                        email.addTo(address.getValue());
                        email.setFrom(loginUser.getMainRecipient().getAddress(), loginUser.getMainRecipient().getName());
                        email.setSubject("FW: " + m.getSubject());

                        // set the html message
                        email.setHtmlMsg(loginUser.getMainRecipient().getName() + " forwarded you attached message");


                        // set the alternative message
                        email.setTextMsg(loginUser.getMainRecipient().getName() + " forwarded you attached message");

                        email.attach(new ByteArrayDataSource(message.getBytes(), "message/rfc822"), m.getSubject() + ".eml", m.getSubject());

                        email.send();

                    } catch (Exception e) {
                        logger.warn(e);
                    }
                    window.close();
                }
            }
        });

        hl.addComponent(forward);
        hl.addComponent(cancel);
        hl.setSpacing(true);

        layout.addComponent(address);
        layout.addComponent(toMe);
        layout.addComponent(hl);

    }
}
