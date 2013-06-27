package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.model.log.AuditActionEnum;
import com.reqo.ironhold.storage.model.log.AuditLogMessage;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.IronholdApplication;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.themes.Reindeer;
import org.apache.log4j.Logger;
import org.elasticsearch.search.SearchHit;

@SuppressWarnings("serial")
public class EmailPreviewPanel extends TabSheet {
    private static Logger logger = Logger.getLogger(EmailPreviewPanel.class);

    private final EmailView htmlView;

    private final EmailView textView;

    private final SourceView sourceView;

    private final AuditView auditView;

    private SearchHitPanel currentHitPanel;

    private SearchHitPanel newHitPanel;
    private SearchHit item;
    private String criteria;
    private String indexPrefix;
    private boolean tabsConfigured = false;

    public EmailPreviewPanel(EmailView htmlView, EmailView textView, SourceView sourceView, AuditView auditView) {
        this.htmlView = htmlView;
        this.textView = textView;
        this.sourceView = sourceView;
        this.auditView = auditView;
        this.setVisible(false);


        this.addSelectedTabChangeListener(new SelectedTabChangeListener() {

            @Override
            public void selectedTabChange(SelectedTabChangeEvent event) {
                try {
                    updateCurrentTab();
                } catch (Exception e) {
                    logger.error("Received exception updating current tab", e);
                }
            }
        });
        this.setStyleName(Reindeer.TABSHEET_MINIMAL);
    }


    public synchronized void show(SearchHitPanel newHitPanel, SearchHit item, String criteria) throws Exception {
        MetaDataIndexService metaDataIndexService = ((IronholdApplication) this.getUI()).getMetaDataIndexService();
        final String client = (String) getSession().getAttribute("client");
        final LoginUser loginUser = (LoginUser) getSession().getAttribute("loginUser");
        AuditLogMessage auditLogMessage = new AuditLogMessage(loginUser, AuditActionEnum.VIEW, item.getId(), criteria);
        metaDataIndexService.store(client, auditLogMessage);
        if (!tabsConfigured) {
            this.addTab(textView, "Text");
            this.addTab(htmlView, "HTML");
            this.addTab(sourceView, "Source");
            this.addTab(auditView, "Audit");
            tabsConfigured = true;
        }
        this.newHitPanel = newHitPanel;
        this.item = item;
        this.criteria = criteria;

        if (currentHitPanel != null) {
            currentHitPanel.setStyleName(Reindeer.PANEL_LIGHT);
        }
        newHitPanel.setStyleName(null);

        currentHitPanel = newHitPanel;

        this.setVisible(true);
        this.setSizeFull();
        updateCurrentTab();
    }

    private synchronized void updateCurrentTab() throws Exception {
        if (item != null) {
            if (this.getSelectedTab() instanceof EmailView) {
                ((EmailView) this.getSelectedTab()).show(newHitPanel, item, criteria);
            } else if (this.getSelectedTab() instanceof SourceView) {
                this.sourceView.show(newHitPanel, item, criteria);
            } else if (this.getSelectedTab() instanceof AuditView) {
                this.auditView.show(newHitPanel, item, criteria);
            }
        }
    }

    public void setIndexPrefix(String indexPrefix) {
        this.indexPrefix = indexPrefix;
        htmlView.setIndexPrefix(indexPrefix);
        textView.setIndexPrefix(indexPrefix);
    }

    public String getIndexPrefix() {
        return indexPrefix;
    }
}
