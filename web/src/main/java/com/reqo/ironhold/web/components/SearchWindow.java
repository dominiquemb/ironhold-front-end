package com.reqo.ironhold.web.components;

import com.vaadin.server.Page;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Properties;

@SuppressWarnings("serial")
public class SearchWindow extends Window {
    private static Logger logger = Logger.getLogger(SearchWindow.class);

    @Autowired
    private SearchResults searchResults;

    @Autowired
    private SearchBar searchBar;
    private final VerticalLayout layout;
    private final String title;

    public SearchWindow(String title) throws Exception {
        this.title = title;
        this.setClosable(false);
        this.setSizeFull();
        layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setMargin(true);
        this.setContent(layout);
        final Properties prop = new Properties();
        prop.load(SearchWindow.class.getResourceAsStream("auth.properties"));

        LoginForm loginForm = new LoginForm();
        loginForm.setWidth("100%");
        loginForm.setHeight("350px");
        loginForm.addLoginListener(new LoginForm.LoginListener() {
            @Override
            public void onLogin(LoginForm.LoginEvent event) {
                String username = event.getLoginParameter("username");
                String password = event.getLoginParameter("password");


                String[] validUserNames = prop.getProperty("usernames").toString().split(",");
                boolean foundValidUsername = false;
                for (String validUserName : validUserNames) {
                    foundValidUsername = validUserName.equals(username);
                    if (foundValidUsername) break;
                }
                if (password.equals(prop.getProperty("password").toString()) && foundValidUsername) {
                    try {
                        login(username);
                    } catch (Exception e) {
                        logger.warn(e);
                    }
                } else {
                    Notification.show("Invalid credentials");
                }
            }
        });
        layout.addComponent(loginForm);

    }

    public void show() {
        this.searchBar.show();
        Page.getCurrent().setTitle(title);

    }


    private void login(String username) throws Exception {
        layout.removeAllComponents();


        searchResults.setIndexPrefix(username);

        final Button searchButton = new Button("Search");

        searchButton.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                String criteria = searchBar.getCriteria();
                if (criteria.trim().length() > 0) {
                    searchResults.setCriteria(criteria);
                } else {
                    searchResults.reset();
                }
            }
        });

        Header header = new Header(username);
        layout.addComponent(header);

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setSpacing(true);
        topLayout.addComponent(searchBar);
        topLayout.addComponent(searchButton);
        layout.addComponent(topLayout);

        layout.addComponent(searchResults);

        layout.setComponentAlignment(topLayout, Alignment.MIDDLE_CENTER);

    }
}
