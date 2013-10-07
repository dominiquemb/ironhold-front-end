package com.reqo.ironhold.storage.model.search;

/**
 * User: ilya
 * Date: 10/6/13
 * Time: 10:53 PM
 */
public enum MessageTypeEnum {
    EMAIL("Email", "EMAIL"), BLOOMBERG_MESSAGE("Bloomberg Message", "BLOOMBERG_MESSAGE"), BLOOMBERG_CHAT("Bloomberg Chat", "BLOOMBERG_CHAT");

    private final String value;
    private final String label;

    MessageTypeEnum(String label, String value) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static MessageTypeEnum getByValue(String value) {
        switch (value.toUpperCase()) {
            case "EMAIL":
                return EMAIL;
            case "BLOOMBERG_MESSAGE":
                return BLOOMBERG_MESSAGE;
            case "BLOOMBERG_CHAT":
                return BLOOMBERG_CHAT;
            default:
                return EMAIL;
        }
    }
}
