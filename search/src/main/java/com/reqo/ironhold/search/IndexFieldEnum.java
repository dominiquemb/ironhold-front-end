package com.reqo.ironhold.search;

public enum IndexFieldEnum {
    SUBJECT("subject"), DATE("messageDate"), FROM("sender.name"), TO("to.name"), CC("cc.name"), SIZE("size"), BODY("body"), ATTACHMENT("attachments.body"), SCORE("_score");

    private final String value;

    IndexFieldEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
