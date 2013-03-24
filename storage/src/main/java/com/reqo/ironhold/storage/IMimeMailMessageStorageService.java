package com.reqo.ironhold.storage;

public interface IMimeMailMessageStorageService {
    long store(String client, String partition, String messageId, String serializedMailMessage, String checkSum) throws Exception;
    boolean exists(String client, String partition, String messageId) throws Exception;
    String get(String client, String partition, String messageId) throws Exception;
}