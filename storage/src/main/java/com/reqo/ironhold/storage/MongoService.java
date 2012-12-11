package com.reqo.ironhold.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elasticsearch.env.Environment;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.QueryBuilder;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.util.JSON;
import com.reqo.ironhold.storage.model.Attachment;
import com.reqo.ironhold.storage.model.IndexStatus;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.MessageSource;
import com.reqo.ironhold.storage.model.PSTFileMeta;

public class MongoService implements IStorageService {

	private static Logger logger = Logger.getLogger(MongoService.class);
	private static final String MESSAGE_COLLECTION = "messages";
	private static final String LOG_COLLECTION = "logs";
	private static final String PST_COLLECTION = "pstFiles";

	private Mongo mongo;
	private DB db;

	/**
	 * Used for testing
	 * 
	 * @param mongo
	 * @param db
	 */
	public MongoService(Mongo mongo, DB db) {
		this.mongo = mongo;
		this.db = db;
	}

	public MongoService(String clientName, String purpose) throws IOException {
		Properties prop = new Properties();
		prop.load(MongoService.class.getResourceAsStream("mongodb.properties"));

		String mongoHost = prop.getProperty("host");
		int mongoPort = Integer.parseInt(prop.getProperty("port"));
		String username = prop.getProperty("username");
		String password = prop.getProperty("password");
		long maxAutoConnectRetry = Long.parseLong(prop
				.getProperty("maxautoconnect"));

		MongoOptions options = new MongoOptions();
		options.setAutoConnectRetry(true);
		options.setDescription(purpose);
		options.setMaxAutoConnectRetryTime(maxAutoConnectRetry);
		mongo = new Mongo(new ServerAddress(mongoHost, mongoPort), options);

		db = mongo.getDB("admin");
		db.authenticate(username, password.toCharArray());
		db = mongo.getDB(clientName);
		logger.info(String.format(
				"Connected to Mongo at %s:%d, db [%s] as [%s]", mongoHost,
				mongoPort, clientName, username));

	}

	public boolean exists(String messageId) {

		GridFS fs = new GridFS(db, MESSAGE_COLLECTION);

		return fs.findOne(messageId) != null;
	}

	public void store(MailMessage mailMessage) throws JsonGenerationException,
			JsonMappingException, MongoException, IOException {
		Date storedDate = new Date();
		mailMessage.setStoredDate(storedDate);
		String jsonString = MailMessage
				.serializeCompressedMailMessage(mailMessage);
		GridFS fs = new GridFS(db, MESSAGE_COLLECTION);
		String attachmentsString = MailMessage
				.serializeCompressedAttachments(mailMessage.getAttachments());
		ByteArrayInputStream bis = new ByteArrayInputStream(
				attachmentsString.getBytes());
		GridFSInputFile fsFile = fs.createFile(bis, mailMessage.getMessageId());

		DBObject metaData = (DBObject) JSON.parse(jsonString);

		fsFile.setMetaData(metaData);
		fsFile.setChunkSize(GridFS.MAX_CHUNKSIZE);
		fsFile.saveChunks();
		fsFile.save();
	}

	public void stopSession() {
		mongo.close();

	}

	public List<MailMessage> findUnindexedMessages(int limit)
			throws JsonParseException, JsonMappingException, IOException {
		logger.info("Statistics: findUnindexedMessages, phase 1 started");

		long started = System.currentTimeMillis();
		List<MailMessage> result = new ArrayList<MailMessage>();
		DBObject query = QueryBuilder.start().put("metadata.indexed")
				.is(IndexStatus.NOT_INDEXED.toString()).get();

		GridFS fs = new GridFS(db, MESSAGE_COLLECTION);

		DBCursor cur = fs.getFileList(query).limit(limit);

		List<String> toBeReturned = new ArrayList<String>();

		while (cur.hasNext()) {
			GridFSDBFile object = (GridFSDBFile) cur.next();

			toBeReturned.add(object.getFilename());

		}
		long finished = System.currentTimeMillis();

		logger.info(String.format(
				"Statistics: findUnindexedMessages, phase 1 %d", finished
						- started));

		started = System.currentTimeMillis();
		for (String fileName : toBeReturned) {

			GridFSDBFile object = fs.findOne(fileName);
			MailMessage mailMessage = MailMessage
					.deserializeCompressedMailMessage(object.getMetaData()
							.toString());
			ByteArrayOutputStream byos = new ByteArrayOutputStream();
			object.writeTo(byos);

			Attachment[] attachments = MailMessage
					.deserializeCompressedAttachments(new String(byos
							.toByteArray()));
			mailMessage.setAttachments(attachments);
			result.add(mailMessage);

		}
		finished = System.currentTimeMillis();

		logger.info(String.format(
				"Statistics: findUnindexedMessages, phase 2 %d", finished
						- started));
		return result;

	}

