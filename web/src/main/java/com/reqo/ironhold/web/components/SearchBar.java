package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.MessageIndexService;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("serial")
public class SearchBar extends VerticalLayout {
    @Autowired
    private MessageIndexService messageIndexService;
    private SearchTextField filterField;

    public SearchBar(final Window window, SearchResults searchResults) {

        filterField = new SearchTextField(window, searchResults);

        final Label previewLabel = new Label(String.format(
                "%,d total messages", messageIndexService.getTotalMessageCount("reqo")));

        filterField.setTextChangeEventMode(TextChangeEventMode.LAZY);
        filterField.setTextChangeTimeout(200);
        filterField.setWidth("400px");
        filterField.addListener(new TextChangeListener() {

            public void textChange(TextChangeEvent event) {
                String criteria = event.getText();
                if (criteria.trim().length() > 0) {
                    long results = messageIndexService.getMatchCount("reqo", event.getText());
                    if (results >= 0) {
                        previewLabel.setValue(String.format(
                                "%,d matched messages", results));
                    } else {
                        previewLabel.setValue("Invalid search query");
                    }
                } else {
                    previewLabel.setValue(String.format("%,d total messages",
                            messageIndexService.getTotalMessageCount("reqo")));
                }
            }
        });
        this.addComponent(filterField);
        this.addComponent(previewLabel);

        filterField.focus();
    }

    public String getCriteria() {
        return (String) filterField.getValue();
    }

}
