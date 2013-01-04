package com.reqo.ironhold.storage;

import java.util.List;

import com.reqo.ironhold.storage.model.IndexStatus;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.MessageSource;
import com.reqo.ironhold.storage.model.MimeMailMessage;
import com.reqo.ironhold.storage.model.PSTFileMeta;

public interface IStorageService {

	boolean exists(String messageId) throws Exception;

	void store(MailMessage mailMessage) throws Exception;

	void store(MimeMailMessage mailMessage) throws Exception;

	void addSource(String messageId, MessageSource source) throws Exception;

	void updateIndexStatus(String messageId, IndexStatus status)
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

}
