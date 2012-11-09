package com.reqo.ironhold.web.components;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.SearchHit;

import com.reqo.ironhold.search.IndexFieldEnum;
import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.search.IndexUtils;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.model.Attachment;
import com.reqo.ironhold.storage.model.MailMessage;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class EmailPreview extends Panel {

	private final IStorageService storageService;
	private final IndexService indexService;
	private final EmailPreview me;
	private SearchHitPanel currentHitPanel;

	public EmailPreview(IStorageService storageService,
			IndexService indexService) {
		this.storageService = storageService;
		this.indexService = indexService;
		this.setVisible(false);
		this.setSizeFull();
		this.me = this;
	}

	public synchronized void show(SearchHitPanel newHitPanel, SearchHit item,
			String criteria) throws Exception {

		this.removeAllComponents();
		if (currentHitPanel != null) {
			currentHitPanel.setStyleName(Reindeer.PANEL_LIGHT);
		}
		newHitPanel.setStyleName(null);
		currentHitPanel = newHitPanel;
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

		MailMessage mailMessage = storageService.getMailMessage(item.getId(),
				true);

		Attachment[] attachments = mailMessage.getPstMessage().getAttachments();
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

		String subjectValue = IndexUtils.getFieldValue(item,
				IndexFieldEnum.SUBJECT, false);
		if (subjectValue.equals(StringUtils.EMPTY)) {
			subjectValue = "&lt;No subject&gt;";
		}
		final Label subject = new Label(subjectValue);
		subject.setStyleName(Reindeer.LABEL_H2);
		this.addComponent(subject);

		final Label body = new Label(IndexUtils.getFieldValue(
				indexService.searchAndFilterById(item.getId(), criteria, 0, 1),
				IndexFieldEnum.BODY, true).replaceAll("\r?\n", "<br/>"));
		body.setContentMode(Label.CONTENT_RAW);
		this.addComponent(body);

	}

	private void addPartyLabel(SearchHit item, IndexFieldEnum field) {
		String value = IndexUtils.getFieldValue(item, field, false);
		if (!value.equals(StringUtils.EMPTY)) {
			final Label from = new Label(field.toString() + ": " + value);
			from.setContentMode(Label.CONTENT_XHTML);
			this.addComponent(from);
		}

	}

}
