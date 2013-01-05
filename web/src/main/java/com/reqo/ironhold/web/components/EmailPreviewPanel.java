package com.reqo.ironhold.web.components;

import org.elasticsearch.search.SearchHit;

import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.storage.IStorageService;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class EmailPreviewPanel extends TabSheet {

    private SearchHitPanel currentHitPanel;
    private final EmailView emailView;
    private final SourceView sourceView;
    private final AuditView auditView;

    public EmailPreviewPanel(IStorageService storageService, IndexService indexService) {
        this.setVisible(false);
        this.setSizeFull();

        this.emailView = new EmailView(storageService, indexService);
        this.sourceView = new SourceView(storageService, indexService);
        this.auditView = new AuditView(storageService);

        this.addTab(emailView, "Email");
        this.addTab(sourceView, "Source");
        this.addTab(auditView, "Audit");

        this.setStyleName(Reindeer.TABSHEET_MINIMAL);
    }

    public synchronized void show(SearchHitPanel newHitPanel, SearchHit item, String criteria) throws Exception {
        if (currentHitPanel != null) {
            currentHitPanel.setStyleName(Reindeer.PANEL_LIGHT);
        }
        newHitPanel.setStyleName(null);
        currentHitPanel = newHitPanel;

        this.setVisible(true);
        this.emailView.show(newHitPanel, item, criteria);
        this.sourceView.show(newHitPanel, item, criteria);
        this.auditView.show(newHitPanel, item, criteria);
    }
}
