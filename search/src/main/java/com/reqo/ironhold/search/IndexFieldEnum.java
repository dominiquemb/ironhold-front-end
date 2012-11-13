package com.reqo.ironhold.search;

public enum IndexFieldEnum {
    SUBJECT("Subject", "subject"), DATE("Date", "messageDate"), FROM_NAME("From", "sender.name"),
    FROM_ADDRESS("From", "sender.address"),
    TO_NAME("To", "to.name"), TO_ADDRESS("To", "to.address"), CC_NAME("CC", "cc.name"), CC_ADDRESS("CC",
            "cc.address"), SIZE("Size", "size"), BODY("Body", "body"),
    FILENAME("Filename", "attachments.fileName"), ATTACHMENT("Attachment", "attachments.body"), SCORE("Score",
            "_score");


    private final String value;
    private final String label;

    IndexFieldEnum(String label, String value) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
