package com.reqo.ironhold.model.message;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Arrays;

/**
 * User: ilya
 * Date: 3/10/13
 * Time: 3:44 PM
 */
public abstract class IMessage {
    private MessageSource[] sources;

    private Attachment[] attachments = new Attachment[0];
    @JsonIgnore
    private boolean hasAttachments;


    /*** Source handling ***/
    public void addSource(MessageSource source) {
        if (sources == null) {
            sources = new MessageSource[]{source};
        } else {
            MessageSource[] copy = Arrays.copyOf(sources, sources.length + 1);
            copy[sources.length] = source;
            sources = copy;
        }
    }

    public MessageSource[] getSources() {
        return sources;
    }

    /*** Attachment handling ***/
    public void addAttachment(Attachment attachment) {
        Attachment[] copy = Arrays.copyOf(attachments, attachments.length + 1);
        copy[attachments.length] = attachment;
        attachments = copy;
        hasAttachments = true;

    }

    public Attachment[] getAttachments() {
        return attachments;
    }

    public void setAttachments(Attachment[] attachments) {
        this.attachments = attachments.clone();
        hasAttachments = attachments != null && attachments.length > 0;
    }

    public void removeAttachments() {
        this.attachments = new Attachment[0];
        this.hasAttachments = false;
    }

    public boolean isHasAttachments() {
        return hasAttachments;
    }

    public void setHasAttachments(boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }

}
