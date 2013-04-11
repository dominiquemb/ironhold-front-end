package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.web.IronholdApplication;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class SearchBar extends VerticalLayout {
    private final SearchTextField searchTextField;

    public SearchBar(SearchTextField searchTextField) {
        this.searchTextField = searchTextField;
    }

    public void init(IronholdApplication ironholdApplication) {
        final MessageIndexService messageIndexService = ironholdApplication.getMessageIndexService();
        final Label previewLabel = new Label(String.format(
                "%,d total messages", messageIndexService.getTotalMessageCount("reqo")));

        searchTextField.addTextChangeListener(new TextChangeListener() {

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
        this.addComponent(searchTextField);
        this.addComponent(previewLabel);

        searchTextField.focus();
    }

    public String getCriteria() {
        return searchTextField.getValue();
    }

}
