package com.reqo.ironhold.web.domain;

import java.util.Date;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 10:08 AM
 */
public class MessagePreview {


    private String messageId;
    private String subject;
    private Date messageDate;
    private String year;
    private String monthDay;
    private Recipient sender;
    private Recipient realSender;
    private Recipient[] to;
    private Recipient[] cc;
    private Recipient[] bcc;
    private long size;
    private String bodyPreview;
    private String importance;
    private String messageType;

    public MessagePreview() {

    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(Date messageDate) {
        this.messageDate = messageDate;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonthDay() {
        return monthDay;
    }

    public void setMonthDay(String monthDay) {
        this.monthDay = monthDay;
    }

    public Recipient getSender() {
        return sender;
    }

    public void setSender(Recipient sender) {
        this.sender = sender;
    }

    public Recipient getRealSender() {
        return realSender;
    }

    public void setRealSender(Recipient realSender) {
        this.realSender = realSender;
    }

    public Recipient[] getTo() {
        return to;
    }

    public void setTo(Recipient[] to) {
        this.to = to;
    }

    public Recipient[] getCc() {
        return cc;
    }

    public void setCc(Recipient[] cc) {
        this.cc = cc;
    }

    public Recipient[] getBcc() {
        return bcc;
    }

    public void setBcc(Recipient[] bcc) {
        this.bcc = bcc;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getBodyPreview() {
        return bodyPreview;
    }

    public void setBodyPreview(String bodyPreview) {
        this.bodyPreview = bodyPreview;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
