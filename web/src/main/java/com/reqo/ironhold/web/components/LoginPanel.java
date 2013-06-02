package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.IronholdApplication;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;

/**
 * User: ilya
 * Date: 4/12/13
 * Time: 2:25 PM
 */
public class LoginPanel extends Panel {
    private final VerticalLayout layout;
    private final TextField username;
    private final PasswordField password;
    private final TextField client;
    private final CheckBox rememberUsername;
    private final CheckBox rememberClient;
    private final Button login;
    private MiscIndexService miscIndexService;

    public LoginPanel() {
        this.setStyleName(Reindeer.PANEL_LIGHT);
        this.setSizeUndefined();

        this.layout = new VerticalLayout();
        this.setContent(layout);


        layout.setSizeUndefined();

        username = new TextField("Username:");
        password = new PasswordField("Password:");
        client = new TextField("Client Key:");

        setUpValidation(username);
        setUpValidation(password);
        setUpValidation(client);

        login = new Button("Login");
        rememberUsername = new CheckBox("Remember username");
        rememberClient = new CheckBox("Remember client key");
        rememberUsername.setValue(Boolean.TRUE);
        rememberClient.setValue(Boolean.TRUE);
        layout.addComponent(username);
        layout.addComponent(password);
        layout.addComponent(client);
        layout.addComponent(rememberUsername);
        layout.addComponent(rememberClient);

        layout.addComponent(login);

    }

    private void setUpValidation(AbstractTextField textField) {
        textField.setNullRepresentation("");
        textField.addValidator(new NullValidator("Required field", false));
        textField.addValidator(new StringLengthValidator(
                "Required field must be greater than 3 characters", 3, 100, true));
        textField.setValidationVisible(false);
    }

    public void init(final IronholdApplication ironholdApplication) {
        miscIndexService = ironholdApplication.getMiscIndexService();

        login.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                attemptAuthentication(ironholdApplication);
            }
        });

        client.addShortcutListener(new DefaultShortcutListener(ironholdApplication));
        username.addShortcutListener(new DefaultShortcutListener(ironholdApplication));
        password.addShortcutListener(new DefaultShortcutListener(ironholdApplication));

        client.setValue(getDefaultClient());
        username.setValue(getCookie("username"));
        if (username.getValue().equals(StringUtils.EMPTY)) {
            username.focus();
        } else {
            password.focus();
        }
    }

    private String getDefaultClient() {
        String url = this.getUI().getPage().getLocation().toString();
        if (url.contains("//")) {
            url = url.split("//")[1];
        }
        if (url.contains(".ironhold.net")) {
            url = url.split(".ironhold.net")[0];
            return url;
        }
        return getCookie("client");
    }

    private String getCookie(String name) {
        for (Cookie cookie : VaadinService.getCurrentRequest().getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }

        return StringUtils.EMPTY;
    }

    private void attemptAuthentication(IronholdApplication ironholdApplication) {

        try {
            username.setValidationVisible(true);
            password.setValidationVisible(true);
            client.setValidationVisible(true);

            username.validate();
            password.validate();
            client.validate();

            LoginUser authenticatedUser = miscIndexService.authenticate(client.getValue(), username.getValue(), password.getValue());
            if (authenticatedUser == null) {
                Notification.show("Invalid credentials", Notification.Type.ERROR_MESSAGE);
            } else {
                getSession().setAttribute("loginUser", authenticatedUser);
                getSession().setAttribute("client", client.getValue());

                addCookie("client", rememberClient.getValue() ? client.getValue() : null);
                addCookie("username", rememberUsername.getValue() ? username.getValue() : null);

                ironholdApplication.getSearchWindow().login();
            }
        } catch (Validator.InvalidValueException e) {
            Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
        } catch (Exception e) {
            Notification.show("Invalid credentials", Notification.Type.ERROR_MESSAGE);
        }

    }

    private void addCookie(String name, String value) {
        Cookie clientCookie = new Cookie(name, value);
        if (value != null) {
            clientCookie.setMaxAge(24 * 30 * 3600); // 30 days
        } else {
            clientCookie.setMaxAge(0); // delete

        }
        clientCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
        VaadinService.getCurrentResponse().addCookie(clientCookie);

    }


    class DefaultShortcutListener extends ShortcutListener {
        private final IronholdApplication ironholdApplication;

        public DefaultShortcutListener(IronholdApplication ironholdApplication) {
            super("Default key", ShortcutAction.KeyCode.ENTER, null);
            this.ironholdApplication = ironholdApplication;
        }

        @Override
        public void handleAction(Object sender, Object target) {
            attemptAuthentication(ironholdApplication);
        }
    }
}
