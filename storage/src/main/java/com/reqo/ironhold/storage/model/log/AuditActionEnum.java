package com.reqo.ironhold.storage.model.log;

/**
 * User: ilya
 * Date: 5/20/13
 * Time: 5:45 PM
 */
public enum AuditActionEnum {
    VIEW("%s viewed this message when searching for \"%s\""), PREVIEW("%s previewed this message when searching for \"%s\""), DOWNLOAD("%s downloaded \"%s\""), SEARCH("%s searched for \"%s\"");

    private final String value;

    AuditActionEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
