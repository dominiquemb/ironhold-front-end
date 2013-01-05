package com.reqo.ironhold.search.model;

public enum IndexedObjectType {
	MIME_MESSAGE("mimeMessage"), PST_MESSAGE("message");

	private String value;

	private IndexedObjectType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static IndexedObjectType getByValue(String value) {
		if (value.equals(IndexedObjectType.MIME_MESSAGE.getValue())) {
			return IndexedObjectType.MIME_MESSAGE;
		} else if (value.equals(IndexedObjectType.PST_MESSAGE.getValue())) {
			return IndexedObjectType.PST_MESSAGE;
		}
		return IndexedObjectType.PST_MESSAGE;
	}
}
