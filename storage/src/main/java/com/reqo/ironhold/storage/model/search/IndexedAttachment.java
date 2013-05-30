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

public class IndexedAttachment extends Attachment {
    private static Logger logger = Logger.getLogger(IndexedAttachment.class);

    public IndexedAttachment(Attachment sourceAttachment) {
        this.setContentType(sourceAttachment.getContentType());
        this.setContentDisposition(sourceAttachment.getContentDisposition());
        this.setCreationTime(sourceAttachment.getCreationTime());
        this.setFileExt(sourceAttachment.getFileExt());
        this.setFileName(sourceAttachment.getFileName());
        this.setModificationTime(sourceAttachment.getModificationTime());
        this.setSize(sourceAttachment.getSize());
        this.setBody(extractText(sourceAttachment.getBody()));
    }

    private static String extractText(String body) {
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

    public static IndexedAttachment[] fromArray(Attachment[] sourceAttachments) {
        IndexedAttachment[] result = new IndexedAttachment[sourceAttachments.length];
        int counter = 0;
        for (Attachment sourceAttachment : sourceAttachments) {
            result[counter++] = new IndexedAttachment(sourceAttachment);
        }

        return result;
    }
}
