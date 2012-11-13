package com.reqo.ironhold.search;

public enum IndexFieldEnum {
    SUBJECT("pstMessage.subject"), DATE("pstMessage.messageDeliveryTime"), FROM("pstMessage.sentRepresentingName"), TO("pstMessage.displayTo"), CC("pstMessage.displayCc"), SIZE("pstMessage.messageSize"), BODY("pstMessage.body"), ATTACHMENT("pstMessage.attachments.body"), SCORE("_score");

    private final String value;

    IndexFieldEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
