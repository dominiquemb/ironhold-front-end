package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.message.Recipient;
import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import com.reqo.ironhold.web.components.validators.EmailListValidator;
import com.reqo.ironhold.web.components.validators.PasswordValidator;
import com.reqo.ironhold.web.components.validators.UniqueUsernameValidator;
import com.reqo.ironhold.web.components.validators.UsernameStringValidator;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * User: ilya
 * Date: 5/23/13
 * Time: 7:37 AM
 */
public class LoginUserEditor extends Panel {
    private static final String TIMESTAMP = "Date";
    private static final String CRITERIA = "Criteria";

    private final VerticalLayout layout;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM dd, YYYY HH:mm");
    private final MiscIndexService miscIndexService;

    private final TextField name;
    private final TextField username;
    private final TextField email;

    private final Button save;
    private final Button cancel;
    private final CheckBox enabled;
    private final CheckBox superUser;
    private final String client;
    private final MetaDataIndexService metaDataIndexService;
    private final PasswordField password;
    private final PasswordField confirmPassword;
    private final TwinColSelect pstSources;
    private LoginUser loginUser;
    private final Map<RoleEnum, CheckBox> roleToggles;
    private final TextArea emailAddresses;
    private UniqueUsernameValidator uniqueUsernameValidator;
    private Validator passwordValidator;

