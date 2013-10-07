package com.reqo.ironhold.storage.es;

public enum IndexFieldEnum {
    SUBJECT("Subject", "subject"), DATE("Date", "messageDate"), YEAR("Year",
            "year"), MONTH_DAY("MonthDay", "monthDay"), FROM_NAME("From", "sender.name"), FROM_ADDRESS("From",
            "sender.address"), FROM_DOMAIN("From Domain", "sender.domain"), TO_NAME(
            "To", "to.name"), TO_ADDRESS("To", "to.address"), TO_DOMAIN(
            "To Domain", "to.domain"), CC_NAME("CC", "cc.name"), CC_ADDRESS(
            "CC", "cc.address"), CC_DOMAIN("CC Domain", "cc.domain"), BCC_NAME("BCC", "bcc.name"), BCC_ADDRESS(
            "BCC", "bcc.address"), BCC_DOMAIN("BCC Domain", "bcc.domain"), SIZE(
            "Size", "size"), BODY("Body", "body"), FILENAME("Filename",
            "attachments.fileName"), FILEEXT("File Ext", "attachments.fileExt"), ATTACHMENT(
            "Attachment", "attachments.body"), SCORE("Score", "_score"), IMPORTANCE("Importance", "importance"), MSGTYPE("Message Type", "messageType");

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
