package com.reqo.ironhold.web.components;

import com.reqo.ironhold.web.IronholdApplication;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apache.log4j.Logger;

import java.util.Properties;

@SuppressWarnings("serial")
public class SearchWindow extends Panel {
    private static Logger logger = Logger.getLogger(SearchWindow.class);

    private final SearchResults searchResults;

    private final SearchBar searchBar;

    private final VerticalLayout layout;

    public SearchWindow(SearchResults searchResults, SearchBar searchBar) throws Exception {
        this.searchResults = searchResults;
        this.searchBar = searchBar;
        this.setSizeFull();
        layout = new VerticalLayout();
        this.setContent(layout);
        layout.setMargin(true);
        final Properties prop = new Properties();
        prop.load(SearchWindow.class.getResourceAsStream("auth.properties"));

        LoginForm loginForm = new LoginForm();
        loginForm.setSizeFull();
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

    public void init(IronholdApplication ironholdApplication) {
        this.searchBar.init(ironholdApplication);
        //  this.addActionHandler(searchTextField);

        Page.getCurrent().setTitle("Ironhold");

    }


    private void login(String username) throws Exception {
        layout.removeAllComponents();


        searchResults.setIndexPrefix(username);

        final Button searchButton = new Button("Search");

        searchButton.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                String criteria = searchBar.getCriteria();
                if (criteria.trim().length() > 0) {
                    try {
                        searchResults.setCriteria(criteria);
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
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
        searchResults.setIndexPrefix("reqo");
    }
}
