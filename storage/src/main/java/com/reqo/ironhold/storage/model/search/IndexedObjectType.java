package com.reqo.ironhold.storage.model.search;

public enum IndexedObjectType {
    MIME_MESSAGE("mimeMessage"), IMAP_MESSAGE_SOURCE("imapMessageSource"), PST_MESSAGE_SOURCE("pstMessageSource"), LOG_MESSAGE("logMessage"),
    AUDIT_LOG_MESSAGE("auditLogMessage"),
    IMAP_BATCH_META("imapBatchMeta"), PST_FILE_META("pstFileMeta"), INDEX_FAILURE("indexFailure"), LOGIN_USER("loginUser"), BLOOMBERG_META("bloombergMeta"), BLOOMBERG_SOURCE("bloombergSource");

    private String value;

    private IndexedObjectType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static IndexedObjectType getByValue(String value) {
        switch (value) {
            case "mimeMessage":
                return MIME_MESSAGE;
            case "imapMessageSource":
                return IMAP_MESSAGE_SOURCE;
            case "pstMessageSource":
                return PST_MESSAGE_SOURCE;
            case "logMessage":
                return LOG_MESSAGE;
            case "auditLogMessage":
                return AUDIT_LOG_MESSAGE;
            case "imapBatchMeta":
                return IMAP_BATCH_META;
            case "pstFileMeta":
                return PST_FILE_META;
            case "indexFailure":
                return INDEX_FAILURE;
            case "bloombergMeta":
                return BLOOMBERG_META;
            case "bloombergSource":
                return BLOOMBERG_SOURCE;
            default:
                return null;
        }
    }
}
