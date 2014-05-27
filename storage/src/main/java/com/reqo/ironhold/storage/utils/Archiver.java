package com.reqo.ironhold.storage.utils;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.web.domain.IndexedMailMessage;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.text.SimpleDateFormat;

/**
 * User: ilya
 * Date: 4/12/13
 * Time: 3:04 PM
 */
public class Archiver {
    static {
        System.setProperty("jobname", Archiver.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(Archiver.class);

    protected SimpleDateFormat dayMonthFormat = new SimpleDateFormat("MMdd");

    @Autowired
    private IndexClient indexClient;

    @Autowired
    private IMimeMailMessageStorageService mimeMailMessageStorageService;

    @Autowired
    private MessageIndexService messageIndexService;

    @Autowired
    private MetaDataIndexService metaDataIndexService;

    public Archiver() {

    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Options bean = new Options();
        CmdLineParser parser = new CmdLineParser(bean);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }

        ApplicationContext context = new ClassPathXmlApplicationContext("utilities.xml");
        Archiver archiver = context.getBean(Archiver.class);
        archiver.run(bean.getClient(), bean.getField(), bean.getValue(), bean.isExecute());
        System.exit(1);
    }

    private void run(String client, String field, String value, boolean execute) throws Exception {
        logger.info("Running in " + (execute ? "archive" : "read-only") + " mode");
        QueryBuilder qb = QueryBuilders.termQuery(field, "\"" + value + "\"");

        int count = 0;

        SearchResponse scrollResp = indexClient.getClient().prepareSearch(client)
                .setSearchType(SearchType.SCAN)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100).execute().actionGet();

        logger.info("Executing scroll request with query: " + qb.toString());
        do {
            scrollResp = indexClient.getClient().prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                IndexedMailMessage indexedMailMessage = new IndexedMailMessage();
                indexedMailMessage = indexedMailMessage.deserialize(hit.getSourceAsString());
                if (!execute) {
                    logger.info("Would archive " + indexedMailMessage.getPartition() + " / " + indexedMailMessage.getMessageId());
                } else {
                    logger.info("Archiving " + indexedMailMessage.getMessageId());
                    mimeMailMessageStorageService.archive(client, indexedMailMessage.getPartition(), dayMonthFormat.format(indexedMailMessage.getMessageDate()), indexedMailMessage.getMessageId());
                    messageIndexService.deleteByField(client, indexedMailMessage.getPartition(), IndexedObjectType.MIME_MESSAGE, "messageId", indexedMailMessage.getMessageId());
                    metaDataIndexService.deleteByField(client, indexedMailMessage.getPartition(), IndexedObjectType.PST_MESSAGE_SOURCE, "messageId", indexedMailMessage.getMessageId());
                    metaDataIndexService.deleteByField(client, indexedMailMessage.getPartition(), IndexedObjectType.IMAP_MESSAGE_SOURCE, "messageId", indexedMailMessage.getMessageId());
                }

                count++;

            }

        } while (scrollResp.getHits().hits().length > 0);


        if (!execute) {
            logger.info(count + " messages would be archived");
        } else {
            logger.info(count + " messages archived");
        }
    }

    static class Options {
        @Option(name = "-client", usage = "client name", required = true)
        private String client;

        @Option(name = "-execute", usage = "perform archiving", required = false)
        private boolean execute;

        @Option(name = "-field", usage = "field to search on", required = false)
        private String field;

        @Option(name = "-value", usage = "field value", required = false)
        private String value;

        String getClient() {
            return client;
        }

        boolean isExecute() {
            return execute;
        }

        String getField() {
            return field;
        }

        String getValue() {
            return value;
        }
    }

}
