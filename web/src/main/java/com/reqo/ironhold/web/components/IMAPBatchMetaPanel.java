package com.reqo.ironhold.web.components;

import com.reqo.ironhold.model.message.eml.IMAPBatchMeta;
import com.vaadin.ui.*;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.Period;
import org.elasticsearch.common.joda.time.format.PeriodFormatter;
import org.elasticsearch.common.joda.time.format.PeriodFormatterBuilder;

import java.text.SimpleDateFormat;

@SuppressWarnings("serial")
public class IMAPBatchMetaPanel extends Panel {
	
	private static PeriodFormatter minsAndSecsFormatter = new PeriodFormatterBuilder().appendMinutes().appendSuffix(" min ", " mins ").appendSeconds().appendSuffix(" sec", " secs").toFormatter();
   
	private final SimpleDateFormat sdf = new SimpleDateFormat(
			"d MMM yyyy HH:mm:ss");

	public IMAPBatchMetaPanel(IMAPBatchMeta imapBatch) {
		super(imapBatch.getSource().getProtocol() + "://" + imapBatch.getSource().getImapSource() + ":" + imapBatch.getSource().getImapPort());  
		this.setDescription("protocol://host:port");
		VerticalLayout layout = (VerticalLayout) getContent();
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSizeFull();
		hl.setSpacing(true);
		final GridLayout grid = new GridLayout(5, 9);
		grid.setSpacing(true);

		Label dateLabel = new Label("<b>Date:</b>");
		dateLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(dateLabel, 0, 0);
		grid.addComponent(new Label(sdf.format(imapBatch.getFinished())), 1, 0);

		Label mailBoxLabel = new Label("<b>Mail Box:</b>");
		mailBoxLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(mailBoxLabel, 0, 1);
		grid.addComponent(new Label(imapBatch.getSource().getUsername()), 1, 1);


		Label messagesLabel = new Label("<b># of Messages:</b>");
		messagesLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(messagesLabel, 0, 2);
		grid.addComponent(
				new Label(String.format("%,d", imapBatch.getMessages())), 1, 2);

		Label duplicatesLabel = new Label("<b># of Duplicates:</b>");
		duplicatesLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(duplicatesLabel, 0, 3);
		grid.addComponent(
				new Label(String.format("%,d [%.0f%%]", imapBatch.getDuplicates(), (double)100*imapBatch.getDuplicates()/imapBatch.getMessages())), 1, 3);

		Label failuresLabel = new Label("<b># of Failures:</b>");
		failuresLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(failuresLabel, 0, 4);
		grid.addComponent(
				new Label(String.format("%,d [%.0f%%]", imapBatch.getFailures(), (double)100*imapBatch.getFailures()/imapBatch.getMessages())), 1, 4);


		Label durationLabel = new Label("<b>Duration:</b>");
		durationLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(durationLabel, 0, 5);
		Period p = new Period(new DateTime(imapBatch.getStarted()), new DateTime(imapBatch.getFinished()));

		grid.addComponent(
				new Label(p.toString(minsAndSecsFormatter)),
				1, 5);

		Label hostnameLabel = new Label("<b>Hostname:</b>");
		hostnameLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(hostnameLabel, 0, 6);
		grid.addComponent(
				new Label(imapBatch.getSource().getHostname()),
				1, 6);

		Label msgRateLabel = new Label("<b>Message Rate:</b>");
		msgRateLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(msgRateLabel, 0, 7);
		grid.addComponent(
				new Label(String.format("%.2f per/sec", (double)imapBatch.getMessages()/((imapBatch.getFinished()
						.getTime() - imapBatch.getStarted().getTime()) / 1000))),
				1, 7);

		Label kbRateLabel = new Label("<b>MB Rate:</b>");
		kbRateLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(kbRateLabel, 0, 8);
		grid.addComponent(
				new Label(String.format("%.2f per/sec", (((double)imapBatch.getMessages()/1024)/1024)/((imapBatch.getFinished()
						.getTime() - imapBatch.getStarted().getTime()) / 1000))),
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
				new Label(FileUtils.byteCountToDisplaySize((long) imapBatch
						.getAverageSize())), 3, 1);

		Label averageCompressedSizeLabel = new Label("<b>Avg Zipped Size:</b>");
		averageCompressedSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(averageCompressedSizeLabel, 2, 2);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) imapBatch
						.getCompressedAverageSize())), 3, 2);

		Label medianSizeLabel = new Label("<b>Median Size:</b>");
		medianSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(medianSizeLabel, 2, 3);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) imapBatch
						.getMedianSize())), 3, 3);

		Label medianCompressedSizeLabel = new Label(
				"<b>Median Zipped Size:</b>");
		medianCompressedSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(medianCompressedSizeLabel, 2, 4);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) imapBatch
						.getMedianCompressedSize())), 3, 4);

		Label maxSizeLabel = new Label("<b>Max Size:</b>");
		maxSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(maxSizeLabel, 2, 5);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) imapBatch
						.getMaxSize())), 3, 5);

		Label maxCompressedSizeLabel = new Label("<b>Max Zipped Size:</b>");
		maxCompressedSizeLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(maxCompressedSizeLabel, 2, 6);
		grid.addComponent(
				new Label(FileUtils.byteCountToDisplaySize((long) imapBatch
						.getCompressedMaxSize())), 3, 6);

		Label noAttachmentsLabel = new Label("<b>Count:</b>");
		noAttachmentsLabel.setContentMode(Label.CONTENT_XHTML);
		grid.addComponent(noAttachmentsLabel, 2, 7);
		grid.addComponent(
				new Label(String.format("%,d [%.0f%%]",
						imapBatch.getMessagesWithoutAttachments(), (double)100*imapBatch.getMessagesWithoutAttachments()/imapBatch.getMessages())), 3, 7);
		grid.addComponent(
				new Label(String.format("%,d [%.0f%%]",
						imapBatch.getMessagesWithAttachments(), (double)100*imapBatch.getMessagesWithAttachments()/imapBatch.getMessages())), 4, 7);

		hl.addComponent(grid);
		
		layout.addComponent(hl);

	}

}
