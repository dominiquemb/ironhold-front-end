package com.reqo.ironhold.web.components;

import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

@SuppressWarnings("serial")
public class SearchWindow extends Window {

    private final SearchWindow me;

    public SearchWindow(String title) throws Exception {
        super(title);
        this.me = this;
        LoginForm loginForm = new LoginForm();
        loginForm.setWidth("100%");
        loginForm.setHeight("350px");
        loginForm.addListener(new LoginForm.LoginListener() {
            @Override
            public void onLogin(LoginForm.LoginEvent event) {
                String username = event.getLoginParameter("username");
                String password = event.getLoginParameter("password");


                if (password.equals("ironhold")) {
                    try {
                        login(username);
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
