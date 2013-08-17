package com.reqo.ironhold.storage;

import org.apache.thrift.TException;

import java.util.List;

public interface IMimeMailMessageStorageService {
    long store(String client, String partition, String subPartition, String messageId, String serializedMailMessage, String checkSum, boolean encrypt) throws Exception;

    boolean exists(String client, String partition, String subPartition, String messageId) throws Exception;

    boolean isEncrypted(String client, String partition, String subPartition, String messageId) throws Exception;

    String get(String client, String partition, String subPartition, String messageId) throws Exception;

    List<String> getPartitions(String clientName) throws TException, Exception;

    List<String> getSubPartitions(String clientName, String partition) throws Exception;

    List<String> getList(String clientName, String partition, String subPartition) throws Exception;

    boolean archive(String clientName, String partition, String subPartition, String messageId) throws Exception;
}
