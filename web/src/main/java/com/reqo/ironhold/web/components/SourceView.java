package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.BaseTheme;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@SuppressWarnings("serial")
public class SourceView extends Panel {

    private static Logger logger = Logger.getLogger(SourceView.class);

    @Autowired
    private IMimeMailMessageStorageService mimeMailMessageStorageService;
    @Autowired
    private MessageIndexService messageIndexService;


    private final SourceView me;
    private SearchHitPanel currentHitPanel;

    public SourceView() {
        this.setSizeFull();
        this.me = this;
    }

    public synchronized void show(SearchHitPanel newHitPanel, final SearchHit item, String criteria) throws Exception {

        this.removeAllComponents();

        final Label messageId = new Label("<b>MessageId: " + StringEscapeUtils.escapeHtml4(item.getId()) + "</b>");
        messageId.setContentMode(Label.CONTENT_XHTML);
        this.addComponent(messageId);


        Object mailMessage = null;
        String rawBody = null;
        if (item.getType().equals(
                IndexedObjectType.MIME_MESSAGE.getValue())) {
            mailMessage = mimeMailMessageStorageService.get("reqo", (String) item.getFields().get("year").getValue(), item.getId());
            rawBody = ((MimeMailMessage) mailMessage).getRawContents();

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


        final Label body = new Label(StringEscapeUtils.escapeHtml4(rawBody).replaceAll("\r?\n", "<br/>"));

        body.setContentMode(Label.CONTENT_RAW);
        this.addComponent(body);

    }


}
