package com.reqo.ironhold.web.components;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.SearchHit;

import com.reqo.ironhold.search.IndexFieldEnum;
import com.reqo.ironhold.search.IndexUtils;
import com.reqo.ironhold.storage.IStorageService;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class EmailPreview extends Panel {

	private final IStorageService storageService;

	public EmailPreview(IStorageService storageService) {
		this.storageService = storageService;
		this.setVisible(false);
		this.setSizeFull();
	}

	public void show(SearchHit item) {
		this.removeAllComponents();
		this.setVisible(true);

		final Label date = new Label(IndexUtils.getFieldValue(item,
				IndexFieldEnum.DATE));
		date.setContentMode(Label.CONTENT_XHTML);
		date.setStyleName(Reindeer.LABEL_SMALL);
		this.addComponent(date);

		addPartyLabel(item, IndexFieldEnum.FROM);
		addPartyLabel(item, IndexFieldEnum.TO);
		addPartyLabel(item, IndexFieldEnum.CC);

		final Label size = new Label(IndexUtils.getFieldValue(item,
				IndexFieldEnum.SIZE));
		size.setContentMode(Label.CONTENT_XHTML);
		size.setStyleName(Reindeer.LABEL_SMALL);
		this.addComponent(size);

		String subjectValue = IndexUtils.getFieldValue(item,
				IndexFieldEnum.SUBJECT, false);
		if (subjectValue.equals(StringUtils.EMPTY)) {
			subjectValue = "&lt;No subject&gt;";
		}
		final Label subject = new Label(subjectValue);
		subject.setStyleName(Reindeer.LABEL_H2);
		this.addComponent(subject);

		
		HorizontalLayout hl = new HorizontalLayout();

		final Label body = new Label(IndexUtils.getFieldValue(item,
				IndexFieldEnum.BODY, false));
		body.setContentMode(Label.CONTENT_PREFORMATTED);
		hl.addComponent(body);
		hl.setSizeUndefined();

		Panel bodyPanel = new Panel(hl);
		bodyPanel.setScrollable(true);
		bodyPanel.setStyleName(Reindeer.PANEL_LIGHT);
		bodyPanel.setSizeFull();
		
		this.addComponent(bodyPanel);
		
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
