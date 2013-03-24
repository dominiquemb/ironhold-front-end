package com.reqo.ironhold.web.components;

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apache.log4j.Logger;

import java.util.Properties;

@SuppressWarnings("serial")
public class SearchWindow extends Window {
    private static Logger logger = Logger.getLogger(SearchWindow.class);

    private final SearchWindow me;

    public SearchWindow(String title) throws Exception {
        super(title);
        final Properties prop = new Properties();
        prop.load(SearchWindow.class.getResourceAsStream("auth.properties"));

        this.me = this;
        LoginForm loginForm = new LoginForm();
        loginForm.setWidth("100%");
        loginForm.setHeight("350px");
        loginForm.addListener(new LoginForm.LoginListener() {
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
                    getWindow().showNotification("Invalid credentials");
                }
            }
        });
        addComponent(loginForm);

    }

    private void login(String username) throws Exception {
        this.removeAllComponents();
        VerticalLayout vl = new VerticalLayout();
        this.setContent(vl);
        vl.setSpacing(true);
        vl.setMargin(true);


        final SearchResults searchResults = new SearchResults(username);
        final SearchBar searchBar = new SearchBar(this, searchResults);
        final Button searchButton = new Button("Search");

        searchButton.addListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                String criteria = searchBar.getCriteria();
                if (criteria.trim().length() > 0) {
                    searchResults.setCriteria(criteria);
                } else {
                    searchResults.reset();
                }
            }
        });

        Header header = new Header(getApplication(), username);
        addComponent(header);

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setSpacing(true);
        topLayout.addComponent(searchBar);
        topLayout.addComponent(searchButton);
        addComponent(topLayout);

        addComponent(searchResults);

        vl.setComponentAlignment(topLayout, Alignment.MIDDLE_CENTER);

    }
}
