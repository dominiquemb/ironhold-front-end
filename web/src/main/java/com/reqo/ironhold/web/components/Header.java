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
    private final HorizontalLayout searchLayout;

    public Header(HorizontalLayout searchLayout) {
        this.searchLayout = searchLayout;
        this.setWidth("100%");
        this.setSpacing(true);
        this.setMargin(true);
        setSizeFull();
    }

    public void init(final LoginUser loginUser, final String client, final IronholdApplication application) {

        Image image = new Image("", new ClassResource("images/logo.gif"));

        VerticalLayout left = new VerticalLayout();
        left.setWidth(Dimensions.LEFT);
        left.addComponent(image);
        left.setSpacing(true);
        this.addComponent(left);

        VerticalLayout middle = new VerticalLayout();
        middle.addComponent(searchLayout);
        middle.setWidth(Dimensions.MIDDLE);
        middle.setSpacing(true);
        middle.setMargin(true);
        this.addComponent(middle);
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

        HorizontalLayout spacer = new HorizontalLayout();
        spacer.setHeight("25px");
        vl.addComponent(spacer);
        this.addComponent(vl);
        this.setExpandRatio(vl, 1.0F);
        this.setComponentAlignment(vl, Alignment.TOP_RIGHT);
    }
}
