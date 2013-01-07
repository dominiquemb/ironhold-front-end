package com.reqo.ironhold.web.components;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.search.SearchHit;

import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.search.model.IndexedObjectType;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.MimeMailMessage;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;

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

    public synchronized void show(SearchHitPanel newHitPanel, final SearchHit item, String criteria) throws Exception {

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

			final String downloadableContent = rawBody;
			
	        final Button download = new Button("Download");
	        download.setStyleName(BaseTheme.BUTTON_LINK);
	        download.addListener(new ClickListener() {

				public void buttonClick(ClickEvent event) {
					event.getButton().getWindow()
							.open(new StreamResource(new StreamSource() {

								public InputStream getStream() {
									return new ByteArrayInputStream(downloadableContent.getBytes());
								}
							}, item.getId() + ".eml", me
									.getApplication()));
				}
			});
	        this.addComponent(download);

		}

        

        final Label body = new Label(rawBody.replaceAll("\r?\n", "<br/>"));

        body.setContentMode(Label.CONTENT_RAW);
        this.addComponent(body);

    }


}
