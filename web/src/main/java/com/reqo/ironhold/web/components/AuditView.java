package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.storage.model.log.AuditLogMessage;
import com.reqo.ironhold.storage.model.log.LogMessage;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.BloombergSource;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.model.message.source.MessageSource;
import com.reqo.ironhold.storage.model.message.source.PSTMessageSource;
import com.reqo.ironhold.web.IronholdApplication;
import com.reqo.ironhold.web.domain.IndexedMailMessage;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.search.SearchHit;

import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class AuditView extends AbstractEmailView {
    public static final String TIMESTAMP = "Timestamp";
    public static final String HOSTNAME = "Hostname";
    public static final String FOLDER = "Folder";
    public static final String PST_FILE_LOCATION = "PST File";
    public static final String FILE_SIZE = "PST File Size";
    public static final String MESSAGE = "Entry";
    public static final String LEVEL = "Level";
    public static final String USER = "User";
    private static final String IMAP_HOST = "IMAP Host";
    private static final String IMAP_USER = "IMAP Mailbox";
    private static final String BLOOMBERG_SOURCE = "Bloomberg Source";
    private final VerticalLayout layout;

    public AuditView() {
        this.setSizeFull();
        layout = new VerticalLayout();
        layout.setMargin(true);
        this.setContent(layout);
    }

    public synchronized void show(SearchHitPanel newHitPanel, IndexedMailMessage item, String criteria) throws Exception {
        String client = (String) getSession().getAttribute("client");
        layout.removeAllComponents();

        addEmailToolBar(layout, client, item);

        final Label messageId = new Label("MessageId: " + item.getMessageId());
        layout.addComponent(messageId);


        IMimeMailMessageStorageService mimeMailMessageStorageService = ((IronholdApplication) this.getUI()).getMimeMailMessageStorageService();
        MetaDataIndexService metaDataIndexService = ((IronholdApplication) this.getUI()).getMetaDataIndexService();
        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessageFromSource(mimeMailMessageStorageService.get(client, item.getYear(), item.getMonthDay(), item.getMessageId()));
        List<MessageSource> messageSources = metaDataIndexService.getSources(client, item.getMessageId());
        loadIMAPSources(messageSources);
        loadPSTSources(messageSources);
        loadBloombergSources(messageSources);

        final Table auditTable = new Table("Audit Log");
        auditTable.setSizeFull();
        auditTable.setColumnWidth(TIMESTAMP, 150);
        auditTable.setColumnExpandRatio(MESSAGE, 1);

        IndexedContainer auditLogs = new IndexedContainer();
        auditLogs.addContainerProperty(TIMESTAMP, Date.class, "");
        auditLogs.addContainerProperty(MESSAGE, String.class, "");

        int logCount = 0;
        for (AuditLogMessage logMessage : metaDataIndexService.getAuditLogMessages(client, item.getMessageId())) {
            Item logItem = auditLogs.addItem(logCount);
            logCount++;
            logItem.getItemProperty(TIMESTAMP).setValue(logMessage.getTimestamp());
            logItem.getItemProperty(MESSAGE).setValue(String.format(logMessage.getAction().getValue(), logMessage.getLoginUser().getName(), logMessage.getContext()));
        }

        auditTable.setContainerDataSource(auditLogs);


        layout.addComponent(auditTable);

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

        logCount = 0;
        for (LogMessage logMessage : metaDataIndexService.getLogMessages(client, item.getMessageId())) {
            Item logItem = logs.addItem(logCount);
            logCount++;
            logItem.getItemProperty(TIMESTAMP).setValue(logMessage.getTimestamp());
            logItem.getItemProperty(HOSTNAME).setValue(logMessage.getHost());
            logItem.getItemProperty(LEVEL).setValue(logMessage.getLevel().name());
            logItem.getItemProperty(MESSAGE).setValue(logMessage.getMessage());
        }


        logTable.setContainerDataSource(logs);


        layout.addComponent(logTable);

    }

    private void loadPSTSources(List<MessageSource> messageSources) {

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

        if (sourceCount > 0) {
            layout.addComponent(sourcesTable);
        }
    }

    private void loadIMAPSources(List<MessageSource> messageSources) {
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


        if (sourceCount > 0) {
            layout.addComponent(sourcesTable);
        }
    }


    private void loadBloombergSources(List<MessageSource> messageSources) {
        final Table sourcesTable = new Table("Message Source");
        sourcesTable.setSizeFull();
        sourcesTable.setColumnWidth(TIMESTAMP, 150);
        sourcesTable.setColumnWidth(HOSTNAME, 60);
        sourcesTable.setColumnWidth(BLOOMBERG_SOURCE, 100);
        sourcesTable.setColumnExpandRatio(BLOOMBERG_SOURCE, 1);

        IndexedContainer sources = new IndexedContainer();
        sources.addContainerProperty(TIMESTAMP, Date.class, "");
        sources.addContainerProperty(HOSTNAME, String.class, "");
        sources.addContainerProperty(BLOOMBERG_SOURCE, String.class, "");


        int sourceCount = 0;

        for (MessageSource messageSource : messageSources) {
            if (messageSource instanceof BloombergSource) {
                BloombergSource bloombergSource = (BloombergSource) messageSource;
                Item sourceItem = sources.addItem(sourceCount);
                sourceCount++;
                sourceItem.getItemProperty(TIMESTAMP).setValue(bloombergSource.getLoadTimestamp());
                sourceItem.getItemProperty(HOSTNAME).setValue(bloombergSource.getHostname());
                sourceItem.getItemProperty(BLOOMBERG_SOURCE).setValue(bloombergSource.getDescription());
            }
        }


        sourcesTable.setContainerDataSource(sources);
        sourcesTable.setHeight("100px");


        if (sourceCount > 0) {
            layout.addComponent(sourcesTable);
        }
    }

}
