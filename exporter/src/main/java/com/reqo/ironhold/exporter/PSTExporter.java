package com.reqo.ironhold.exporter;

import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.MailMessage;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class PSTExporter {
    static {
        System.setProperty("jobname", PSTExporter.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(PSTExporter.class);
    protected SimpleDateFormat yearFormat = new SimpleDateFormat("YYYY");

    private final String exportDir;
    private final int batchSize;
    private final String client;
    private final String compression;
    private final IStorageService storageService;
    private final int max;

    public PSTExporter(String exportDir, int batchSize, int max, String client,
                       String compression, IStorageService storageService) {
        this.exportDir = exportDir;
        this.batchSize = batchSize;
        this.max = max;
        this.client = client;
        this.compression = compression;
        this.storageService = storageService;

    }

    public static void main(String[] args) {
        Options bean = new Options();
        CmdLineParser parser = new CmdLineParser(bean);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }
        try {
            IStorageService storageService = new MongoService(bean.getClient(),
                    "PSTExporter");
            PSTExporter exporter = new PSTExporter(bean.getData(),
                    bean.getBatchSize(), bean.getMax(), bean.getClient(),
                    bean.getCompression(), storageService);
            exporter.start();
        } catch (Exception e) {
            logger.error("Critical error detected. Exiting.", e);
            System.exit(0);
        }
    }

    protected void start() {

        Calendar c = GregorianCalendar.getInstance();
        c.set(2000, 1, 1);
        Date date = c.getTime();
        try {
            int count = 0;
            while (true) {

                List<MailMessage> newMessages = storageService
                        .findNewMailMessagesSince(date, batchSize);

                if (newMessages.size() > 0 && count < max) {
                    logger.info("Exported " + count + " messages");
                    for (MailMessage newMessage : newMessages) {
                        String dirName = exportDir
                                + File.separator
                                + client
                                + File.separator
                                + yearFormat.format(newMessage.getPstMessage()
                                .getMessageDeliveryTime());
                        FileUtils.forceMkdir(new File(dirName));
                        String filename = dirName
                                + File.separator
                                + getExportFileName(newMessage.getMessageId())
                                + "." + compression;
                        compress(new File(filename),
                                MailMessage.serializeMailMessage(newMessage));
                        logger.info("Exporting to " + filename);
                    }

                    date = storageService.getUploadDate(newMessages
                            .get(newMessages.size() - 1));
                    count += newMessages.size();
                } else {
                    return;
                }
            }
        } catch (Exception e) {
            logger.warn(e);
        }

    }

    protected String getExportFileName(String messageId) {
        return messageId.replaceAll("\\W+",
                "_");
    }

    protected void compress(File file, String contents)
            throws CompressorException, IOException, InterruptedException {

        if (!compression.equals("NONE")) {
            CompressorOutputStream compressedStream = null;
            try {
                compressedStream = new CompressorStreamFactory()
                        .createCompressorOutputStream(compression,
                                new FileOutputStream(file));

                compressedStream.write(contents.getBytes());
            } finally {
                if (compressedStream != null)
                    compressedStream.close();
            }
        } else {
            FileUtils.writeStringToFile(file, contents);
        }
    }
}
