package com.reqo.ironhold.storage;

import java.util.List;

import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MailMessage;

public interface IStorageService {

	boolean exists(String messageId) throws Exception;
	void store(MailMessage mailMessage) throws Exception;

	void addSource(String messageId, String source) throws Exception;
	void markAsIndexed(String messageId) throws Exception;

	List<MailMessage> findUnindexedMessages(int limit) throws Exception;
	
	MailMessage getMailMessage(String messageId) throws Exception;
	MailMessage getMailMessage(String messageId, boolean includeAttachments) throws Exception;
	
	long getTotalMessageCount(); 
	
	void log(LogMessage logMessage) throws Exception;
	
	List<LogMessage> getLogMessages(String messageId) throws Exception;

}
