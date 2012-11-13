package com.reqo.ironhold.web.components;

import com.reqo.ironhold.search.IndexFieldEnum;
import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.search.IndexUtils;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MessageSource;
import com.reqo.ironhold.storage.model.PSTMessageSource;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
    private final IStorageService storageService;
    private final IndexService indexService;
    private final AuditView me;
    private SearchHitPanel currentHitPanel;

    public AuditView(IStorageService storageService, IndexService indexService) {
        this.storageService = storageService;
        this.indexService = indexService;
        this.setSizeFull();

        this.me = this;
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setMargin(true);
        this.setContent(verticalLayout);
    }

    public synchronized void show(SearchHitPanel newHitPanel, SearchHit item, String criteria) throws Exception {

        this.removeAllComponents();


        final Label messageId = new Label("MessageId: " + item.getId());
        this.addComponent(messageId);

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
        for (MessageSource messageSource : storageService.getMailMessage(item.getId()).getSources()) {
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

    private void addPartyLabel(SearchHit item, IndexFieldEnum field) {
        String value = IndexUtils.getFieldValue(item, field, false);
        if (!value.equals(StringUtils.EMPTY)) {
            final Label from = new Label(String.format("%s: %s", field.getLabel(), value));
            from.setContentMode(Label.CONTENT_XHTML);
            this.addComponent(from);
        }

    }

}
