package com.reqo.ironhold.web.components;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.SearchHit;

import com.reqo.ironhold.search.IndexFieldEnum;
import com.reqo.ironhold.search.IndexUtils;
import com.vaadin.event.MouseEvents;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class SearchHitPanel extends Panel {

	private final SearchHitPanel me;
	public SearchHitPanel(final SearchHit item, final EmailPreview emailPreview, final String criteria) {
		this.me = this;
		
		me.setStyleName(Reindeer.PANEL_LIGHT);
		String subjectValue = IndexUtils.getFieldValue(item,
				IndexFieldEnum.SUBJECT);
		if (subjectValue.equals(StringUtils.EMPTY)) {
			subjectValue = "&lt;No subject&gt;";
		}
		
		final HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setSpacing(true);
		final NativeButton subject = new NativeButton(subjectValue);
		subject.setHtmlContentAllowed(true);
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
		headerLayout.addComponent(subject);
		
		final Label size = new Label(IndexUtils.getFieldValue(item,
				IndexFieldEnum.SIZE));
		size.setContentMode(Label.CONTENT_XHTML);
		size.setStyleName(Reindeer.LABEL_SMALL);
		headerLayout.addComponent(size);
		headerLayout.setComponentAlignment(size, Alignment.MIDDLE_CENTER);
		
		this.addComponent(headerLayout);
		final Label date = new Label(IndexUtils.getFieldValue(item,
				IndexFieldEnum.DATE));
		date.setContentMode(Label.CONTENT_XHTML);
		date.setStyleName(Reindeer.LABEL_SMALL);
		this.addComponent(date);

		addPartyLabel(item, IndexFieldEnum.FROM);
		addPartyLabel(item, IndexFieldEnum.TO);
		addPartyLabel(item, IndexFieldEnum.CC);

		

		final Label body = new Label(IndexUtils.getFieldValue(item,
				IndexFieldEnum.BODY));
		body.setContentMode(Label.CONTENT_XHTML);
		this.addComponent(body);

		String attachmentValue = IndexUtils.getFieldValue(item,
				IndexFieldEnum.ATTACHMENT);
		if (!attachmentValue.equals(StringUtils.EMPTY)) {
			final Label attachment = new Label("Attachments: "
					+ attachmentValue);
			attachment.setContentMode(Label.CONTENT_XHTML);
			attachment.setStyleName(Reindeer.LABEL_SMALL);
			this.addComponent(attachment);
		}

	}

	private void addPartyLabel(SearchHit item, IndexFieldEnum field) {
		String value = IndexUtils.getFieldValue(item, field);
		if (!value.equals(StringUtils.EMPTY)) {
			final Label from = new Label(field.toString() + ": " + value);
			from.setContentMode(Label.CONTENT_XHTML);
			this.addComponent(from);
		}

	}

}
