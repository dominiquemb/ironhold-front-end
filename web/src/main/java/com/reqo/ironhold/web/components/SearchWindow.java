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
        VerticalLayout vl = new VerticalLayout();
        this.setContent(vl);
        vl.setSpacing(true);
        vl.setMargin(true);

        final IndexService indexService = new IndexService("reqo");
        final IStorageService storageService = new MongoService("reqo", "web");

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


        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setSpacing(true);
        topLayout.addComponent(searchBar);
        topLayout.addComponent(searchButton);
        addComponent(topLayout);

        addComponent(searchResults);

        vl.setComponentAlignment(topLayout, Alignment.MIDDLE_CENTER);
    }
}
