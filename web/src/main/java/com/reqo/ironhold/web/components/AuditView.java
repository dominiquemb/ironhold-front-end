package com.reqo.ironhold.web.components;

import com.reqo.ironhold.model.message.MessageSource;
import com.reqo.ironhold.model.message.eml.IMAPMessageSource;
import com.reqo.ironhold.model.message.pst.PSTMessageSource;
import com.reqo.ironhold.search.model.IndexedObjectType;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.MimeMailMessage;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.search.SearchHit;

import java.util.Date;

@SuppressWarnings("serial")
public class AuditView extends Panel {

    public static final String TIMESTAMP = "Timestamp";
    public static final String HOSTNAME = "Hostname";
    public static final String FOLDER = "Folder";
    public static final String PST_FILE_LOCATION = "PST File";
    public static final String FILE_SIZE = "PST File Size";
    public static final String MESSAGE = "Entry";
    public static final String LEVEL = "Level";
	private static final String IMAP_HOST = "IMAP Host";
	private static final String IMAP_USER = "IMAP Mailbox";
    private final IStorageService storageService;

    public AuditView(IStorageService storageService) {
        this.storageService = storageService;
        this.setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setMargin(true);
        this.setContent(verticalLayout);
    }

    public synchronized void show(SearchHitPanel newHitPanel, SearchHit item, String criteria) throws Exception {

        this.removeAllComponents();


        final Label messageId = new Label("MessageId: " + item.getId());
        this.addComponent(messageId);

        
        
        MessageSource[] messageSources = null;
    	if (item.getType().equals(IndexedObjectType.PST_MESSAGE.getValue())) {
			MailMessage mailMessage = storageService.getMailMessage(item.getId(), true);
			messageSources = mailMessage.getSources();
			loadPSTSources(messageSources);
		} else if (item.getType().equals(
				IndexedObjectType.MIME_MESSAGE.getValue())) {
			MimeMailMessage mailMessage = storageService.getMimeMailMessage(item.getId());
			messageSources = mailMessage.getSources();
			loadIMAPSources(messageSources);
		}



        final Table logTable = new Table("Message Log");
        logTable.setSizeFull();
        logTable.setColumnWidth(TIMESTAMP, 150);
        logTable.setColumnWidth(HOSTNAME, 60);
        logTable.setColumnWidth(LEVEL, 60);
        logTable.setColumnExpandRatio(MESSAGE, 1);

        IndexedContainer logs = new IndexedContainer();
        logs.addContainerProperty(TIMESTAMP, Date.class, "");
        logs.addContainerProperty(HOSTNAME, String.class, "");
        logs.addContainerProperty(LEVEL, String.class, "");
        logs.addContainerProperty(MESSAGE, String.class, "");

        int logCount = 0;
        for (LogMessage logMessage : storageService.getLogMessages(item.getId())) {
            Item logItem = logs.addItem(logCount);
            logCount++;
            logItem.getItemProperty(TIMESTAMP).setValue(logMessage.getTimestamp());
            logItem.getItemProperty(HOSTNAME).setValue(logMessage.getHost());
            logItem.getItemProperty(LEVEL).setValue(logMessage.getLevel());
            logItem.getItemProperty(MESSAGE).setValue(logMessage.getMessage());
        }


        logTable.setContainerDataSource(logs);


        this.addComponent(logTable);

    }

   private void loadPSTSources(MessageSource[] messageSources) {

       final Table sourcesTable = new Table("Message Source");
       sourcesTable.setSizeFull();
       sourcesTable.setColumnWidth(TIMESTAMP, 150);
       sourcesTable.setColumnWidth(HOSTNAME, 60);
       sourcesTable.setColumnWidth(PST_FILE_LOCATION, 100);
       sourcesTable.setColumnExpandRatio(PST_FILE_LOCATION, 1);
       sourcesTable.setColumnWidth(FOLDER, 100);
       sourcesTable.setColumnExpandRatio(FOLDER, 1);
       sourcesTable.setColumnWidth(FILE_SIZE, 100);

       IndexedContainer sources = new IndexedContainer();
       sources.addContainerProperty(TIMESTAMP, Date.class, "");
       sources.addContainerProperty(HOSTNAME, String.class, "");
       sources.addContainerProperty(PST_FILE_LOCATION, String.class, "");
       sources.addContainerProperty(FOLDER, String.class, "");
       sources.addContainerProperty(FILE_SIZE, String.class, "");


       int sourceCount = 0;
       
       for (MessageSource messageSource : messageSources) {
           if (messageSource instanceof PSTMessageSource) {
               PSTMessageSource pstMessageSource = (PSTMessageSource) messageSource;
               Item sourceItem = sources.addItem(sourceCount);
               sourceCount++;
               sourceItem.getItemProperty(TIMESTAMP).setValue(pstMessageSource.getLoadTimestamp());
               sourceItem.getItemProperty(HOSTNAME).setValue(pstMessageSource.getHostname());
               sourceItem.getItemProperty(FOLDER).setValue(pstMessageSource.getFolder());
               sourceItem.getItemProperty(PST_FILE_LOCATION).setValue(pstMessageSource.getPstFileName());
               sourceItem.getItemProperty(FILE_SIZE).setValue(FileUtils.byteCountToDisplaySize(pstMessageSource
                       .getSize()));
           }
       }


       sourcesTable.setContainerDataSource(sources);
       sourcesTable.setHeight("100px");


       this.addComponent(sourcesTable);

   }
   
   private void loadIMAPSources(MessageSource[] messageSources) {

       final Table sourcesTable = new Table("Message Source");
       sourcesTable.setSizeFull();
       sourcesTable.setColumnWidth(TIMESTAMP, 150);
       sourcesTable.setColumnWidth(HOSTNAME, 60);
       sourcesTable.setColumnWidth(IMAP_HOST, 100);
       sourcesTable.setColumnWidth(IMAP_USER, 100);
       sourcesTable.setColumnExpandRatio(IMAP_HOST, 1);
       
       IndexedContainer sources = new IndexedContainer();
       sources.addContainerProperty(TIMESTAMP, Date.class, "");
       sources.addContainerProperty(HOSTNAME, String.class, "");
       sources.addContainerProperty(IMAP_HOST, String.class, "");
       sources.addContainerProperty(IMAP_USER, String.class, "");


       int sourceCount = 0;
       
       for (MessageSource messageSource : messageSources) {
           if (messageSource instanceof IMAPMessageSource) {
        	   IMAPMessageSource imapMessageSource = (IMAPMessageSource) messageSource;
               Item sourceItem = sources.addItem(sourceCount);
               sourceCount++;
               sourceItem.getItemProperty(TIMESTAMP).setValue(imapMessageSource.getLoadTimestamp());
               sourceItem.getItemProperty(HOSTNAME).setValue(imapMessageSource.getHostname());
               sourceItem.getItemProperty(IMAP_HOST).setValue(imapMessageSource.getProtocol() + "://" + imapMessageSource.getImapSource() + ":" + imapMessageSource.getImapPort());
               sourceItem.getItemProperty(IMAP_USER).setValue(imapMessageSource.getUsername());
           }
       }


       sourcesTable.setContainerDataSource(sources);
       sourcesTable.setHeight("100px");


       this.addComponent(sourcesTable);

   }

}
