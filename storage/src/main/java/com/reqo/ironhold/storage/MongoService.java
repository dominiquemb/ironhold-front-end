package com.reqo.ironhold.storage;

import com.mongodb.*;
import com.mongodb.util.JSON;
import com.reqo.ironhold.storage.model.LogMessage;
import com.reqo.ironhold.storage.model.MailMessage;
import com.reqo.ironhold.storage.model.MessageSource;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class MongoService implements IStorageService {

    private static Logger logger = Logger.getLogger(MongoService.class);
    private static final String MESSAGE_COLLECTION = "messages";
    private static final String LOG_COLLECTION = "logs";

    private Mongo mongo;
    private DB db;

    /**
     * Used for testing
     *
     * @param mongo
     * @param db
     */
    protected MongoService(Mongo mongo, DB db) {
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
        long maxAutoConnectRetry = Long.parseLong(prop.getProperty("maxautoconnect"));

        MongoOptions options = new MongoOptions();
        options.setAutoConnectRetry(true);
        options.setDescription(purpose);
        options.setMaxAutoConnectRetryTime(maxAutoConnectRetry);
        mongo = new Mongo(new ServerAddress(mongoHost, mongoPort), options);


        db = mongo.getDB("admin");
        db.authenticate(username, password.toCharArray());
        db = mongo.getDB(clientName);
        logger.info(String.format("Connected to Mongo at %s:%d, db [%s] as [%s]", mongoHost, mongoPort, clientName,
                username));
    }

    public boolean exists(String messageId) {

        BasicDBObject query = new BasicDBObject();

        query.put("messageId", messageId);
        return db.getCollection(MESSAGE_COLLECTION).count(query) > 0;
    }

    public void store(MailMessage mailMessage) throws JsonGenerationException, JsonMappingException, MongoException,
            IOException {
        Date storedDate = new Date();
        mailMessage.setStoredDate(storedDate);
        String jsonString = MailMessage.toJSON(mailMessage);
        db.getCollection(MESSAGE_COLLECTION).insert((DBObject) JSON.parse(jsonString));
    }

    public void stopSession() {
        mongo.close();

    }

    public List<MailMessage> findUnindexedMessages(int limit) throws JsonParseException, JsonMappingException,
            IOException {
        List<MailMessage> result = new ArrayList<MailMessage>();
        DBObject query = QueryBuilder.start().put("indexed").is(false).get();

        DBCursor cur = db.getCollection(MESSAGE_COLLECTION).find(query).limit(limit);

        while (cur.hasNext()) {
            DBObject object = cur.next();
            object.removeField("_id");
            result.add(MailMessage.fromJSON(object.toString()));
        }

        return result;

    }

    public void addSource(String messageId, MessageSource source) throws JsonParseException, JsonMappingException,
            IOException {
        MailMessage message = getMailMessage(messageId, true);
        message.addSource(source);

        update(message);

    }

    public void markAsIndexed(String messageId) throws JsonParseException, JsonMappingException, IOException {
        MailMessage message = getMailMessage(messageId, true);
        message.setIndexed(true);

        update(message);
    }

    public void update(MailMessage mailMessage) throws JsonGenerationException, JsonMappingException, MongoException,
            IOException {
        BasicDBObject query = new BasicDBObject();

        query.put("messageId", mailMessage.getMessageId());
        db.getCollection(MESSAGE_COLLECTION).update(query, (DBObject) JSON.parse(MailMessage.toJSON(mailMessage)));

    }

    public long getTotalMessageCount() {
        return db.getCollection(MESSAGE_COLLECTION).getCount();
    }

    public MailMessage getMailMessage(String messageId) throws JsonParseException, JsonMappingException, IOException {
        return getMailMessage(messageId, false);
    }

    public MailMessage getMailMessage(String messageId, boolean includeAttachments) throws JsonParseException,
            JsonMappingException, IOException {
        BasicDBObject query = new BasicDBObject();

        query.put("messageId", messageId);
        DBObject result = null;
        if (!includeAttachments) {
            DBObject fields = BasicDBObjectBuilder.start().add("body", 1).add("cc", 1).add("from",
                    1).add("messageId", 1).add("recievedDate", 1).add("subject", 1).add("to", 1).add("bcc",
                    1).add("indexed", 1).add("sources", 1).add("storedDate", 1).get();
            result = db.getCollection(MESSAGE_COLLECTION).findOne(query, fields);
        } else {
            result = db.getCollection(MESSAGE_COLLECTION).findOne(query);
        }
        result.removeField("_id");
        String match = result.toString();
        return MailMessage.fromJSON(match);
    }

    public void log(LogMessage logMessage) throws JsonGenerationException, JsonMappingException, MongoException,
            IOException {
        db.getCollection(LOG_COLLECTION).insert((DBObject) JSON.parse(LogMessage.toJSON(logMessage)));
    }

    public List<LogMessage> getLogMessages(String messageId) throws Exception {
        List<LogMessage> result = new ArrayList<LogMessage>();
        DBObject query = QueryBuilder.start().put("messageId").is(messageId).get();

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
    public long getTotalCount() {
        return db.getCollection(MESSAGE_COLLECTION).count();
    }

}
