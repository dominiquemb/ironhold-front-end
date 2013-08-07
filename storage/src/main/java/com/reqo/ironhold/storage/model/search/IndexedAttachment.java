package com.reqo.ironhold.storage.model.search;

import com.reqo.ironhold.storage.model.message.Attachment;
import com.reqo.ironhold.storage.utils.TikaInstance;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.elasticsearch.common.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.*;

public class IndexedAttachment extends Attachment {
    private static Logger logger = Logger.getLogger(IndexedAttachment.class);
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public IndexedAttachment(Attachment sourceAttachment, boolean extractTextFromAttachments) {
        this.setContentType(sourceAttachment.getContentType());
        this.setContentDisposition(sourceAttachment.getContentDisposition());
        this.setCreationTime(sourceAttachment.getCreationTime());
        this.setFileExt(sourceAttachment.getFileExt());
        this.setFileName(sourceAttachment.getFileName());
        this.setModificationTime(sourceAttachment.getModificationTime());
        this.setSize(sourceAttachment.getSize());
        if (extractTextFromAttachments) {
            logger.info("Attempting to extract text from " + getFileName());
            this.setBody(extractText(sourceAttachment.getBody()));
        } else {
            logger.info("Skipping extraction of text from " + getFileName());
            this.setBody(StringUtils.EMPTY);
        }
    }

    private static String extractText(final String body) {

        CompletionService<String> taskCompletionService = new ExecutorCompletionService<String>(
                executorService);

        try {

            Callable<String> task = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    try {
                        byte[] bytes = Base64.decode(body);
                        String parsedContent;

                        // Set the maximum length of strings returned by the parseToString
                        // method, -1 sets no limit
                        parsedContent = TikaInstance.tika().parseToString(
                                new ByteArrayInputStream(bytes), new Metadata(), -1);

                        return parsedContent;
                    } catch (TikaException | IOException e) {
                        logger.warn("Failed to extract characters " + e.getMessage());
                    }

                    return StringUtils.EMPTY;
                }
            };
            Future<String> future = taskCompletionService.submit(task);

            Future<String> result = taskCompletionService.poll(2, TimeUnit.MINUTES);
            if (result == null) {
                if (future.isDone()) {
                    return future.get();
                } else {
                    logger.warn("Extraction task did not return in time, canceling");
                    future.cancel(true);
                    return StringUtils.EMPTY;
                }
            }


            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.warn(e);
        }

        return StringUtils.EMPTY;
    }

    public static IndexedAttachment[] fromArray(Attachment[] sourceAttachments, boolean extractTextFromAttachments) {
        IndexedAttachment[] result = new IndexedAttachment[sourceAttachments.length];
        int counter = 0;
        for (Attachment sourceAttachment : sourceAttachments) {
            result[counter++] = new IndexedAttachment(sourceAttachment, extractTextFromAttachments);
        }

        return result;
    }
}
