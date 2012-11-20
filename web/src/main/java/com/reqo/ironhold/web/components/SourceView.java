package com.reqo.ironhold.web.components;

import com.reqo.ironhold.search.IndexFieldEnum;
import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.search.IndexUtils;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.model.MailMessage;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.SearchHit;

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

        final Label messageId = new Label("MessageId: " + item.getId());
        this.addComponent(messageId);

        MailMessage mailMessage = storageService.getMailMessage(item.getId(), true);

        String rawBody = MailMessage.serializeMailMessagePretty(mailMessage).replaceAll("\r?\n", "<br/>");

        final Label body = new Label(rawBody);

        body.setContentMode(Label.CONTENT_RAW);
        this.addComponent(body);

    }

    private void addPartyLabel(SearchHit item, IndexFieldEnum field) {
        String value = IndexUtils.getFieldValue(item, field, false);
        if (!value.equals(StringUtils.EMPTY)) {
            final Label from = new Label(String.format("%s: %s", field.getLabel(), value));
            from.setContentMode(Label.CONTENT_XHTML);
            this.addComponent(from);
        }

    }

}
