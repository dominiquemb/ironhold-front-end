package com.reqo.ironhold.web.components;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

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


        final IndexService indexService = new IndexService(username);
        final IStorageService storageService = new MongoService(username, "web");

        final SearchResults searchResults = new SearchResults(indexService, storageService);
        final SearchBar searchBar = new SearchBar(this, indexService, storageService, searchResults);
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

        Header header = new Header(getApplication(), username, storageService);
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
