package com.reqo.ironhold.web.components;

import com.vaadin.Application;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import java.text.SimpleDateFormat;

@SuppressWarnings("serial")
public class Header extends HorizontalLayout {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G");

    public Header(Application application, final String username) {
        this.setWidth("100%");
        setSizeFull();

//		Button logo = new Button();
//		logo.setStyleName(BaseTheme.BUTTON_LINK);
//		logo.setIcon(new ClassResource("images/logo.png", application));
//
//		this.addComponent(logo);
//
//		this.setComponentAlignment(logo, Alignment.TOP_LEFT);

        VerticalLayout vl = new VerticalLayout();
        Button logout = new Button("Logout");
        logout.addListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                getApplication().close();

            }
        });
        vl.setSizeFull();
        vl.setWidth("100");
        Label label = new Label("Welcome, " + username);
        label.setWidth(null);

        vl.addComponent(label);
        vl.addComponent(logout);

        this.addComponent(vl);
        this.setComponentAlignment(vl, Alignment.TOP_RIGHT);
    }
}
