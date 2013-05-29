package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.log.AuditActionEnum;
import com.reqo.ironhold.storage.model.log.AuditLogMessage;
import com.reqo.ironhold.storage.model.message.Recipient;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import com.reqo.ironhold.web.components.validators.UniqueUsernameValidator;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * User: ilya
 * Date: 5/23/13
 * Time: 7:37 AM
 */
public class LoginUserViewer extends Panel {
    private static final String TIMESTAMP = "Date";
    private static final String CRITERIA = "Criteria";

    private final VerticalLayout layout;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM dd, YYYY HH:mm");
    private final MiscIndexService miscIndexService;

    private final Label name;
    private final Label username;
    private final Label email;

    private final Button edit;
    private final Label enabled;
    private final Label superUser;
    private final String client;
    private final MetaDataIndexService metaDataIndexService;
    private final Label searchResults;
    private final MessageIndexService messageIndexService;
    private LoginUser loginUser;
    private final Map<RoleEnum, Label> roleToggles;
    private final Table searchHistoryTable;
    private final Label emailAddresses;
    private UniqueUsernameValidator uniqueUsernameValidator;

    public LoginUserViewer(final UserManagementWindow window, final String client, final MessageIndexService messageIndexService, final MiscIndexService miscIndexService, MetaDataIndexService metaDataIndexService) {
        this.client = client;
        this.messageIndexService = messageIndexService;
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

        name = new Label();
        addLabelComponentPair("Name:", name);

        email = new Label();
        addLabelComponentPair("Email:", email);

        username = new Label();
        addLabelComponentPair("Username:", username);

        enabled = new Label();
        addLabelComponentPair("Enabled:", enabled);

        superUser = new Label();
        addLabelComponentPair("Administrator:", superUser);

        VerticalLayout vl = new VerticalLayout();
        for (RoleEnum role : RoleEnum.values()) {
            if (role != RoleEnum.SUPER_USER && role != RoleEnum.NONE) {
                Label roleCheckbox = new Label(role.toString());
                roleToggles.put(role, roleCheckbox);

                vl.addComponent(roleCheckbox);
            }
        }
        addLabelComponentPair("Rights:", vl);

        emailAddresses = new Label();
        addLabelComponentPair("Other Email Addresses:", emailAddresses);

        searchResults = new Label();
        addLabelComponentPair("Maximum Search Results:", searchResults);


        searchHistoryTable = new Table();
        searchHistoryTable.setWidth("300px");
        searchHistoryTable.setColumnWidth(CRITERIA, 60);
        searchHistoryTable.setColumnWidth(TIMESTAMP, 150);
        searchHistoryTable.setColumnExpandRatio(CRITERIA, 1);


        searchHistoryTable.setHeight("100px");
        addLabelComponentPair("Search History", searchHistoryTable);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        edit = new Button("Edit");
        edit.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    window.setEditMode(loginUser);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (ExecutionException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });


        buttonLayout.setSpacing(true);
        buttonLayout.addComponent(edit);

        layout.addComponent(buttonLayout);
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
        layout.setVisible(true);
        setCaption("View " + loginUser.getName());
        name.setValue(loginUser.getName());
        email.setValue(loginUser.getMainRecipient().getAddress());
        username.setValue(loginUser.getUsername());

        enabled.setValue(Boolean.toString(loginUser.getRolesBitMask() != RoleEnum.NONE.getValue()));
        superUser.setValue(Boolean.toString(loginUser.hasRole(RoleEnum.SUPER_USER)));
        for (RoleEnum role : roleToggles.keySet()) {
            roleToggles.get(role).setValue(role.toString() + ": " + Boolean.toString(loginUser.hasRole(role)));
        }


        int sourceCount = 0;
        IndexedContainer searchHistory = new IndexedContainer();
        searchHistory.addContainerProperty(TIMESTAMP, Date.class, "");
        searchHistory.addContainerProperty(CRITERIA, String.class, "");


        for (AuditLogMessage message : metaDataIndexService.getAuditLogMessages(client, loginUser, AuditActionEnum.SEARCH)) {
            Item sourceItem = searchHistory.addItem(sourceCount + ":" + message.getContext());
            sourceCount++;
            sourceItem.getItemProperty(CRITERIA).setValue(message.getContext());
            sourceItem.getItemProperty(TIMESTAMP).setValue(message.getTimestamp());
        }
        searchHistoryTable.setContainerDataSource(searchHistory);

        StringBuffer sb = new StringBuffer();
        if (loginUser.getRecipients() != null) {
            for (Recipient recipient : loginUser.getRecipients()) {
                sb.append(recipient.getAddress());
                sb.append("\n");
            }
        }

        emailAddresses.setValue(sb.toString());
        if (loginUser.hasRole(RoleEnum.CAN_SEARCH)) {
            long messageCount = messageIndexService.getTotalMessageCount(client, loginUser);
            searchResults.setValue(messageCount + " messages");
        } else {
            searchResults.setValue("User can't search");
        }

    }
}
