package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.IronholdApplication;
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
                "%,d total messages", messageIndexService.getTotalMessageCount(client, loginUser)));

        searchTextField.addTextChangeListener(new TextChangeListener() {

            public void textChange(TextChangeEvent event) {
                String criteria = event.getText();
                if (criteria.trim().length() > 0) {

                    long results = 0;
                    try {
                        results = messageIndexService.getMatchCount(client, event.getText(), loginUser);
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
                    try {
                        previewLabel.setValue(String.format("%,d total messages",
                                messageIndexService.getTotalMessageCount(client, loginUser)));
                    } catch (ExecutionException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
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
