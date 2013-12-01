package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.IronholdApplication;
import com.reqo.ironhold.web.domain.CountSearchResponse;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import java.util.concurrent.ExecutionException;

@SuppressWarnings("serial")
public class SearchBar extends VerticalLayout {
    private final SearchTextField searchTextField;

    public SearchBar(SearchTextField searchTextField) {
        this.searchTextField = searchTextField;
    }

    public void init(IronholdApplication ironholdApplication) throws Exception {
        final String client = (String) ironholdApplication.getSession().getAttribute("client");
        final LoginUser loginUser = (LoginUser) ironholdApplication.getSession().getAttribute("loginUser");


        final MessageIndexService messageIndexService = ironholdApplication.getMessageIndexService();
        final Label previewLabel = new Label(String.format(
                "%,d total messages", messageIndexService.getTotalMessageCount(client, loginUser).getMatches()));

        searchTextField.addTextChangeListener(new TextChangeListener() {

            public void textChange(TextChangeEvent event) {
                String criteria = event.getText();
                if (criteria.trim().length() > 0) {

                    long results = 0;
                    try {
                        CountSearchResponse matchResult = messageIndexService.getMatchCount(client, event.getText(), loginUser);
                        results = matchResult.getMatches();
                        if (results >= 0) {
                            previewLabel.setValue(String.format(
                                    "%,d matched messages", results));
                        } else {
                            previewLabel.setValue("Invalid search query");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                } else {
                    previewLabel.setValue(String.format("%,d total messages",
                            messageIndexService.getTotalMessageCount(client, loginUser)));
                }
            }
        });
        this.addComponent(searchTextField);
        this.addComponent(previewLabel);

        searchTextField.init();
    }

    public String getCriteria() {
        return searchTextField.getValue();
    }

}
