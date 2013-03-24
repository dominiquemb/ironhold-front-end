package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.storage.es.IndexUtils;
import com.reqo.ironhold.storage.model.message.Attachment;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@SuppressWarnings("serial")
public class EmailView extends Panel {
    @Autowired
    private IMimeMailMessageStorageService mimeMailMessageStorageService;
    @Autowired
    private MessageIndexService messageIndexService;

    private final EmailView me;
    private final String indexPrefix;
    private SearchHitPanel currentHitPanel;
    private final boolean displayHTML;

    public EmailView(String indexPrefix, boolean displayHTML) {
        this.indexPrefix = indexPrefix;
        this.displayHTML = displayHTML;
        this.setSizeFull();
        this.me = this;
    }

    public synchronized void show(SearchHitPanel newHitPanel, SearchHit item,
                                  String criteria) throws Exception {

        this.removeAllComponents();

        String subjectValue = IndexUtils.getFieldValue(item,
                IndexFieldEnum.SUBJECT, null, false);
        if (subjectValue.equals(StringUtils.EMPTY)) {
            subjectValue = "&lt;No subject&gt;";
        }
        HorizontalLayout subjectLayout = new HorizontalLayout();

        final Label subject = new Label(subjectValue);
        subject.setStyleName(Reindeer.LABEL_H2);
        subject.setContentMode(Label.CONTENT_XHTML);
        subjectLayout.addComponent(subject);

        String importance = IndexUtils.getFieldValue(item, IndexFieldEnum.IMPORTANCE);
        if (importance != null && !importance.isEmpty()) {
            switch (importance) {
                case MimeMailMessage.IMPORTANCE_HIGH:
                case MimeMailMessage.IMPORTANCE_LOW:
                    final Button icon = new Button();
                    icon.setStyleName(BaseTheme.BUTTON_LINK);
                    icon.setIcon(new ClassResource("images/" + importance + ".png",
                            getApplication()));
                    subjectLayout.addComponent(icon);
            }
        }

        this.addComponent(subjectLayout);

        final Label date = new Label(IndexUtils.getFieldValue(item,
                IndexFieldEnum.DATE));
        date.setContentMode(Label.CONTENT_XHTML);
        date.setStyleName(Reindeer.LABEL_SMALL);
        this.addComponent(date);

        addPartyLabel(item, IndexFieldEnum.FROM_NAME, IndexFieldEnum.FROM_ADDRESS);
        addPartyLabel(item, IndexFieldEnum.TO_NAME, IndexFieldEnum.TO_ADDRESS);
        addPartyLabel(item, IndexFieldEnum.CC_NAME, IndexFieldEnum.CC_ADDRESS);

        final Label size = new Label(IndexUtils.getFieldValue(item,
                IndexFieldEnum.SIZE));
        size.setContentMode(Label.CONTENT_XHTML);
        size.setStyleName(Reindeer.LABEL_SMALL);
        this.addComponent(size);

        MimeMailMessage mailMessage = new MimeMailMessage();
        Attachment[] attachments = null;

        if (item.getType().equals(
                IndexedObjectType.MIME_MESSAGE.getValue())) {
            mailMessage.loadMimeMessageFromSource(mimeMailMessageStorageService.get("reqo", (String) item.getFields().get("year").getValue(), item.getId()));
            attachments = mailMessage.getAttachments();
        }

        if (attachments != null) {
            for (final Attachment attachment : attachments) {
                if (attachment.getFileName().trim().length() > 0) {
                    final HorizontalLayout attachmentLayout = new HorizontalLayout();
                    attachmentLayout.setHeight("16px");
                    attachmentLayout.setSpacing(true);

                    final Button attachmentLink = new Button(
                            attachment.getFileName());
                    attachmentLink.setIcon(new ClassResource("images/file.png",
                            getApplication()));
                    attachmentLink.setStyleName(BaseTheme.BUTTON_LINK);
                    attachmentLink.addListener(new ClickListener() {

                        public void buttonClick(ClickEvent event) {
                            event.getButton().getWindow()
                                    .open(new StreamResource(new StreamSource() {

                                        public InputStream getStream() {
                                            byte[] byteArray = Base64
                                                    .decodeBase64(attachment
                                                            .getBody().getBytes());
                                            return new ByteArrayInputStream(
                                                    byteArray);
                                        }
                                    }, attachment.getFileName(), me
                                            .getApplication()));
                        }
                    });
                    attachmentLayout.addComponent(attachmentLink);

                    final Label attachmentSizeLabel = new Label(
                            FileUtils.byteCountToDisplaySize(attachment.getSize()));
                    attachmentSizeLabel.setStyleName(Reindeer.LABEL_SMALL);
                    attachmentLayout.addComponent(attachmentSizeLabel);

                    attachmentLayout.setComponentAlignment(attachmentSizeLabel,
                            Alignment.MIDDLE_LEFT);
                    this.addComponent(attachmentLayout);
                }

            }
        }

        final HorizontalLayout bodyLayout = new HorizontalLayout();
        bodyLayout.setMargin(new Layout.MarginInfo(true, true, true, true));
        bodyLayout.setSizeFull();
        SearchHits hits = messageIndexService.search(
                messageIndexService.getNewBuilder(indexPrefix).withCriteria(criteria)
                        .withId(item.getId(), IndexedObjectType.getByValue(item.getType())).withFullBody()).getHits();

        String bodyText = null;
        if (displayHTML) {
            bodyText = mailMessage.getBodyHTML().isEmpty() ? mailMessage.getBody() : mailMessage.getBodyHTML();
        } else {
            bodyText = IndexUtils.getFieldValue(hits.getAt(0),
                    IndexFieldEnum.BODY, null, false).replaceAll("\r?\n", "<br/>");
        }

        final Label body = new Label(bodyText);
        body.setContentMode(Label.CONTENT_RAW);
        bodyLayout.addComponent(body);

        this.addComponent(bodyLayout);

    }

    private void addPartyLabel(SearchHit item, IndexFieldEnum field, IndexFieldEnum subField) {
        String value = IndexUtils.getFieldValue(item, field, subField);
        if (!value.equals(StringUtils.EMPTY)) {
            HorizontalLayout hl = new HorizontalLayout();
            final Label typeLabel = new Label(field.getLabel() + ":");
            typeLabel.setContentMode(Label.CONTENT_XHTML);
            hl.addComponent(typeLabel);
            final Label valueLabel = new Label(value);
            valueLabel.setContentMode(Label.CONTENT_XHTML);
            hl.addComponent(valueLabel);


            typeLabel.setWidth("35px");
            valueLabel.setWidth(null);
            hl.setExpandRatio(valueLabel, 1.0f);

            this.addComponent(hl);
        }

    }
}
