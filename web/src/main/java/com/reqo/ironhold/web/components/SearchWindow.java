package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.IronholdApplication;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("serial")
public class SearchWindow extends Panel {
    private static Logger logger = Logger.getLogger(SearchWindow.class);

    private final SearchResults searchResults;

    private final SearchBar searchBar;

    private final VerticalLayout layout;
    private final LoginPanel loginPanel;

    public SearchWindow(SearchResults searchResults, SearchBar searchBar, LoginPanel loginPanel) throws Exception {
        this.searchResults = searchResults;
        this.searchBar = searchBar;
        this.setSizeFull();
        layout = new VerticalLayout();
        this.setContent(layout);
        layout.setMargin(true);
        final Properties prop = new Properties();
        prop.load(SearchWindow.class.getResourceAsStream("auth.properties"));

        this.loginPanel = loginPanel;
        layout.addComponent(loginPanel);

    }

    public void init(IronholdApplication ironholdApplication) throws ExecutionException, InterruptedException {

        //  this.addActionHandler(searchTextField);

        Page.getCurrent().setTitle("Ironhold");
        this.loginPanel.init(ironholdApplication);

    }


    public void login() throws Exception {
        this.searchBar.init((IronholdApplication) this.getUI());
        layout.removeAllComponents();

        LoginUser authenticatedUser = (LoginUser) getSession().getAttribute("loginUser");
        String client = (String) getSession().getAttribute("client");
        searchResults.setIndexPrefix(client);

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

        Header header = new Header(authenticatedUser.getName());
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
