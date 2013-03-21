package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.storage.es.IndexUtils;
import com.vaadin.Application;
import com.vaadin.event.MouseEvents;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.search.SearchHit;

@SuppressWarnings("serial")
public class SearchHitPanel extends Panel {

    private final SearchHitPanel me;
    private static Logger logger = Logger.getLogger(SearchHitPanel.class);
	
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
        subject.setStyleName(BaseTheme.BUTTON_LINK);
        subject.addListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                try {
                    emailPreview.show(me, item, criteria);
                } catch (Exception e) {
                    logger.warn(e);
                }
            }
        });

        this.addListener(new MouseEvents.ClickListener() {

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

        addPartyLabel(item, IndexFieldEnum.FROM_NAME, IndexFieldEnum.FROM_ADDRESS);
        addPartyLabel(item, IndexFieldEnum.TO_NAME, IndexFieldEnum.TO_ADDRESS);
        addPartyLabel(item, IndexFieldEnum.CC_NAME, IndexFieldEnum.CC_ADDRESS);


    	this.addComponent(renderKeyValuePair("Body:", IndexUtils.getFieldValue(item, IndexFieldEnum.BODY) + "..."));

        String attachmentValue = IndexUtils.getFieldValue(item, IndexFieldEnum.ATTACHMENT);
        if (!attachmentValue.equals(StringUtils.EMPTY)) {
        	this.addComponent(renderKeyValuePair("Att.:", attachmentValue + "..."));
        }

    }


	private void addPartyLabel(SearchHit item, IndexFieldEnum field, IndexFieldEnum subField) {
        String value = IndexUtils.getFieldValue(item, field, subField);
        if (!value.equals(StringUtils.EMPTY)) {
        	this.addComponent(renderKeyValuePair(field.getLabel() + ":", value));
        }

    }


    private Component renderKeyValuePair(String caption, String value) {
    	HorizontalLayout hl = new HorizontalLayout();
    	final Label captionLabel = new Label(caption);
    	captionLabel.setWidth("35px");
    	hl.addComponent(captionLabel);
        final Label valueLabel = new Label(value);
        valueLabel.setContentMode(Label.CONTENT_XHTML);
        valueLabel.setWidth(null);
        
        hl.addComponent(valueLabel);
        hl.setExpandRatio(valueLabel, 1.0f);
        return hl;
	}
}
