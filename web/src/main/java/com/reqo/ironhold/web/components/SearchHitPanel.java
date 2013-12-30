package com.reqo.ironhold.web.components;

import com.gs.collections.impl.utility.ArrayIterate;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.web.IronholdApplication;
import com.reqo.ironhold.web.domain.*;
import com.vaadin.event.MouseEvents;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;

@SuppressWarnings("serial")
public class SearchHitPanel extends Panel {

    private final SearchHitPanel me;
    private static Logger logger = Logger.getLogger(SearchHitPanel.class);
    private final VerticalLayout layout;

    public SearchHitPanel(final FormattedIndexedMailMessage item, final EmailPreviewPanel emailPreview, final String criteria, final IronholdApplication ironholdApplication) throws Exception {
        this.me = this;
        MetaDataIndexService metaDataIndexService = ironholdApplication.getMetaDataIndexService();
        final String client = (String) ironholdApplication.getSession().getAttribute("client");
        final LoginUser loginUser = (LoginUser) ironholdApplication.getSession().getAttribute("loginUser");
        AuditLogMessage auditLogMessage = new AuditLogMessage(loginUser, AuditActionEnum.PREVIEW, item.getMessageId(), criteria);
        metaDataIndexService.store(client, auditLogMessage);

        layout = new VerticalLayout();
        this.setContent(layout);
        layout.setMargin(true);
        me.setStyleName(Reindeer.PANEL_LIGHT);
        String subjectValue = item.getSubject();
        if (subjectValue.equals(StringUtils.EMPTY)) {
            subjectValue = "&lt;No subject&gt;";
        }


        final HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setSpacing(true);
        headerLayout.setWidth("560px");
        if (subjectValue.length() > 100) {
            subjectValue = StringUtils.abbreviate(subjectValue, 100) + "...";
        }
        final NativeButton subject = new NativeButton(subjectValue);
        subject.setHtmlContentAllowed(true);
        subject.setStyleName(BaseTheme.BUTTON_LINK);
        subject.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                try {
                    emailPreview.show(me, item, criteria);
                } catch (Exception e) {
                    logger.warn(e);
                }
            }
        });

        this.addClickListener(new MouseEvents.ClickListener() {

            public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
                try {
                    emailPreview.show(me, item, criteria);
                } catch (Exception e) {
                    logger.warn(e);
                }
            }
        });
        subject.setStyleName(BaseTheme.BUTTON_LINK);
        subject.setWidth(null);
        headerLayout.addComponent(subject);

        final Label size = new Label(Long.toString(item.getSize()));
        size.setContentMode(ContentMode.HTML);
        size.setStyleName(Reindeer.LABEL_SMALL);
        size.setWidth(null);
        headerLayout.addComponent(size);
        headerLayout.setExpandRatio(subject, 1.0f);
        headerLayout.setComponentAlignment(size, Alignment.MIDDLE_CENTER);

        layout.addComponent(headerLayout);

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

        layout.addComponent(renderKeyValuePair("Body:", StringUtils.abbreviate(item.getBody(), 100) + "..."));

        String attachmentValue = ArrayIterate.makeString(item.getAttachments(), ",");
        if (!attachmentValue.equals(StringUtils.EMPTY)) {
            layout.addComponent(renderKeyValuePair("Att.:", attachmentValue + "..."));
        }

    }


    private void addPartyLabel(String type, Recipient recipient) {
        String value = recipient.getName();
        if (!value.equals(StringUtils.EMPTY)) {
            layout.addComponent(renderKeyValuePair(type + ":", value));
        }

    }


    private void addPartyLabel(String type, Recipient[] recipients) {
        if (recipients.length > 0) {
            layout.addComponent(renderKeyValuePair(type + ":", ArrayIterate.collect(recipients, Recipient.TO_NAME).makeString(",")));
        }
    }

    private Component renderKeyValuePair(String caption, String value) {
        HorizontalLayout hl = new HorizontalLayout();
        final Label captionLabel = new Label(caption);
        captionLabel.setWidth("35px");
        hl.addComponent(captionLabel);
        final Label valueLabel = new Label(value);
        valueLabel.setContentMode(ContentMode.HTML);
        valueLabel.setWidth("530px");
        hl.addComponent(valueLabel);
        hl.setExpandRatio(valueLabel, 1.0f);
        return hl;
    }
}
