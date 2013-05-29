package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.vaadin.event.MouseEvents;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;

/**
 * User: ilya
 * Date: 5/22/13
 * Time: 9:11 PM
 */
public class LoginUserPanel extends Panel {
    private static Logger logger = Logger.getLogger(LoginUserPanel.class);

    private final VerticalLayout layout;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM dd, YYYY HH:mm");
    private final LoginUserPanel me;

    public LoginUserPanel(final UserManagementWindow window, final LoginUser loginUser, final MiscIndexService miscIndexService) {
        setWidth("590px");
        this.me = this;
        layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();
        this.setContent(layout);
        setStyleName(Reindeer.PANEL_LIGHT);

        layout.addComponent(renderKeyValuePair("Name:", loginUser.getName()));
        layout.addComponent(renderKeyValuePair("Username:", loginUser.getUsername()));
        layout.addComponent(renderKeyValuePair("Email:", loginUser.getMainRecipient().getAddress()));
        layout.addComponent(renderKeyValuePair("Last Login:", loginUser.getLastLogin() == null ? "Never" : sdf.format(loginUser.getLastLogin())));

        this.addClickListener(new MouseEvents.ClickListener() {

            public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
                try {
                    window.setViewMode(me, loginUser);
                } catch (Exception e) {
                    logger.warn(e);
                }
            }
        });
    }


    private Component renderKeyValuePair(String caption, String value) {
        HorizontalLayout hl = new HorizontalLayout();
        final Label captionLabel = new Label(caption);
        captionLabel.setWidth("100px");
        hl.addComponent(captionLabel);
        final Label valueLabel = new Label(value);
        valueLabel.setContentMode(ContentMode.TEXT);
        valueLabel.setWidth("540px");
        hl.addComponent(valueLabel);
        hl.setExpandRatio(valueLabel, 1.0f);
        return hl;
    }
}

