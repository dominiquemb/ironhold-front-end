package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.model.user.LoginUser;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.*;

/**
 * User: ilya
 * Date: 4/17/13
 * Time: 8:15 AM
 */
public class EmailForwardWindow extends Window {
    private final String id;
    private final String message;
    private final EmailForwardWindow
            window;


    public EmailForwardWindow(final LoginUser loginUser, String id, String message) {
        super("Forward message");
        this.id = id;
        this.message = message;
        this.window = this;
        setHeight("190px");
        setWidth("300px");
        setResizable(false);
        VerticalLayout layout = new VerticalLayout();
        this.setContent(layout);
        layout.setMargin(true);
        layout.setSpacing(true);
        final TextField address = new TextField("Address:");

        address.setWidth("200px");
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
        hl.addComponent(forward);
        hl.addComponent(cancel);
        hl.setSpacing(true);

        layout.addComponent(address);
        layout.addComponent(toMe);
        layout.addComponent(hl);

    }
}
