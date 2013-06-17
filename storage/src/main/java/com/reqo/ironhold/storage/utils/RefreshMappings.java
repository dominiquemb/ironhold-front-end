package com.reqo.ironhold.storage.utils;

import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * User: ilya
 * Date: 6/17/13
 * Time: 12:11 AM
 */
public class RefreshMappings {
    static {
        System.setProperty("jobname", RefreshMappings.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(RefreshMappings.class);

    @Autowired
    private MiscIndexService miscIndexService;

    @Autowired
    private MetaDataIndexService metaDataIndexService;

    @Autowired
    private MessageIndexService messageIndexService;

    public RefreshMappings() {

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
        RefreshMappings client = context.getBean(RefreshMappings.class);
        client.messageIndexService.forceRefreshMappings(bean.getClient(), true);
        client.metaDataIndexService.forceRefreshMappings(bean.getClient() + "." + MetaDataIndexService.SUFFIX, true);
        client.miscIndexService.forceRefreshMappings(bean.getClient() + "." + MiscIndexService.SUFFIX, false);

        System.exit(1);
    }

    static class Options {
        @Option(name = "-client", usage = "client name", required = true)
        private String client;

        public String getClient() {
            return client;
        }
    }
}
