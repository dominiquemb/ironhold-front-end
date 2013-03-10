package com.reqo.ironhold.exporter;

import com.reqo.ironhold.model.message.ExportableMessage;
import com.reqo.ironhold.storage.IStorageService;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * User: ilya
 * Date: 3/2/13
 * Time: 10:07 PM
 */
public abstract class MessageExporter {
    private static Logger logger = Logger.getLogger(MessageExporter.class);

    protected final String exportDir;
    protected final int batchSize;
    protected final String client;
    protected final String compression;
    protected final IStorageService storageService;
    protected final int max;
    protected final String recoveryFile;

    public MessageExporter(String exportDir, int batchSize, int max, String client,
                           String compression, String recoveryFile, IStorageService storageService) {
        this.exportDir = exportDir;
        this.batchSize = batchSize;
        this.max = max;
        this.client = client;
        this.compression = compression;
        this.recoveryFile = recoveryFile;
        this.storageService = storageService;


    }

    protected abstract List<ExportableMessage> findNewMessagesSince(Date date, int batchSize) throws Exception;

    protected abstract Date getUploadDate(String messageId) throws Exception;

    protected void start() {

        Calendar c = GregorianCalendar.getInstance();
        c.set(2000, 1, 1);

        Date date = c.getTime();

        File f = new File(recoveryFile);

        if (f.exists()) {
            try {
                date.setTime(Long.parseLong(FileUtils.readFileToString(f)));
                logger.info("Recovering export to " + date.toString());
            } catch (IOException e) {
                logger.warn("Failed to load recovery file", e);
            }
        }
        try {
            int count = 0;
            while (true) {


                List<ExportableMessage> newMessages = findNewMessagesSince(date, batchSize);

                if (count >= max && max > 0) {
                    return;
                }

                if (newMessages.size() == 0) {
                    Thread.sleep(60000);
                } else {
                    logger.info("Exported " + count + " messages");
                    for (ExportableMessage newMessage : newMessages) {


                        String dirName = newMessage.getExportDirName(exportDir, client);
                        FileUtils.forceMkdir(new File(dirName));
                        String filename = newMessage.getExportFileName(compression);
                        compress(new File(dirName + File.separator + filename),
                                newMessage.serializeMessageWithAttachments());
                    }

                    date = getUploadDate(newMessages
                            .get(newMessages.size() - 1).getMessageId());
                    count += newMessages.size();

                    FileUtils.writeStringToFile(f, Long.toString(date.getTime()));
                }
            }
        } catch (Exception e) {
            logger.warn(e);
        }

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
