package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import com.reqo.ironhold.web.IronholdApplication;
import com.vaadin.server.ClassResource;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;

@SuppressWarnings("serial")
public class Header extends HorizontalLayout {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G");
    private static Logger logger = Logger.getLogger(Header.class);

    public Header() {
        this.setWidth("100%");
        setSizeFull();
    }

    public void init(final LoginUser loginUser, final String client, final IronholdApplication application) {
       /* Button logo = new Button();
        logo.setStyleName(BaseTheme.BUTTON_LINK);
        logo.setIcon(new ClassResource("images/logo.png"));
        this.addComponent(logo);
        this.setComponentAlignment(logo, Alignment.TOP_LEFT);
         */
        VerticalLayout vl = new VerticalLayout();


        vl.setSizeFull();
        vl.setWidth(null);
        Label label = new Label("Welcome, " + loginUser.getUsername());
        label.setWidth(null);

        vl.addComponent(label);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        if (loginUser.hasRole(RoleEnum.CAN_MANAGE_USERS)) {
            Button userManagement = new Button("Users");
            userManagement.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    UserManagementWindow window;
                    try {
                        window = new UserManagementWindow(loginUser, client, application.getMessageIndexService(), application.getMiscIndexService(), application.getMetaDataIndexService());
                        window.setModal(true);
                        getUI().addWindow(window);

                    } catch (Exception e) {
                        logger.warn(e);
                    }

                }
            });

            hl.addComponent(userManagement);
        }

        Button logout = new Button("Logout");
        logout.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                UI.getCurrent().getSession().close();
                Page.getCurrent().setLocation(Page.getCurrent().getLocation());
            }
        });
        hl.addComponent(logout);

        vl.addComponent(hl);

        VerticalLayout left = new VerticalLayout();
        Image image = new Image("", new ClassResource("images/logo.gif"));
        left.addComponent(image);
        left.setWidth(null);
        HorizontalLayout spacer = new HorizontalLayout();
        spacer.setHeight("25px");
        vl.addComponent(spacer);
        this.addComponent(left);
        this.addComponent(vl);
        this.setComponentAlignment(vl, Alignment.TOP_RIGHT);
        this.setExpandRatio(left, 1.0f);
    }
}
