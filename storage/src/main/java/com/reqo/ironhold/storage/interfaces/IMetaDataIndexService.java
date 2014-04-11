package com.reqo.ironhold.storage.interfaces;

import com.reqo.ironhold.storage.model.message.source.MessageSource;
import com.reqo.ironhold.storage.model.search.IndexFailure;
import com.reqo.ironhold.web.domain.AuditActionEnum;
import com.reqo.ironhold.web.domain.AuditLogMessage;
import com.reqo.ironhold.web.domain.LogMessage;
import com.reqo.ironhold.web.domain.LoginUser;

import java.util.List;

public interface IMetaDataIndexService {
    void store(String indexPrefix, IndexFailure failure);

    void store(String indexPrefix, MessageSource source);

    List<MessageSource> getSources(String indexPrefix, String messageId);

    void store(String indexPrefix, LogMessage logMessage);

    void store(String indexPrefix, AuditLogMessage auditLogMessage);


    List<AuditLogMessage> getAuditLogMessages(String indexPrefix, String messageId);

    List<AuditLogMessage> getAuditLogMessages(String indexPrefix, LoginUser loginUser, AuditActionEnum action);

    List<LogMessage> getLogMessages(String indexPrefix, String messageId);

    List<IndexFailure> getIndexFailures(String indexPrefix, String criteria, int limit);

}
