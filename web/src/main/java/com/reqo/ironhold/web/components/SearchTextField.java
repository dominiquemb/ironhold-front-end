package com.reqo.ironhold.web.components;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

@SuppressWarnings({"serial", "unchecked"})
public class SearchTextField extends TextField implements Handler {
    private final Action action_ok = new ShortcutAction("Default key", ShortcutAction.KeyCode.ENTER, null);

    private final SearchResults searchResults;

    public SearchTextField(SearchWindow mainWindow, SearchResults searchResults) {
        super();
        this.searchResults = searchResults;
        mainWindow.addActionHandler(this);
    }

    public Action[] getActions(Object target, Object sender) {
        return new Action[]{action_ok};
    }

    public void handleAction(Action action, Object sender, Object target) {
        if (action == action_ok) {
            String criteria = this.getValue();
            if (criteria.trim().length() > 0) {
                searchResults.setCriteria(criteria);
            } else {
                searchResults.reset();
            }

            UI.getCurrent().scrollIntoView(this.getParent().getParent());

        }

    }

}
