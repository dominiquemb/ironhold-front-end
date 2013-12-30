package com.reqo.ironhold.web.components;

import com.reqo.ironhold.web.domain.LoginUser;
import com.vaadin.server.ClassResource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.Reindeer;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * User: ilya
 * Date: 4/16/13
 * Time: 8:51 AM
 */
public class EmailToolBar extends Panel {
    private static Logger logger = Logger.getLogger(EmailToolBar.class);

    public EmailToolBar(final LoginUser loginUser, final String id, final String mailMessage) {
        HorizontalLayout layout = new HorizontalLayout();
        this.setContent(layout);
        this.setStyleName(Reindeer.PANEL_LIGHT);

        layout.setSpacing(true);
        //    layout.setMargin(true);
        final Link download = new Link("Download", new StreamResource(new StreamResource.StreamSource() {

            public InputStream getStream() {
                return new ByteArrayInputStream(mailMessage.getBytes());
            }
        }, id + ".eml"));
        download.setIcon(new ClassResource("images/download.png"));

    /*    final Button forward = new Button();
        forward.setCaption("Forward");
        forward.setIcon(new ClassResource("images/email.png"));
        forward.setStyleName(Reindeer.BUTTON_LINK);
        forward.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                logger.info("Forward message");
                EmailForwardWindow window;
                try {
                    window = new EmailForwardWindow(loginUser, id, mailMessage);
                    window.setModal(true);
                    getUI().addWindow(window);

                } catch (Exception e) {
                    logger.warn(e);
                }
            }
        });
      */

        layout.addComponent(download);
        // layout.addComponent(forward);
        this.setHeight("30px");
    }
}
