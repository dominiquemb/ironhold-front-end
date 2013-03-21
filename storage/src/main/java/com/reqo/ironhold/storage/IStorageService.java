package com.reqo.ironhold.storage;

import com.reqo.ironhold.storage.model.ExportableMessage;
import com.reqo.ironhold.storage.model.IndexStatus;
import com.reqo.ironhold.storage.model.log.LogMessage;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.model.message.source.PSTMessageSource;
import com.reqo.ironhold.storage.model.metadata.IMAPBatchMeta;
import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;

import java.util.Date;
import java.util.List;

public interface IStorageService {

    List<ExportableMessage> findNewMimeMailMessagesSince(Date date, int limit) throws Exception;

    Date getMimeMailMessageUploadDate(String messageId);



    void addSource(String messageId, IMAPMessageSource source) throws Exception;

    void addSource(String messageId, PSTMessageSource source) throws Exception;

    void updateIndexStatus(MimeMailMessage message, IndexStatus status)
            throws Exception;

    List<MimeMailMessage> findUnindexedIMAPMessages(int limit) throws Exception;


    void store(LogMessage logMessage) throws Exception;

    List<LogMessage> getLogMessages(String messageId) throws Exception;

    void addPSTFile(PSTFileMeta pstFile) throws Exception;

    List<PSTFileMeta> getPSTFiles() throws Exception;

    void addIMAPBatch(IMAPBatchMeta imapBatchMeta) throws Exception;

    List<IMAPBatchMeta> getIMAPBatches() throws Exception;


}
