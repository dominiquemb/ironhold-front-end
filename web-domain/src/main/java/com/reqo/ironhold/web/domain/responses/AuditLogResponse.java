package com.reqo.ironhold.web.domain.responses;

import com.reqo.ironhold.web.domain.AuditLogMessage;

import java.util.Collection;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 10:05 AM
 */
public class AuditLogResponse {
    private Collection<AuditLogMessage> messages;


    public AuditLogResponse() {

    }

    public AuditLogResponse(Collection<AuditLogMessage> messages) {
        this.messages = messages;
    }

    public Collection<AuditLogMessage> getMessages() {
        return messages;
    }

    public void setMessages(Collection<AuditLogMessage> messages) {
        this.messages = messages;
    }
}
