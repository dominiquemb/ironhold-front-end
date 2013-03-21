package com.reqo.ironhold.storage.model.search;

public enum IndexedObjectType {
	MIME_MESSAGE("mimeMessage"), IMAP_MESSAGE_SOURCE("imapMessageSource"), PST_MESSAGE_SOURCE("pstMessageSource"), LOG_MESSAGE("logMessage");

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
            default:
                return null;
        }
	}
}
