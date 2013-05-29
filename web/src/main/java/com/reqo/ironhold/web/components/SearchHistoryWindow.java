package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.model.log.AuditLogMessage;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

/**
 * User: ilya
 * Date: 4/17/13
 * Time: 8:15 AM
 */
public class SearchHistoryWindow extends Window {
    private static final String TIMESTAMP = "Date";
    private static final String CRITERIA = "Criteria";
    private static Logger logger = Logger.getLogger(SearchHistoryWindow.class);

    private final SearchHistoryWindow
            window;

    public SearchHistoryWindow(final TextField textField, final SearchResults searchResults, List<AuditLogMessage> messages) {
        super("Search History");
        this.window = this;
        setHeight("190px");
        setWidth("400px");
        setResizable(false);

        VerticalLayout layout = new VerticalLayout();
        this.setContent(layout);
        layout.setMargin(true);
        layout.setSpacing(true);


        final Table searchHistoryTable = new Table();
        searchHistoryTable.setSizeFull();
        searchHistoryTable.setColumnWidth(CRITERIA, 60);
        searchHistoryTable.setColumnWidth(TIMESTAMP, 150);
        searchHistoryTable.setColumnExpandRatio(CRITERIA, 1);


        IndexedContainer searchHistory = new IndexedContainer();
        searchHistory.addContainerProperty(TIMESTAMP, Date.class, "");
        searchHistory.addContainerProperty(CRITERIA, String.class, "");

        int sourceCount = 0;

        for (AuditLogMessage message : messages) {
            Item sourceItem = searchHistory.addItem(sourceCount + ":" + message.getContext());
            sourceCount++;
            sourceItem.getItemProperty(CRITERIA).setValue(message.getContext());
            sourceItem.getItemProperty(TIMESTAMP).setValue(message.getTimestamp());
        }


        searchHistoryTable.setContainerDataSource(searchHistory);
        searchHistoryTable.setHeight("100px");

        layout.addComponent(searchHistoryTable);

        searchHistoryTable.setSelectable(true);
        searchHistoryTable.setMultiSelect(false);

        searchHistoryTable.select(searchHistoryTable.firstItemId());
        searchHistoryTable.focus();
        searchHistoryTable.addShortcutListener(new ShortcutListener("Default key", ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                String criteria = ((String) searchHistoryTable.getValue()).split(":")[1];
                if (criteria.trim().length() > 0) {
                    try {
                        textField.setValue(criteria);
                        searchResults.setCriteria(criteria);
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                window.close();

            }
        });

        searchHistoryTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                String criteria = ((String) searchHistoryTable.getValue()).split(":")[1];
                if (criteria.trim().length() > 0) {
                    try {
                        textField.setValue(criteria);
                        searchResults.setCriteria(criteria);
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                window.close();
            }
        });

        searchHistoryTable.addShortcutListener(new ShortcutListener("Esc", ShortcutAction.KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                window.close();
            }
        });

    }

}
