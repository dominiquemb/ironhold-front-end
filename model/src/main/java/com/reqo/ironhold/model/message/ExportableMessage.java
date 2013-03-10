package com.reqo.ironhold.model.message;

/**
 * User: ilya
 * Date: 3/2/13
 * Time: 10:10 PM
 */
public interface ExportableMessage {
    String serializeMessageWithAttachments() throws Exception;

    ExportableMessage deserializeMessageWithAttachments(String serializedMessage) throws Exception;

    String getExportFileName(String compression);

    String getExportDirName(String exportDir, String client);

    String getMessageId();
}
