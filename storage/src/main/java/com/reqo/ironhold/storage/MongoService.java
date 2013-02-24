package com.reqo.ironhold.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.mongodb.BasicDBObject;
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
import com.reqo.ironhold.storage.model.IMAPBatchMeta;
import com.reqo.ironhold.storage.model.IMAPMessageSource;
import com.reqo.ironhold.storage.model.IndexStatus;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.MimeMailMessage;
import com.reqo.ironhold.storage.model.PSTFileMeta;
import com.reqo.ironhold.storage.model.PSTMessageSource;
import com.reqo.ironhold.storage.utils.Compression;

public class MongoService implements IStorageService {

	private static Logger logger = Logger.getLogger(MongoService.class);
	private static final String MESSAGE_COLLECTION = "messages";
	private static final String LOG_COLLECTION = "logs";
	private static final String PST_COLLECTION = "pstFiles";
	private static final String MIME_MESSAGE_COLLECTION = "mimeMessages";
	private static final String IMAP_COLLECTION = "imapBatches";

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

		List<ServerAddress> hosts = new ArrayList<ServerAddress>();
		for (String eachHost : mongoHost.split(",")) {
			hosts.add(new ServerAddress(eachHost, mongoPort));
		}
		mongo = new Mongo(hosts, options);

		db = mongo.getDB("admin");
		if (!username.equals("${mongo.username}")) {
			db.authenticate(username, password.toCharArray());
			logger.info(String.format(
					"Connected to Mongo at %s:%d, db [%s] as [%s]", mongoHost,
					mongoPort, clientName, username));
		} else {
			logger.info(String
					.format("Connected to Mongo at %s:%d, db [%s] without authentication",
							mongoHost, mongoPort, clientName));
		}
		db = mongo.getDB(clientName);

