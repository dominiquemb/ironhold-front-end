package com.reqo.ironhold.web.components;

import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.reqo.ironhold.storage.model.PSTFileMeta;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
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
			"d MMM yyyy HH:mm:ss");

	public PSTFileMetaPanel(PSTFileMeta pstFile) {
		super(pstFile.getPstFileName() + " [" + pstFile.getOriginalFilePath()
				+ "] [" + pstFile.getMd5() + "]");
		this.setDescription("PST File Name [Original PST Location] [Checksum]");
		VerticalLayout layout = (VerticalLayout) getContent();
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSizeFull();
		hl.setSpacing(true);
		final GridLayout grid = new GridLayout(5, 9);
		grid.setSpacing(true);

		Label dateLabel = new Label("<b>Date:</b>");
		dateLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(dateLabel, 0, 0);
		grid.addComponent(new Label(sdf.format(pstFile.getFinished())), 1, 0);

		Label mailBoxLabel = new Label("<b>Mail Box:</b>");
		mailBoxLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(mailBoxLabel, 0, 1);
		grid.addComponent(new Label(pstFile.getMailBoxName()), 1, 1);

		Label commentsLabel = new Label("<b>Comments:</b>");
		commentsLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(commentsLabel, 0, 2);
		Label comments = new Label(StringUtils.abbreviate(
				pstFile.getCommentary(), 20));
		comments.setDescription(pstFile.getCommentary());
		grid.addComponent(comments, 1, 2);

		Label pstFileSizeLabel = new Label("<b>PST File Size:</b>");
		pstFileSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(pstFileSizeLabel, 0, 3);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize(pstFile.getSize())),
				1, 3);

		Label messagesLabel = new Label("<b># of Messages:</b>");
		messagesLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(messagesLabel, 0, 4);
		grid.addComponent(
				new Label(String.format("%,d", pstFile.getMessages())), 1, 4);

		Label duplicatesLabel = new Label("<b># of Duplicates:</b>");
		duplicatesLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(duplicatesLabel, 0, 5);
		grid.addComponent(
				new Label(String.format("%,d [%.0f%%]", pstFile.getDuplicates(), (double)100*pstFile.getDuplicates()/pstFile.getMessages())), 1, 5);

		Label failuresLabel = new Label("<b># of Failures:</b>");
		failuresLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(failuresLabel, 0, 6);
		grid.addComponent(
				new Label(String.format("%,d [%.0f%%]", pstFile.getFailures(), (double)100*pstFile.getFailures()/pstFile.getMessages())), 1, 6);

		Label partialFailuresLabel = new Label("<b># of Partial Failures:</b>");
		partialFailuresLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(partialFailuresLabel, 0, 7);
		grid.addComponent(
				new Label(String.format("%,d [%.0f%%]", pstFile.getPartialFailures(), (double)100*pstFile.getPartialFailures()/pstFile.getMessages())),
				1, 7);

		Label durationLabel = new Label("<b>Duration:</b>");
		durationLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(durationLabel, 0, 8);
		grid.addComponent(
				new Label(String.format("%,d secs", (pstFile.getFinished()
						.getTime() - pstFile.getStarted().getTime()) / 1000)),
				1, 8);

		// Msg Statistics

		Label msgStatisticsLabel = new Label("<b>Message Statistics</b>");
		msgStatisticsLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(msgStatisticsLabel, 2, 0);
		Label msgStatistics1Label = new Label("<b>w/o Attach.</b>");
		msgStatistics1Label.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(msgStatistics1Label, 3, 0);
		Label msgStatistics2Label = new Label("<b>w/Attach.</b>");
		msgStatistics2Label.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(msgStatistics2Label, 4, 0);


		Label averageSizeLabel = new Label("<b>Avg Size:</b>");
		averageSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(averageSizeLabel, 2, 1);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getAverageSize())), 3, 1);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getAverageAttachmentSize())), 4, 1);

		Label averageCompressedSizeLabel = new Label("<b>Avg Zipped Size:</b>");
		averageCompressedSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(averageCompressedSizeLabel, 2, 2);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getCompressedAverageSize())), 3, 2);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getCompressedAverageAttachmentSize())), 4, 2);

		Label medianSizeLabel = new Label("<b>Median Size:</b>");
		medianSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(medianSizeLabel, 2, 3);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getMedianSize())), 3, 3);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getMedianAttachmentSize())), 4, 3);

		Label medianCompressedSizeLabel = new Label(
				"<b>Median Zipped Size:</b>");
		medianCompressedSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(medianCompressedSizeLabel, 2, 4);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getMedianCompressedSize())), 3, 4);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getMedianCompressedAttachmentSize())), 4, 4);

		Label maxSizeLabel = new Label("<b>Max Size:</b>");
		maxSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(maxSizeLabel, 2, 5);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getMaxSize())), 3, 5);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getMaxAttachmentSize())), 4, 5);

		Label maxCompressedSizeLabel = new Label("<b>Max Zipped Size:</b>");
		maxCompressedSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(maxCompressedSizeLabel, 2, 6);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getCompressedMaxSize())), 3, 6);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) pstFile
						.getCompressedMaxAttachmentSize())), 4, 6);

		Label noAttachmentsLabel = new Label("<b>Count:</b>");
		noAttachmentsLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(noAttachmentsLabel, 2, 7);
		grid.addComponent(
				new Label(String.format("%,d [%.0f%%]",
						pstFile.getMessagesWithoutAttachments(), (double)100*pstFile.getMessagesWithoutAttachments()/pstFile.getMessages())), 3, 7);
		grid.addComponent(
				new Label(String.format("%,d [%.0f%%]",
						pstFile.getMessagesWithAttachments(), (double)100*pstFile.getMessagesWithAttachments()/pstFile.getMessages())), 4, 7);

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
		typeTable.setWidth("100%");
		
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
		folderTable.setWidth("100%");
		
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setSizeFull();
		vl.addComponent(folderTable);
		vl.addComponent(typeTable);

		hl.addComponent(vl);
		hl.setComponentAlignment(vl, Alignment.MIDDLE_LEFT);
		hl.setExpandRatio(vl, 1);
		layout.addComponent(hl);

	}

}
