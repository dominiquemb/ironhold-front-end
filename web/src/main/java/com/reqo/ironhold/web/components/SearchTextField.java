package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.model.log.AuditActionEnum;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.IronholdApplication;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import org.apache.log4j.Logger;

@SuppressWarnings({"serial", "unchecked"})
public class SearchTextField extends Panel {
    private static Logger logger = Logger.getLogger(SearchTextField.class);

    private final SearchResults searchResults;

    private final TextField textField;

    public SearchTextField(final SearchResults searchResults) {
        super();

        this.searchResults = searchResults;
        this.textField = new TextField();
        this.setContent(textField);

        this.textField.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.LAZY);
        this.textField.setTextChangeTimeout(200);
        this.textField.setWidth("500px");
        this.textField.setStyleName("search big");


        this.textField.addShortcutListener(new ShortcutListener("Default key", ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                String criteria = textField.getValue();
                if (criteria.trim().length() > 0) {
                    try {
                        searchResults.setCriteria(criteria);
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                } else {
                    final LoginUser authenticatedUser = (LoginUser) getSession().getAttribute("loginUser");
                    searchResults.reset(authenticatedUser);
                }

                UI.getCurrent().getScrollTop();

            }
        });

        this.textField.setTabIndex(0);

    }
/*
    public Action[] getActions(Object target, Object sender) {
        return new Action[]{action_ok};
    }
  */

    public void addTextChangeListener(FieldEvents.TextChangeListener textChangeListener) {
        this.textField.addTextChangeListener(textChangeListener);
    }

    public String getValue() {
        return this.textField.getValue();
    }

    public void init() {
        this.textField.focus();

        this.textField.addShortcutListener(new ShortcutListener("Arrow Down", ShortcutAction.KeyCode.ARROW_DOWN, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                SearchHistoryWindow window;
                try {
                    final String client = (String) getSession().getAttribute("client");
                    final LoginUser loginUser = (LoginUser) getUI().getSession().getAttribute("loginUser");
                    MetaDataIndexService metaDataIndexService = ((IronholdApplication) getUI()).getMetaDataIndexService();


                    window = new SearchHistoryWindow(textField, searchResults, metaDataIndexService.getAuditLogMessages(client, loginUser, AuditActionEnum.SEARCH));
                    window.setModal(true);
                    getUI().addWindow(window);

                } catch (Exception e) {
                    logger.warn(e);
                }
            }
        });
    }
}
