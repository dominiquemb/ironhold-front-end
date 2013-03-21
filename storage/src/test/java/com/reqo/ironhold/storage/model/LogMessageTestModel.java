package com.reqo.ironhold.storage.model;

import com.reqo.ironhold.storage.model.log.LogLevel;
import com.reqo.ironhold.storage.model.log.LogMessage;

import java.net.UnknownHostException;
import java.util.UUID;

public class LogMessageTestModel extends CommonTestModel {

	public static LogMessage generate() throws UnknownHostException {
		LogMessage testMessage = new LogMessage();
		testMessage.setHost(df.getName());
		testMessage.setMessage(generateText());
		testMessage.setMessageId(UUID.randomUUID().toString());
		testMessage.setTimestamp(df.getDateBetween(getMinDate(), getMaxDate()));
		testMessage.setLevel(LogLevel.Failure);
		return testMessage;
	}


}
