package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.model.*;

import java.util.Date;
import java.util.List;

public interface IStorageService {

    List<ExportableMessage> findNewMailMessagesSince(Date date, int limit) throws Exception;

    List<ExportableMessage> findNewMimeMailMessagesSince(Date date, int limit) throws Exception;

    Date getMailMessageUploadDate(String messageId);

    Date getMimeMailMessageUploadDate(String messageId);

    boolean existsMailMessage(String messageId) throws Exception;

    boolean existsMimeMailMessage(String messageId) throws Exception;

    long store(MailMessage mailMessage) throws Exception;

    long store(MimeMailMessage mailMessage) throws Exception;

    void addSource(String messageId, IMAPMessageSource source) throws Exception;

    void addSource(String messageId, PSTMessageSource source) throws Exception;

    void updateIndexStatus(MailMessage message, IndexStatus status)
            throws Exception;

    void updateIndexStatus(MimeMailMessage message, IndexStatus status)
            throws Exception;

    List<MailMessage> findUnindexedPSTMessages(int limit) throws Exception;

    List<MimeMailMessage> findUnindexedIMAPMessages(int limit) throws Exception;

    MailMessage getMailMessage(String messageId) throws Exception;

    MailMessage getMailMessage(String messageId, boolean includeAttachments)
            throws Exception;

    MimeMailMessage getMimeMailMessage(String messageId) throws Exception;

    long getTotalMessageCount();

    void store(LogMessage logMessage) throws Exception;

    List<LogMessage> getLogMessages(String messageId) throws Exception;

    void addPSTFile(PSTFileMeta pstFile) throws Exception;

    List<PSTFileMeta> getPSTFiles() throws Exception;

    void addIMAPBatch(IMAPBatchMeta imapBatchMeta) throws Exception;

    List<IMAPBatchMeta> getIMAPBatches() throws Exception;


}
