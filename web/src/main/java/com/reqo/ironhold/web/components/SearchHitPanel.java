package com.reqo.ironhold.web.components;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.SearchHit;

import com.reqo.ironhold.search.IndexFieldEnum;
import com.reqo.ironhold.search.IndexUtils;
import com.vaadin.Application;
import com.vaadin.event.MouseEvents;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class SearchHitPanel extends Panel {

    private final SearchHitPanel me;

    public SearchHitPanel(final SearchHit item, final EmailPreviewPanel emailPreview, final String criteria,
                          final Application application) {
        this.me = this;

        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(true);
        this.setContent(vl);
        me.setStyleName(Reindeer.PANEL_LIGHT);
        String subjectValue = IndexUtils.getFieldValue(item, IndexFieldEnum.SUBJECT);
        if (subjectValue.equals(StringUtils.EMPTY)) {
            subjectValue = "&lt;No subject&gt;";
        }


        final HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setSpacing(true);
        headerLayout.setWidth("100%");
        final NativeButton subject = new NativeButton(subjectValue);
        subject.setHtmlContentAllowed(true);
        subject.setStyleName(Reindeer.LABEL_H2);
        subject.addListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                try {
                    emailPreview.show(me, item, criteria);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        this.addListener(new MouseEvents.ClickListener() {

            public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
                try {
                    emailPreview.show(me, item, criteria);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        subject.setStyleName(BaseTheme.BUTTON_LINK);
        subject.setWidth(null);
        headerLayout.addComponent(subject);

        final Label size = new Label(IndexUtils.getFieldValue(item, IndexFieldEnum.SIZE));
        size.setContentMode(Label.CONTENT_XHTML);
        size.setStyleName(Reindeer.LABEL_SMALL);
        size.setWidth(null);
        headerLayout.addComponent(size);
        headerLayout.setExpandRatio(subject, 1.0f);
        headerLayout.setComponentAlignment(size, Alignment.MIDDLE_CENTER);

        this.addComponent(headerLayout);
        final Label date = new Label(IndexUtils.getFieldValue(item, IndexFieldEnum.DATE));
        date.setContentMode(Label.CONTENT_XHTML);
        date.setStyleName(Reindeer.LABEL_SMALL);
        this.addComponent(date);

        addPartyLabel(item, IndexFieldEnum.FROM_NAME);
        addPartyLabel(item, IndexFieldEnum.TO_NAME);
        addPartyLabel(item, IndexFieldEnum.CC_NAME);


        final Label body = new Label(IndexUtils.getFieldValue(item, IndexFieldEnum.BODY));
        body.setContentMode(Label.CONTENT_XHTML);
        this.addComponent(body);


        String attachmentValue = IndexUtils.getFieldValue(item, IndexFieldEnum.ATTACHMENT);
        if (!attachmentValue.equals(StringUtils.EMPTY)) {
            final Label attachment = new Label(attachmentValue);
            attachment.setContentMode(Label.CONTENT_XHTML);
            attachment.setStyleName(Reindeer.LABEL_SMALL);

            this.addComponent(attachment);
        }

    }

    private void addPartyLabel(SearchHit item, IndexFieldEnum field) {
        String value = IndexUtils.getFieldValue(item, field);
        if (!value.equals(StringUtils.EMPTY)) {
            HorizontalLayout hl = new HorizontalLayout();
            final Label typeLabel = new Label(field.getLabel() + ":");
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