		createCollectionAndIndexIfRequired(MIME_MESSAGE_COLLECTION,
				"metadata.indexed", true);
		createCollectionAndIndexIfRequired(MESSAGE_COLLECTION,
				"metadata.indexed", true);
		createCollectionAndIndexIfRequired(LOG_COLLECTION, "messageId", false);
	}

	public void createCollectionAndIndexIfRequired(String coll, String index,
			boolean isGridFS) {
		String collection = isGridFS ? coll + ".files" : coll;
		if (db.getCollectionNames().contains(collection)) {
			boolean found = false;
			for (DBObject info : db.getCollection(collection).getIndexInfo()) {

				if (info.containsField("key")) {
					DBObject key = (DBObject) info.get("key");
					if (key.containsField(index) && key.keySet().size() == 1) {
						found = true;
						break;
					}

				}
			}

			if (!found) {
				logger.warn("Missing index found: Collection " + collection
						+ ", Index " + index + " => creating");
				BasicDBObject indexObj = new BasicDBObject();
				indexObj.put(index, 1);
				db.getCollection(collection).ensureIndex(indexObj);
			}
		} else {
			logger.warn("Collection " + collection
					+ " is not present => creating");
			if (isGridFS) {
				GridFS fs = new GridFS(db, coll);
				createCollectionAndIndexIfRequired(coll, index, true);
			} else {
				db.getCollection(collection);
				createCollectionAndIndexIfRequired(coll, index, true);
			}
		}
	}

	public boolean existsMailMessage(String messageId) {

		GridFS fs = new GridFS(db, MESSAGE_COLLECTION);

		return fs.findOne(messageId) != null;
	}

	@Override
	public boolean existsMimeMailMessage(String messageId) throws Exception {
		GridFS fs = new GridFS(db, MIME_MESSAGE_COLLECTION);

		return fs.findOne(messageId) != null;
	}

	public long store(MailMessage mailMessage) throws JsonGenerationException,
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
		mongo.fsync(false);
		return fsFile.getLength();
	}

	public void stopSession() {
		mongo.close();

	}

	public List<MailMessage> findUnindexedPSTMessages(int limit)
			throws JsonParseException, JsonMappingException, IOException {
		logger.debug("Statistics: findUnindexedMessages, phase 1 started");

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

		long started2 = System.currentTimeMillis();
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
		long finished2 = System.currentTimeMillis();

		logger.info(String.format(
				"Statistics: findUnindexedMessages, phase 2 %d", finished2
						- started2));

		logger.info(String.format(
				"Statistics: findUnindexedMessages took %dms", finished2
						- started));
		return result;

	}

	public void addSource(String messageId, PSTMessageSource source)
			throws JsonParseException, JsonMappingException, IOException {
		MailMessage message = getMailMessage(messageId);
		message.addSource(source);

		update(message);

	}

	public void addSource(String messageId, IMAPMessageSource source)
			throws Exception {
		MimeMailMessage message = getMimeMailMessage(messageId);
		message.addSource(source);

		update(message);

	}

	public void updateIndexStatus(MailMessage message, IndexStatus status)
			throws JsonParseException, JsonMappingException, IOException {
		long started = System.currentTimeMillis();

		message.setIndexed(status);

		update(message);
		long finished = System.currentTimeMillis();
		logger.debug(String.format("Statistics: updateIndexStatus %d", finished
				- started));
	}

	public void updateIndexStatus(MimeMailMessage message, IndexStatus status)
			throws JsonParseException, JsonMappingException, IOException {
		long started = System.currentTimeMillis();

		message.setIndexed(status);

		update(message);
		long finished = System.currentTimeMillis();
		logger.debug(String.format("Statistics: updateIndexStatus %d", finished
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
		mongo.fsync(false);
	}

	public void update(MimeMailMessage mailMessage)
			throws JsonGenerationException, JsonMappingException,
			MongoException, IOException {
		GridFS fs = new GridFS(db, MIME_MESSAGE_COLLECTION);
		GridFSDBFile fsFile = fs.findOne(mailMessage.getMessageId());

		String jsonString = MimeMailMessage.serialize(mailMessage);
		fsFile.setMetaData((DBObject) JSON.parse(jsonString));
		fsFile.save();
		mongo.fsync(false);
	}

	@Override
	public long getTotalMessageCount() {
		return db.getCollection(MESSAGE_COLLECTION + ".files").getCount()
				+ db.getCollection(MIME_MESSAGE_COLLECTION + ".files")
						.getCount();
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

	public void store(LogMessage logMessage) throws JsonGenerationException,
			JsonMappingException, MongoException, IOException {
		db.getCollection(LOG_COLLECTION).insert(
				(DBObject) JSON.parse(LogMessage.toJSON(logMessage)));
		mongo.fsync(false);
	}

	public List<LogMessage> getLogMessages(String messageId) throws Exception {
		List<LogMessage> result = new ArrayList<LogMessage>();
		DBObject query = QueryBuilder.start().put("messageId").is(messageId)
				.get();

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
		db.getCollection(PST_COLLECTION).insert(
				(DBObject) JSON.parse(PSTFileMeta.toJSON(pstFile)));
	}

	@Override
	public List<PSTFileMeta> getPSTFiles() throws Exception {
		List<PSTFileMeta> result = new ArrayList<PSTFileMeta>();

		DBCursor cur = db.getCollection(PST_COLLECTION).find();

		while (cur.hasNext()) {
			DBObject object = cur.next();
			object.removeField("_id");
			result.add(PSTFileMeta.fromJSON(object.toString()));
		}

		return result;
	}

	@Override
	public long store(MimeMailMessage mailMessage) throws Exception {
		long started = System.currentTimeMillis();
		try {
			Date storedDate = new Date();
			mailMessage.setStoredDate(storedDate);
			String jsonString = MimeMailMessage.serialize(mailMessage);
			GridFS fs = new GridFS(db, MIME_MESSAGE_COLLECTION);
			String compressedRawContents = Compression.compress(mailMessage
					.getRawContents());
			ByteArrayInputStream bis = new ByteArrayInputStream(
					compressedRawContents.getBytes());
			GridFSInputFile fsFile = fs.createFile(bis,
					mailMessage.getMessageId());

			DBObject metaData = (DBObject) JSON.parse(jsonString);

			fsFile.setMetaData(metaData);
			fsFile.setChunkSize(GridFS.MAX_CHUNKSIZE);
			fsFile.saveChunks();
			fsFile.save();
			mongo.fsync(false);
			return fsFile.getLength();
		} finally {
			long finished = System.currentTimeMillis();
			logger.info("Stored mime message in " + (finished - started)
					+ " ms");
		}
	}

	@Override
	public List<MimeMailMessage> findUnindexedIMAPMessages(int limit)
			throws Exception {
		logger.debug("Statistics: findUnindexedIMAPMessages, phase 1 started");

		long started = System.currentTimeMillis();
		List<MimeMailMessage> result = new ArrayList<MimeMailMessage>();
		DBObject query = QueryBuilder.start().put("metadata.indexed")
				.is(IndexStatus.NOT_INDEXED.toString()).get();

		GridFS fs = new GridFS(db, MIME_MESSAGE_COLLECTION);

		DBCursor cur = fs.getFileList(query).limit(limit);

		List<String> toBeReturned = new ArrayList<String>();

		while (cur.hasNext()) {
			GridFSDBFile object = (GridFSDBFile) cur.next();

			toBeReturned.add(object.getFilename());

		}
		long finished = System.currentTimeMillis();

		logger.debug(String.format("Found %d unindexed messages",
				toBeReturned.size()));
		logger.debug(String.format(
				"Statistics: findUnindexedIMAPMessages, phase 1 %d", finished
						- started));

		started = System.currentTimeMillis();
		for (String fileName : toBeReturned) {
			try {
				result.add(getMimeMailMessage(fileName));
			} catch (Exception e) {
				logger.warn("Failed to retrieve " + fileName, e);
			}
		}
		finished = System.currentTimeMillis();

		logger.debug(String.format(
				"Statistics: findUnindexedIMAPMessages, phase 2 %d", finished
						- started));

		return result;

	}

	@Override
	public MimeMailMessage getMimeMailMessage(String messageId)
			throws Exception {
		GridFS fs = new GridFS(db, MIME_MESSAGE_COLLECTION);

		GridFSDBFile object = fs.findOne(messageId);
		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		object.writeTo(byos);

		MimeMailMessage mailMessage = MimeMailMessage.deserialize(object
				.getMetaData().toString());

		mailMessage.loadMimeMessageFromSource(Compression
				.decompress(new String(byos.toByteArray())));

		return mailMessage;
	}

	@Override
	public void addIMAPBatch(IMAPBatchMeta imapBatchMeta) throws Exception {
		db.getCollection(IMAP_COLLECTION).insert(
				(DBObject) JSON.parse(IMAPBatchMeta.toJSON(imapBatchMeta)));

	}

	@Override
	public List<IMAPBatchMeta> getIMAPBatches() throws Exception {
		List<IMAPBatchMeta> result = new ArrayList<IMAPBatchMeta>();

		DBCursor cur = db.getCollection(IMAP_COLLECTION).find();

		while (cur.hasNext()) {
			DBObject object = cur.next();
			object.removeField("_id");
			result.add(IMAPBatchMeta.fromJSON(object.toString()));
		}

		return result;
	}

}
