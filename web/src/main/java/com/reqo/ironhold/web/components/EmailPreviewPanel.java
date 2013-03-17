package com.reqo.ironhold.web.components;

import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.storage.IStorageService;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.themes.Reindeer;
import org.apache.log4j.Logger;
import org.elasticsearch.search.SearchHit;

@SuppressWarnings("serial")
public class EmailPreviewPanel extends TabSheet {
    private static Logger logger = Logger.getLogger(EmailPreviewPanel.class);

    private SearchHitPanel currentHitPanel;
    private final EmailView htmlView;
    private final EmailView textView;
    private final SourceView sourceView;
    private final AuditView auditView;
    private SearchHitPanel newHitPanel;
    private SearchHit item;
    private String criteria;

    public EmailPreviewPanel(String indexPrefix, IStorageService storageService, IndexService indexService) {
        this.setVisible(false);
        this.setSizeFull();

        this.htmlView = new EmailView(indexPrefix, storageService, indexService, true);
        this.textView = new EmailView(indexPrefix, storageService, indexService, false);

        this.sourceView = new SourceView(storageService, indexService);
        this.auditView = new AuditView(storageService);

        this.addTab(textView, "Text");
        this.addTab(htmlView, "Graphical");
        this.addTab(sourceView, "Source");
        this.addTab(auditView, "Audit");

        this.addListener(new SelectedTabChangeListener() {

            @Override
            public void selectedTabChange(SelectedTabChangeEvent event) {
                try {
                    updateCurrentTab();
                } catch (Exception e) {
                    logger.warn(e);
                }
            }
        });
        this.setStyleName(Reindeer.TABSHEET_MINIMAL);
    }

    public synchronized void show(SearchHitPanel newHitPanel, SearchHit item, String criteria) throws Exception {
        this.newHitPanel = newHitPanel;
        this.item = item;
        this.criteria = criteria;

        if (currentHitPanel != null) {
            currentHitPanel.setStyleName(Reindeer.PANEL_LIGHT);
        }
        newHitPanel.setStyleName(null);
        currentHitPanel = newHitPanel;

        this.setVisible(true);
        updateCurrentTab();
    }

    private synchronized void updateCurrentTab() throws Exception {
        if (this.getSelectedTab() instanceof EmailView) {
            ((EmailView)this.getSelectedTab()).show(newHitPanel, item, criteria);
        } else if (this.getSelectedTab() instanceof SourceView) {
            this.sourceView.show(newHitPanel, item, criteria);
        } else if (this.getSelectedTab() instanceof AuditView) {
            this.auditView.show(newHitPanel, item, criteria);
        }
    }
}
