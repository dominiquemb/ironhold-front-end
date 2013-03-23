package com.reqo.ironhold.storage.model.exceptions;

/**
 * User: ilya
 * Date: 3/20/13
 * Time: 10:13 PM
 */
public class MessageExistsException extends Exception {
    public MessageExistsException(String client, String messageId) {
        super("Failed to store message [" + messageId + "] for client [" + client + "] as it already exists");
    }
}