    public LoginUserEditor(final UserManagementWindow window, final String client, final MiscIndexService miscIndexService, MetaDataIndexService metaDataIndexService) {
        this.client = client;
        this.miscIndexService = miscIndexService;
        this.metaDataIndexService = metaDataIndexService;
        setStyleName(Reindeer.PANEL_LIGHT);
        layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();
        layout.setSpacing(true);
        this.setContent(layout);
        setStyleName(Reindeer.PANEL_LIGHT);

        roleToggles = new HashMap<>();

        name = new TextField();
        name.addValidator(new StringLengthValidator("Required field", 3, 150, false));
        addLabelComponentPair("Name:", name);

        email = new TextField();
        email.addValidator(new EmailValidator("Must be valid email address"));
        email.addValidator(new StringLengthValidator("Required field", 3, 150, false));
        addLabelComponentPair("Email:", email);

        username = new TextField();
        uniqueUsernameValidator = new UniqueUsernameValidator(loginUser, client, miscIndexService);
        username.addValidator(uniqueUsernameValidator);
        username.addValidator(new UsernameStringValidator());
        username.addValidator(new StringLengthValidator("Required field", 3, 150, false));
        addLabelComponentPair("Username:", username);

        password = new PasswordField();
        password.setInputPrompt("reset password");
        addLabelComponentPair("Password:", password);

        passwordValidator = new StringLengthValidator("Required field", 3, 150, false);
        confirmPassword = new PasswordField();
        confirmPassword.setInputPrompt("reset password");

        addLabelComponentPair("Confirm Password:", confirmPassword);

        password.addValidator(new PasswordValidator(confirmPassword));

        enabled = new CheckBox();
        enabled.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                updateVisibility();

            }
        });
        addLabelComponentPair("Enabled:", enabled);

        superUser = new CheckBox();
        superUser.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                updateVisibility();

            }
        });
        addLabelComponentPair("Administrator:", superUser);

        for (RoleEnum role : RoleEnum.values()) {
            if (role != RoleEnum.SUPER_USER && role != RoleEnum.NONE) {
                CheckBox roleCheckbox = new CheckBox(role.toString());
                roleToggles.put(role, roleCheckbox);

                layout.addComponent(roleCheckbox);
            }
        }

        emailAddresses = new TextArea();
        emailAddresses.setRows(5);
        emailAddresses.addValidator(new EmailListValidator("Contains invalid email address"));
        addLabelComponentPair("Other Email Addresses:", emailAddresses);

        pstSources = new TwinColSelect();
        pstSources.setRows(6);
        pstSources.setNullSelectionAllowed(true);
        pstSources.setMultiSelect(true);
        pstSources.setImmediate(true);
        pstSources.setLeftColumnCaption("Available PSTs");
        pstSources.setRightColumnCaption("Assigned PSTSs");
        addLabelComponentPair("View messages from PSTs", pstSources);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        save = new Button("Save");
        save.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    if (name.isValid() && email.isValid() && username.isValid() && password.isValid() && confirmPassword.isValid() && emailAddresses.isValid()) {
                        if (loginUser == null) {
                            loginUser = new LoginUser();
                            loginUser.setMainRecipient(new Recipient(name.getValue(), email.getValue()));
                            loginUser.setName(name.getValue());
                            loginUser.setUsername(username.getValue());
                            int role = 0;
                            if (enabled.getValue()) {
                                if (superUser.getValue()) {
                                    role = RoleEnum.SUPER_USER.getValue();
                                } else {
                                    for (RoleEnum roleEnum : roleToggles.keySet()) {
                                        if (roleToggles.get(roleEnum).getValue()) {
                                            role += roleEnum.getValue();
                                        }
                                    }
                                }
                            }
                            loginUser.setRolesBitMask(role);
                            loginUser.setCreated(new Date());
                            loginUser.setHashedPassword(CheckSumHelper.getCheckSum(password.getValue().getBytes()));
                            List<Recipient> recipientList = new ArrayList<>();
                            for (String email : emailAddresses.getValue().trim().split("\n")) {
                                if (!email.trim().isEmpty()) {
                                    recipientList.add(new Recipient(name.getValue(), email.trim()));
                                }
                            }
                            loginUser.setRecipients(recipientList);

                            Collection<String> values = (Collection<String>) pstSources.getValue();
                            loginUser.setSources(values.toArray(new String[]{}));

                            loginUser.setId(UUID.randomUUID().toString());

                            miscIndexService.store(client, loginUser);
                            window.setViewMode(null, loginUser);
                        } else {
                            loginUser.setMainRecipient(new Recipient(name.getValue(), email.getValue()));
                            loginUser.setName(name.getValue());
                            loginUser.setUsername(username.getValue());
                            int role = 0;
                            if (enabled.getValue()) {
                                if (superUser.getValue()) {
                                    role = RoleEnum.SUPER_USER.getValue();
                                } else {
                                    for (RoleEnum roleEnum : roleToggles.keySet()) {
                                        if (roleToggles.get(roleEnum).getValue()) {
                                            role += roleEnum.getValue();
                                        }
                                    }
                                }
                            }
                            loginUser.setRolesBitMask(role);
                            if (!password.getValue().isEmpty()) {
                                loginUser.setHashedPassword(CheckSumHelper.getCheckSum(password.getValue().getBytes()));
                            }
                            List<Recipient> recipientList = new ArrayList<>();
                            for (String email : emailAddresses.getValue().trim().split("\n")) {
                                if (!email.trim().isEmpty()) {
                                    recipientList.add(new Recipient(name.getValue(), email.trim()));
                                }
                            }
                            loginUser.setRecipients(recipientList);

                            Collection<String> values = (Collection<String>) pstSources.getValue();
                            loginUser.setSources(values.toArray(new String[]{}));
                            miscIndexService.store(client, loginUser);
                            window.setViewMode(null, loginUser);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
        cancel = new Button("Cancel");

        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    window.setViewMode(null, loginUser);
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });

        buttonLayout.setSpacing(true);
        buttonLayout.addComponent(save);
        buttonLayout.addComponent(cancel);

        layout.addComponent(buttonLayout);
        layout.setVisible(false);
    }

    private void updateVisibility() {

        superUser.getParent().setVisible(enabled.getValue());
        for (CheckBox roleCheckbox : roleToggles.values()) {
            roleCheckbox.setVisible(enabled.getValue() && !superUser.getValue());
        }
        pstSources.getParent().setVisible(!superUser.getValue());

    }

    private void addLabelComponentPair(String labelText, Component component) {
        HorizontalLayout hl = new HorizontalLayout();
        Label label = new Label(labelText);
        label.setWidth("200px");
        component.setWidth("300px");
        hl.addComponent(label);
        hl.addComponent(component);
        hl.setSpacing(true);
        layout.addComponent(hl);
    }

    public void setLoginUser(LoginUser loginUser) throws Exception {
        this.loginUser = loginUser;
        password.setValue("");
        confirmPassword.setValue("");
        this.uniqueUsernameValidator.setLoginUser(loginUser);

        if (loginUser == null) {
            newUserMode();
        } else {
            editUserMode();
        }
        loadPsts();
        layout.setVisible(true);
    }

    private void loadPsts() throws Exception {
        pstSources.removeAllItems();

        int start = 0;
        int limit = 100;
        List<PSTFileMeta> sources;
        do {
            sources = miscIndexService.getPSTFileMetas(client, start, limit);
            start += sources.size();
            for (PSTFileMeta source : sources) {
                pstSources.addItem(source.getId());

                pstSources.setItemCaption(source.getId(), FilenameUtils.getBaseName(source.getPstFileName()));

                if (loginUser != null && loginUser.hasSource(source.getId())) {
                    pstSources.select(source.getId());
                }
            }
        } while (sources.size() == limit - 1);
    }

    private void newUserMode() {
        setCaption("Add New User");
        if (!password.getValidators().contains(passwordValidator)) {
            password.addValidator(passwordValidator);
        }
        if (!confirmPassword.getValidators().contains(passwordValidator)) {
            confirmPassword.addValidator(passwordValidator);
        }

        name.setValue("");
        email.setValue("");
        username.setValue("");

        enabled.setValue(true);
        superUser.setValue(false);
        for (RoleEnum role : roleToggles.keySet()) {
            if (role == RoleEnum.CAN_LOGIN || role == RoleEnum.CAN_SEARCH) {
                roleToggles.get(role).setValue(true);
            } else {
                roleToggles.get(role).setValue(false);
            }
        }
        updateVisibility();

        emailAddresses.setValue("");

    }

    private void editUserMode() throws InterruptedException, ExecutionException, IOException {
        setCaption("Edit " + loginUser.getName());
        if (password.getValidators().contains(passwordValidator)) {
            password.removeValidator(passwordValidator);
        }
        if (confirmPassword.getValidators().contains(passwordValidator)) {
            confirmPassword.removeValidator(passwordValidator);
        }

        name.setValue(loginUser.getName());
        email.setValue(loginUser.getMainRecipient().getAddress());
        username.setValue(loginUser.getUsername());

        enabled.setValue(loginUser.getRolesBitMask() != RoleEnum.NONE.getValue());
        superUser.setValue(loginUser.hasRole(RoleEnum.SUPER_USER));
        for (RoleEnum role : roleToggles.keySet()) {
            roleToggles.get(role).setValue(loginUser.hasRole(role));
        }
        updateVisibility();


        StringBuffer sb = new StringBuffer();
        if (loginUser.getRecipients() != null) {
            for (Recipient recipient : loginUser.getRecipients()) {
                sb.append(recipient.getAddress());
                sb.append("\n");
            }
        }

        emailAddresses.setValue(sb.toString());

    }
}
