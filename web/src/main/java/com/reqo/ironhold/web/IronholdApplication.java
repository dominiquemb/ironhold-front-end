package com.reqo.ironhold.web;

import com.reqo.ironhold.storage.LocalMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.web.components.*;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
@Theme(value = "ironhold")
@Scope("prototype")
@Component
@PreserveOnRefresh
public class IronholdApplication extends UI {
    private static Logger logger = Logger.getLogger(IronholdApplication.class);

    private SearchWindow searchWindow;
    private SearchResults searchResults;
    private SearchBar searchBar;
    private SearchTextField searchTextField;
    private EmailPreviewPanel emailPreview;
    private EmailView htmlView;
    private EmailView textView;
    private SourceView sourceView;
    private AuditView auditView;
    private LoginPanel loginPanel;

    @Autowired
    private MessageIndexService messageIndexService;

    @Autowired
    private MetaDataIndexService metaDataIndexService;

    @Autowired
    private MiscIndexService miscIndexService;

    @Autowired
    private LocalMimeMailMessageStorageService mimeMailMessageStorageService;


    public IronholdApplication() throws Exception {
        loginPanel = new LoginPanel();
        htmlView = new EmailView(true);
        textView = new EmailView(false);
        sourceView = new SourceView();
        auditView = new AuditView();
        emailPreview = new EmailPreviewPanel(htmlView, textView, sourceView, auditView);
        searchResults = new SearchResults(emailPreview);
        searchTextField = new SearchTextField(searchResults);
        searchBar = new SearchBar(searchTextField);
        searchWindow = new SearchWindow(searchResults, searchBar, loginPanel);

    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();
        setContent(layout);

        layout.addComponent(searchWindow);
        try {
            searchWindow.init(this);
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public SearchWindow getSearchWindow() {
        return searchWindow;
    }

    public SearchResults getSearchResults() {
        return searchResults;
    }

    public SearchBar getSearchBar() {
        return searchBar;
    }

    public SearchTextField getSearchTextField() {
        return searchTextField;
    }

    public EmailPreviewPanel getEmailPreview() {
        return emailPreview;
    }

    public EmailView getHtmlView() {
        return htmlView;
    }

    public EmailView getTextView() {
        return textView;
    }

    public SourceView getSourceView() {
        return sourceView;
    }

    public AuditView getAuditView() {
        return auditView;
    }

    public MessageIndexService getMessageIndexService() {
        return messageIndexService;
    }

    public MetaDataIndexService getMetaDataIndexService() {
        return metaDataIndexService;
    }

    public MiscIndexService getMiscIndexService() {
        return miscIndexService;
    }

    public LocalMimeMailMessageStorageService getMimeMailMessageStorageService() {
        return mimeMailMessageStorageService;
    }
}
