package com.reqo.ironhold.web.components;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.SearchHit;

import com.reqo.ironhold.search.IndexFieldEnum;
import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.search.IndexUtils;
import com.reqo.ironhold.search.model.IndexedObjectType;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.MimeMailMessage;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

@SuppressWarnings("serial")
public class SourceView extends Panel {

    private final IStorageService storageService;
    private final IndexService indexService;
    private final SourceView me;
    private SearchHitPanel currentHitPanel;

    public SourceView(IStorageService storageService, IndexService indexService) {
        this.storageService = storageService;
        this.indexService = indexService;
        this.setSizeFull();
        this.me = this;
    }

    public synchronized void show(SearchHitPanel newHitPanel, SearchHit item, String criteria) throws Exception {

        this.removeAllComponents();

        final Label messageId = new Label("<b>MessageId: " + item.getId() + "</b>");
        messageId.setContentMode(Label.CONTENT_XHTML);
        this.addComponent(messageId);

        
        Object mailMessage = null;
        String rawBody = null;
    	if (item.getType().equals(IndexedObjectType.PST_MESSAGE.getValue())) {
			mailMessage = storageService.getMailMessage(item.getId(), true);
			rawBody = MailMessage.serializeMailMessage((MailMessage)mailMessage);	
		} else if (item.getType().equals(
				IndexedObjectType.MIME_MESSAGE.getValue())) {
			mailMessage = storageService.getMimeMailMessage(item.getId());
			rawBody = ((MimeMailMessage)mailMessage).getRawContents();
			
		}

        

        final Label body = new Label(rawBody.replaceAll("\r?\n", "<br/>"));

        body.setContentMode(Label.CONTENT_RAW);
        this.addComponent(body);

    }


}
