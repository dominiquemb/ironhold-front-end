package com.reqo.ironhold.storage.thrift;

import com.reqo.ironhold.storage.LocalMimeMailMessageStorageService;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * User: ilya
 * Date: 4/2/13
 * Time: 9:04 PM
 */
public class MimeMailMessageStorageServiceHandler implements MimeMailMessageStorage.Iface {

    private static Logger logger = Logger.getLogger(MimeMailMessageStorageServiceHandler.class);

    @Autowired
    private LocalMimeMailMessageStorageService mimeMailMessageStorageService;

    @Override
    public long store(String clientName, String partition, String subPartition, String messageId, String message, String checkSum, boolean encrypt) throws TException {
        try {
            logger.info("Storing " + messageId + " for " + clientName + "/" + partition + "/" + subPartition);
            return mimeMailMessageStorageService.store(clientName, partition, subPartition, messageId, message, checkSum, encrypt);
        } catch (Exception e) {
            throw new TException(e);
        }
    }

    @Override
    public boolean exists(String clientName, String partition, String subPartition, String messageId) throws TException {
        try {
            logger.info("Check for " + messageId + " for " + clientName + "/" + partition + "/" + subPartition);
            return mimeMailMessageStorageService.exists(clientName, partition, subPartition, messageId);
        } catch (Exception e) {
            throw new TException(e);
        }
    }

    @Override
    public String get(String clientName, String partition, String subPartition, String messageId) throws TException {
        try {
            logger.info("Getting " + messageId + " for " + clientName + "/" + partition + "/" + subPartition);
            return mimeMailMessageStorageService.get(clientName, partition, subPartition, messageId);
        } catch (Exception e) {
            throw new TException(e);
        }
    }

    @Override
    public List<String> getPartitions(String clientName) throws TException {
        try {
            logger.info("Getting partitions for " + clientName);
            return mimeMailMessageStorageService.getPartitions(clientName);
        } catch (Exception e) {
            throw new TException(e);
        }
    }

    @Override
    public List<String> getSubPartitions(String clientName, String partition) throws TException {
        try {
            logger.info("Getting sub partitions for " + clientName + "/" + partition);
            return mimeMailMessageStorageService.getSubPartitions(clientName, partition);
        } catch (Exception e) {
            throw new TException(e);
        }
    }

    @Override
    public List<String> getList(String clientName, String partition, String subPartition) throws TException {
        try {
            logger.info("Getting sub partitions for " + clientName + "/" + partition + "/" + subPartition);
            return mimeMailMessageStorageService.getList(clientName, partition, subPartition);
        } catch (Exception e) {
            throw new TException(e);
        }
    }

    @Override
    public boolean archive(String clientName, String partition, String subPartition, String messageId) throws TException {
        try {
            logger.info("Archiving " + messageId + " for " + clientName + "/" + partition + "/" + subPartition);
            return mimeMailMessageStorageService.archive(clientName, partition, subPartition, messageId);
        } catch (Exception e) {
            throw new TException(e);
        }
    }
}
