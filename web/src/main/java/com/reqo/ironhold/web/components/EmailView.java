package com.reqo.ironhold.web.components;

import com.gs.collections.impl.utility.ArrayIterate;
import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.storage.es.IndexUtils;
import com.reqo.ironhold.storage.model.log.AuditActionEnum;
import com.reqo.ironhold.storage.model.log.AuditLogMessage;
import com.reqo.ironhold.web.domain.Attachment;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.IronholdApplication;
import com.reqo.ironhold.web.domain.IndexedMailMessage;
import com.reqo.ironhold.web.domain.MessageSearchResponse;
import com.reqo.ironhold.web.domain.Recipient;
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;

@SuppressWarnings("serial")
public class EmailView extends AbstractEmailView {
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

    public synchronized void show(SearchHitPanel newHitPanel, final IndexedMailMessage item,
                                  String criteria) throws Exception {
        final String client = (String) getSession().getAttribute("client");
        final LoginUser loginUser = (LoginUser) getSession().getAttribute("loginUser");

        layout.removeAllComponents();

        addEmailToolBar(layout, client, item);

        String subjectValue = item.getSubject();
        if (subjectValue.equals(StringUtils.EMPTY)) {
            subjectValue = "&lt;No subject&gt;";
        }
        HorizontalLayout subjectLayout = new HorizontalLayout();

        final Label subject = new Label(subjectValue);
        subject.setStyleName(Reindeer.LABEL_H2);
        subject.setContentMode(ContentMode.HTML);
        subjectLayout.addComponent(subject);

        String importance = item.getImportance();
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

        final SimpleDateFormat sdf = new SimpleDateFormat(
                "EEE, d MMM yyyy HH:mm:ss z");

        final Label date = new Label(sdf.format(item.getMessageDate()));
        date.setContentMode(ContentMode.HTML);
        date.setStyleName(Reindeer.LABEL_SMALL);
        layout.addComponent(date);

        addPartyLabel("From", item.getSender());
        addPartyLabel("To", item.getTo());
        addPartyLabel("CC", item.getCc());
        addPartyLabel("BCC", item.getBcc());

        final Label size = new Label(Long.toString(item.getSize()));
        size.setContentMode(ContentMode.HTML);
        size.setStyleName(Reindeer.LABEL_SMALL);
        layout.addComponent(size);

        MimeMailMessage mailMessage = new MimeMailMessage();
        Attachment[] attachments = null;

        IMimeMailMessageStorageService mimeMailMessageStorageService = ((IronholdApplication) this.getUI()).getMimeMailMessageStorageService();

        mailMessage.loadMimeMessageFromSource(mimeMailMessageStorageService.get(client, item.getYear(), item.getMonthDay(), item.getMessageId()));
        attachments = mailMessage.getAttachments();

        if (attachments != null) {
            for (final Attachment attachment : attachments) {
                if (attachment.getFileName().trim().length() > 0) {
                    final HorizontalLayout attachmentLayout = new HorizontalLayout();
                    attachmentLayout.setHeight("16px");
                    attachmentLayout.setSpacing(true);

                    final Link attachmentLink = new Link(attachment.getFileName(), new StreamResource(new StreamSource() {

                        public InputStream getStream() {
                            try {

                                MetaDataIndexService metaDataIndexService = ((IronholdApplication) getUI()).getMetaDataIndexService();
                                final String client = (String) getSession().getAttribute("client");
                                final LoginUser loginUser = (LoginUser) getSession().getAttribute("loginUser");
                                AuditLogMessage auditLogMessage = new AuditLogMessage(loginUser, AuditActionEnum.DOWNLOAD, item.getMessageId(), attachment.getFileName());
                                metaDataIndexService.store(client, auditLogMessage);
                            } catch (Exception e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

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
        MessageIndexService messageIndexService = ((IronholdApplication) this.getUI()).getMessageIndexService();
        MessageSearchResponse response = messageIndexService.search(
                messageIndexService.getNewBuilder(indexPrefix, loginUser).withCriteria(criteria)
                        .withId(item.getMessageId(), IndexedObjectType.getByValue(item.getMessageType())).withFullBody());

        String bodyText = null;
        if (displayHTML) {
            bodyText = mailMessage.getBodyHTML().isEmpty() ? mailMessage.getBody() : mailMessage.getBodyHTML();
        } else {
            bodyText = response.getMessages().get(0).getFormattedIndexedMailMessage().getBody().replaceAll("\r?\n", "<br/>");
            while (bodyText.contains("<br/> <br/>")) {
                bodyText = bodyText.replace("<br/> <br/>", "<br/>");
            }
        }


        final Label body = new Label(bodyText);
        body.setContentMode(Label.CONTENT_RAW);
        bodyLayout.addComponent(body);

        layout.addComponent(bodyLayout);

    }

    private void addPartyLabel(String type, Recipient[] recipients) {

        if (recipients.length > 0) {
            HorizontalLayout hl = new HorizontalLayout();
            final Label typeLabel = new Label(type + ":");
            typeLabel.setContentMode(ContentMode.HTML);
            hl.addComponent(typeLabel);
            final Label valueLabel = new Label(ArrayIterate.collect(recipients, Recipient.TO_NAME).makeString(", "));
            valueLabel.setContentMode(ContentMode.HTML);
            hl.addComponent(valueLabel);


            typeLabel.setWidth("35px");
            valueLabel.setWidth(null);
            hl.setExpandRatio(valueLabel, 1.0f);

            layout.addComponent(hl);
        }
    }

    private void addPartyLabel(String type, Recipient recipient) {

        if (recipient != null) {
            HorizontalLayout hl = new HorizontalLayout();
            final Label typeLabel = new Label(type + ":");
            typeLabel.setContentMode(ContentMode.HTML);
            hl.addComponent(typeLabel);
            final Label valueLabel = new Label(recipient.getName());
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
