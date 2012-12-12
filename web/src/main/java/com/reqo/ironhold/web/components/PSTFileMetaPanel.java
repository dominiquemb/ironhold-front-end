package com.reqo.ironhold.web.components;

import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.reqo.ironhold.storage.model.PSTFileMeta;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

public class PSTFileMetaPanel extends Panel {
	private static final String TYPE = "Type";
	private static final String COUNT = "Count";
	private static final Object FOLDER = "Folder";

	private final SimpleDateFormat sdf = new SimpleDateFormat(
			"EEE, d MMM yyyy HH:mm:ss z");

	public PSTFileMetaPanel(PSTFileMeta pstFile) {
		super(pstFile.getPstFileName());
		VerticalLayout layout = (VerticalLayout) getContent();
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSizeFull();
		hl.setSpacing(true);
		final GridLayout grid = new GridLayout(6, 11);
		grid.setSpacing(true);
		
		Label dateLabel = new Label("<b>Date:</b>");
		dateLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(dateLabel, 0, 0);
		grid.addComponent(new Label(sdf.format(pstFile.getFinished())), 1, 0);

		Label mailBoxLabel = new Label("<b>Mail Box:</b>");
		mailBoxLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(mailBoxLabel, 0, 1);
		grid.addComponent(new Label(pstFile.getMailBoxName()), 1, 1);

		Label filePathLabel = new Label("<b>Original File Path:</b>");
		filePathLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(filePathLabel, 0, 2);
		grid.addComponent(new Label(pstFile.getOriginalFilePath()), 1, 2);

		Label md5Label = new Label("<b>MD5 CheckSum:</b>");
		md5Label.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(md5Label, 0, 3);
		grid.addComponent(new Label(pstFile.getMd5()), 1, 3);

		Label commentsLabel = new Label("<b>Comments:</b>");
		commentsLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(commentsLabel, 0, 4);
		grid.addComponent(new Label(pstFile.getCommentary()), 1, 4);

		Label pstFileSizeLabel = new Label("<b>PST File Size:</b>");
		pstFileSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(pstFileSizeLabel, 0, 5);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize(pstFile.getSize())),
				1, 5);

		Label messagesLabel = new Label("<b># of Messages:</b>");
		messagesLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(messagesLabel, 0, 6);
		grid.addComponent(
				new Label(String.format("%,d", pstFile.getMessages())), 1, 6);

		Label duplicatesLabel = new Label("<b># of Duplicates:</b>");
		duplicatesLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(duplicatesLabel, 0, 7);
		grid.addComponent(
				new Label(String.format("%,d", pstFile.getDuplicates())), 1, 7);

		Label failuresLabel = new Label("<b># of Failures:</b>");
		failuresLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(failuresLabel, 0, 8);
		grid.addComponent(
				new Label(String.format("%,d", pstFile.getFailures())), 1, 8);

		Label partialFailuresLabel = new Label("<b># of Partial Failures:</b>");
		partialFailuresLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(partialFailuresLabel, 0, 9);
		grid.addComponent(
				new Label(String.format("%,d", pstFile.getPartialFailures())),
				1, 9);

		Label durationLabel = new Label("<b>Duration:</b>");
		durationLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(durationLabel, 0, 10);
		grid.addComponent(
				new Label(String.format("%,d secs", (pstFile.getFinished().getTime()-pstFile.getStarted().getTime())/1000)),
				1, 10);

		// Message Statistics

		Label averageSizeLabel = new Label("<b>Average Message Size:</b>");
		averageSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(averageSizeLabel, 2, 0);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getAverageSize())), 3, 0);

		Label averageCompressedSizeLabel = new Label(
				"<b>Average Compressed Message Size:</b>");
		averageCompressedSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(averageCompressedSizeLabel, 2, 1);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getCompressedAverageSize())), 3, 1);

		Label medianSizeLabel = new Label("<b>Median Message Size:</b>");
		medianSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(medianSizeLabel, 2, 2);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getMedianSize())), 3, 2);

		Label medianCompressedSizeLabel = new Label(
				"<b>Median Compressed Message Size:</b>");
		medianCompressedSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(medianCompressedSizeLabel, 2, 3);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getMedianCompressedSize())), 3, 3);

		Label maxSizeLabel = new Label("<b>Max Message Size:</b>");
		maxSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(maxSizeLabel, 2, 4);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getMaxSize())), 3, 4);

		Label maxCompressedSizeLabel = new Label(
				"<b>Max Compressed Message Size:</b>");
		maxCompressedSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(maxCompressedSizeLabel, 2, 5);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getCompressedMaxSize())), 3, 5);

		Label noAttachmentsLabel = new Label(
				"<b># of Messages without Attachments:</b>");
		noAttachmentsLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(noAttachmentsLabel, 2, 6);
		grid.addComponent(
				new Label(String.format("%,d",
						pstFile.getMessagesWithoutAttachments())), 3, 6);

		// Attachment statistics

		Label averageAttachmentSizeLabel = new Label(
				"<b>Average Attachment Attachment Size:</b>");
		averageAttachmentSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(averageAttachmentSizeLabel, 4, 0);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getAverageAttachmentSize())), 5, 0);

		Label averageAttachmentCompressedSizeLabel = new Label(
				"<b>Average Compressed Attachment Size:</b>");
		averageAttachmentCompressedSizeLabel
				.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(averageAttachmentCompressedSizeLabel, 4, 1);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getCompressedAverageAttachmentSize())), 5, 1);

		Label medianAttachmentSizeLabel = new Label(
				"<b>Median Attachment Size:</b>");
		medianAttachmentSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(medianAttachmentSizeLabel, 4, 2);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getMedianAttachmentSize())), 5, 2);

		Label medianCompressedAttachmentSizeLabel = new Label(
				"<b>Median Compressed Attachment Size:</b>");
		medianCompressedAttachmentSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(medianCompressedAttachmentSizeLabel, 4, 3);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getMedianCompressedAttachmentSize())), 5, 3);

		Label maxAttachmentSizeLabel = new Label("<b>Max Attachment Size:</b>");
		maxAttachmentSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(maxAttachmentSizeLabel, 4, 4);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getMaxAttachmentSize())), 5, 4);

		Label maxCompressedAttachmentSizeLabel = new Label(
				"<b>Max Compressed Attachment Size:</b>");
		maxCompressedAttachmentSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(maxCompressedAttachmentSizeLabel, 4, 5);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getCompressedMaxAttachmentSize())), 5, 5);

		Label attachmentsLabel = new Label(
				"<b># of Messages with Attachments:</b>");
		attachmentsLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(attachmentsLabel, 4, 6);
		grid.addComponent(
				new Label(String.format("%,d",
						pstFile.getMessagesWithAttachments())), 5, 6);

		
		hl.addComponent(grid);
		
		final Table typeTable = new Table("Content Types");
		typeTable.setSizeFull();
		typeTable.setColumnWidth(COUNT, 60);

		IndexedContainer types = new IndexedContainer();
		types.addContainerProperty(TYPE, String.class, "");
		types.addContainerProperty(COUNT, Integer.class, "");

		int typeCount = 0;
		for (String type : pstFile.getTypeMap().keySet()) {

			Item typeItem = types.addItem(typeCount);
			typeCount++;
			typeItem.getItemProperty(TYPE).setValue(type);
			typeItem.getItemProperty(COUNT).setValue(
					pstFile.getTypeMap().get(type));

		}

		typeTable.setContainerDataSource(types);
		typeTable.setHeight("100px");
		
		final Table folderTable = new Table("Folders");
		folderTable.setSizeFull();
		folderTable.setColumnWidth(COUNT, 60);

		IndexedContainer folders = new IndexedContainer();
		folders.addContainerProperty(FOLDER, String.class, "");
		folders.addContainerProperty(COUNT, Integer.class, "");

		int folderCount = 0;
		for (String folder : pstFile.getFolderMap().keySet()) {

			if (!folder.trim().equals(StringUtils.EMPTY)) {
				Item folderItem = folders.addItem(folderCount);
				folderCount++;
				folderItem.getItemProperty(FOLDER).setValue(folder);
				folderItem.getItemProperty(COUNT).setValue(
						pstFile.getFolderMap().get(folder));
			}

		}

		folderTable.setContainerDataSource(folders);
		folderTable.setHeight("200px");
		

		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setWidth("500px");
		vl.addComponent(folderTable);
		vl.addComponent(typeTable);

		hl.addComponent(vl);
		layout.addComponent(hl);

	}

}
