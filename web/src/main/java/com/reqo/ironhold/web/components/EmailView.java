package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.storage.es.IndexUtils;
import com.reqo.ironhold.storage.model.message.Attachment;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.vaadin.server.ClassResource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
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

    private String indexPrefix;
    private final boolean displayHTML;
    private final VerticalLayout layout;

    public EmailView(boolean displayHTML) {
        this.displayHTML = displayHTML;
        this.setSizeFull();
        layout = new VerticalLayout();
        layout.setMargin(true);
        this.setContent(layout);
    }

    public synchronized void show(SearchHitPanel newHitPanel, SearchHit item,
                                  String criteria) throws Exception {
        layout.removeAllComponents();

        String subjectValue = IndexUtils.getFieldValue(item,
                IndexFieldEnum.SUBJECT, null, false);
        if (subjectValue.equals(StringUtils.EMPTY)) {
            subjectValue = "&lt;No subject&gt;";
        }
        HorizontalLayout subjectLayout = new HorizontalLayout();

        final Label subject = new Label(subjectValue);
        subject.setStyleName(Reindeer.LABEL_H2);
        subject.setContentMode(ContentMode.HTML);
        subjectLayout.addComponent(subject);

        String importance = IndexUtils.getFieldValue(item, IndexFieldEnum.IMPORTANCE);
        if (importance != null && !importance.isEmpty()) {
            switch (importance) {
                case MimeMailMessage.IMPORTANCE_HIGH:
                case MimeMailMessage.IMPORTANCE_LOW:
                    final Button icon = new Button();
                    icon.setStyleName(BaseTheme.BUTTON_LINK);
                    icon.setIcon(new ClassResource("images/" + importance + ".png"));
                    subjectLayout.addComponent(icon);
            }
        }

        layout.addComponent(subjectLayout);

        final Label date = new Label(IndexUtils.getFieldValue(item,
                IndexFieldEnum.DATE));
        date.setContentMode(ContentMode.HTML);
        date.setStyleName(Reindeer.LABEL_SMALL);
        layout.addComponent(date);

        addPartyLabel(item, IndexFieldEnum.FROM_NAME, IndexFieldEnum.FROM_ADDRESS);
        addPartyLabel(item, IndexFieldEnum.TO_NAME, IndexFieldEnum.TO_ADDRESS);
        addPartyLabel(item, IndexFieldEnum.CC_NAME, IndexFieldEnum.CC_ADDRESS);

        final Label size = new Label(IndexUtils.getFieldValue(item,
                IndexFieldEnum.SIZE));
        size.setContentMode(ContentMode.HTML);
        size.setStyleName(Reindeer.LABEL_SMALL);
        layout.addComponent(size);

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

                    final Link attachmentLink = new Link(attachment.getFileName(), new StreamResource(new StreamSource() {

                        public InputStream getStream() {
                            byte[] byteArray = Base64
                                    .decodeBase64(attachment
                                            .getBody().getBytes());
                            return new ByteArrayInputStream(
                                    byteArray);
                        }
                    }, attachment.getFileName()));
                    attachmentLink.setIcon(new ClassResource("images/file.png"));

                    attachmentLayout.addComponent(attachmentLink);

                    final Label attachmentSizeLabel = new Label(
                            FileUtils.byteCountToDisplaySize(attachment.getSize()));
                    attachmentSizeLabel.setStyleName(Reindeer.LABEL_SMALL);
                    attachmentLayout.addComponent(attachmentSizeLabel);

                    attachmentLayout.setComponentAlignment(attachmentSizeLabel,
                            Alignment.MIDDLE_LEFT);
                    layout.addComponent(attachmentLayout);
                }

            }
        }

        final HorizontalLayout bodyLayout = new HorizontalLayout();
        bodyLayout.setMargin(new MarginInfo(true, true, true, true));
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

        layout.addComponent(bodyLayout);

    }

    private void addPartyLabel(SearchHit item, IndexFieldEnum field, IndexFieldEnum subField) {
        String value = IndexUtils.getFieldValue(item, field, subField);
        if (!value.equals(StringUtils.EMPTY)) {
            HorizontalLayout hl = new HorizontalLayout();
            final Label typeLabel = new Label(field.getLabel() + ":");
            typeLabel.setContentMode(ContentMode.HTML);
            hl.addComponent(typeLabel);
            final Label valueLabel = new Label(value);
            valueLabel.setContentMode(ContentMode.HTML);
            hl.addComponent(valueLabel);


            typeLabel.setWidth("35px");
            valueLabel.setWidth(null);
            hl.setExpandRatio(valueLabel, 1.0f);

            layout.addComponent(hl);
        }

    }

    public void setIndexPrefix(String indexPrefix) {
        this.indexPrefix = indexPrefix;
    }

    public String getIndexPrefix() {
        return indexPrefix;
    }
}