	public void addSource(String messageId, MessageSource source)
			throws JsonParseException, JsonMappingException, IOException {
		MailMessage message = getMailMessage(messageId);
		message.addSource(source);

		update(message);

	}

	public void updateIndexStatus(String messageId, IndexStatus status)
			throws JsonParseException, JsonMappingException, IOException {
		long started = System.currentTimeMillis();

		MailMessage message = getMailMessage(messageId);
		message.setIndexed(status);

		update(message);
		long finished = System.currentTimeMillis();
		logger.info(String.format("Statistics: updateIndexStatus %d", finished
				- started));
	}

	public void update(MailMessage mailMessage) throws JsonGenerationException,
			JsonMappingException, MongoException, IOException {
		GridFS fs = new GridFS(db, MESSAGE_COLLECTION);
		GridFSDBFile fsFile = fs.findOne(mailMessage.getMessageId());

		String jsonString = MailMessage
				.serializeCompressedMailMessage(mailMessage);
		fsFile.setMetaData((DBObject) JSON.parse(jsonString));
		fsFile.save();

	}

	@Override
	public long getTotalMessageCount() {
		return db.getCollection(MESSAGE_COLLECTION + ".files").getCount();
	}

	public MailMessage getMailMessage(String messageId)
			throws JsonParseException, JsonMappingException, IOException {
		return getMailMessage(messageId, false);
	}

	public MailMessage getMailMessage(String messageId,
			boolean includeAttachments) throws JsonParseException,
			JsonMappingException, IOException {
		GridFS fs = new GridFS(db, MESSAGE_COLLECTION);
		GridFSDBFile fsFile = fs.findOne(messageId);
		String compressedMessage = fsFile.getMetaData().toString();
		MailMessage matchMessage = MailMessage
				.deserializeCompressedMailMessage(compressedMessage);

		if (includeAttachments) {

			ByteArrayOutputStream byos = new ByteArrayOutputStream();
			fsFile.writeTo(byos);

			Attachment[] attachments = MailMessage
					.deserializeCompressedAttachments(new String(byos
							.toByteArray()));
			matchMessage.setAttachments(attachments);

		}

		return matchMessage;
	}

	public void log(LogMessage logMessage) throws JsonGenerationException,
			JsonMappingException, MongoException, IOException {
		db.getCollection(LOG_COLLECTION).insert(
				(DBObject) JSON.parse(LogMessage.toJSON(logMessage)));
	}

	public List<LogMessage> getLogMessages(String messageId) throws Exception {
		List<LogMessage> result = new ArrayList<LogMessage>();
		DBObject query = QueryBuilder.start().put("messageId").is(messageId)
				.get();

		logger.info(query.toString());
		DBCursor cur = db.getCollection(LOG_COLLECTION).find(query);

		while (cur.hasNext()) {
			DBObject object = cur.next();
			object.removeField("_id");
			result.add(LogMessage.fromJSON(object.toString()));
		}

		return result;
	}

	@Override
	public void addPSTFile(PSTFileMeta pstFile) throws Exception {
		db.getCollection(PST_COLLECTION).insert((DBObject) JSON.parse(PSTFileMeta.toJSON(pstFile)));
	}
}
