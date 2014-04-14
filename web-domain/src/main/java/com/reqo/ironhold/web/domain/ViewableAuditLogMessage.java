package com.reqo.ironhold.web.domain;

import com.gs.collections.api.block.function.Function;

import java.text.SimpleDateFormat;

/**
 * User: ilya
 * Date: 4/11/14
 * Time: 8:51 AM
 */
public class ViewableAuditLogMessage {
    private final String description;
    private final AuditLogMessage auditLogMessage;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY HH:mm");

    public static Function<ViewableAuditLogMessage, Comparable> SORT_BY_CONTEXT = new Function<ViewableAuditLogMessage, Comparable>() {
        @Override
        public Comparable valueOf(ViewableAuditLogMessage auditLogMessage) {
            return auditLogMessage.getAuditLogMessage().getContext();
        }
    };


    public static final Function<AuditLogMessage, ViewableAuditLogMessage> FROM_AUDIT_LOG_MESSAGE = new Function<AuditLogMessage, ViewableAuditLogMessage>() {
        @Override
        public ViewableAuditLogMessage valueOf(AuditLogMessage logMessage) {
            return new ViewableAuditLogMessage(logMessage);
        }
    };


    public ViewableAuditLogMessage(AuditLogMessage logMessage) {
        this.auditLogMessage = logMessage;
        this.description = String.format(logMessage.getAction().getValue(), logMessage.getLoginUser().getName(), logMessage.getContext());
    }

    public AuditLogMessage getAuditLogMessage() {
        return auditLogMessage;
    }

    public String getDescription() {
        return description;
    }

    public String getTimestamp() {
        return dateFormat.format(this.auditLogMessage.getTimestamp());
    }
}
