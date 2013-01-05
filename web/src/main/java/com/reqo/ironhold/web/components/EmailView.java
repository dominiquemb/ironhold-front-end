package com.reqo.ironhold.web.components;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.reqo.ironhold.search.IndexFieldEnum;
import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.search.IndexUtils;
import com.reqo.ironhold.search.model.IndexedObjectType;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.model.Attachment;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.MimeMailMessage;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class EmailView extends Panel {

	private final IStorageService storageService;
	private final IndexService indexService;
	private final EmailView me;
	private SearchHitPanel currentHitPanel;

	public EmailView(IStorageService storageService, IndexService indexService) {
		this.storageService = storageService;
		this.indexService = indexService;
		this.setSizeFull();
		this.me = this;
	}

	public synchronized void show(SearchHitPanel newHitPanel, SearchHit item,
			String criteria) throws Exception {

		this.removeAllComponents();

		String subjectValue = IndexUtils.getFieldValue(item,
				IndexFieldEnum.SUBJECT, false);
		if (subjectValue.equals(StringUtils.EMPTY)) {
			subjectValue = "&lt;No subject&gt;";
		}
		final Label subject = new Label(subjectValue);
		subject.setStyleName(Reindeer.LABEL_H2);
		subject.setContentMode(Label.CONTENT_XHTML);
		this.addComponent(subject);

		final Label date = new Label(IndexUtils.getFieldValue(item,
				IndexFieldEnum.DATE));
		date.setContentMode(Label.CONTENT_XHTML);
		date.setStyleName(Reindeer.LABEL_SMALL);
		this.addComponent(date);

		addPartyLabel(item, IndexFieldEnum.FROM_NAME);
		addPartyLabel(item, IndexFieldEnum.TO_NAME);
		addPartyLabel(item, IndexFieldEnum.CC_NAME);

		final Label size = new Label(IndexUtils.getFieldValue(item,
				IndexFieldEnum.SIZE));
		size.setContentMode(Label.CONTENT_XHTML);
		size.setStyleName(Reindeer.LABEL_SMALL);
		this.addComponent(size);

		Object mailMessage = null;
		Attachment[] attachments = null;
		
		if (item.getType().equals(IndexedObjectType.PST_MESSAGE.getValue())) {
			mailMessage = storageService.getMailMessage(item.getId(), true);
			attachments = ((MailMessage) mailMessage).getAttachments();
		} else if (item.getType().equals(
				IndexedObjectType.MIME_MESSAGE.getValue())) {
			mailMessage = storageService.getMimeMailMessage(item.getId());
			attachments = ((MimeMailMessage) mailMessage).getAttachments();
		}

		if (attachments != null) {
			for (final Attachment attachment : attachments) {
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

		final HorizontalLayout bodyLayout = new HorizontalLayout();
		bodyLayout.setMargin(new Layout.MarginInfo(true, true, true, true));
		bodyLayout.setSizeFull();
		SearchHits hits = indexService.search(
				indexService.getNewBuilder().withCriteria(criteria)
						.withId(item.getId(), IndexedObjectType.getByValue(item.getType())).withFullBody()).getHits();
		final Label body = new Label(IndexUtils.getFieldValue(hits.getAt(0),
				IndexFieldEnum.BODY, true).replaceAll("\r?\n", "<br/>"));
		body.setContentMode(Label.CONTENT_RAW);
		bodyLayout.addComponent(body);

		this.addComponent(bodyLayout);

	}

	private void addPartyLabel(SearchHit item, IndexFieldEnum field) {
		String value = IndexUtils.getFieldValue(item, field);
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
