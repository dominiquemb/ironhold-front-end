package com.reqo.ironhold.web.components;

import org.elasticsearch.search.SearchHit;

import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.storage.IStorageService;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class EmailPreviewPanel extends TabSheet {

    private SearchHitPanel currentHitPanel;
    private final EmailView emailView;
    private final SourceView sourceView;
    private final AuditView auditView;
	private SearchHitPanel newHitPanel;
	private SearchHit item;
	private String criteria;

    public EmailPreviewPanel(IStorageService storageService, IndexService indexService) {
        this.setVisible(false);
        this.setSizeFull();

        this.emailView = new EmailView(storageService, indexService);
        this.sourceView = new SourceView(storageService, indexService);
        this.auditView = new AuditView(storageService);

        this.addTab(emailView, "Email");
        this.addTab(sourceView, "Source");
        this.addTab(auditView, "Audit");

        this.addListener(new SelectedTabChangeListener() {
			
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				try {
					updateCurrentTab();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
			 this.emailView.show(newHitPanel, item, criteria);
		} else if (this.getSelectedTab() instanceof SourceView) {
			this.sourceView.show(newHitPanel, item, criteria);
		} else if (this.getSelectedTab() instanceof AuditView) {
			this.auditView.show(newHitPanel, item, criteria);
		}
    }
}
