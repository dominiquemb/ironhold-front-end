package com.reqo.ironhold.web.domain;

import com.gs.collections.api.block.function.Function;

/**
 * User: ilya
 * Date: 11/25/13
 * Time: 10:16 PM
 */
public class MessageMatch {
    public static final Function<MessageMatch, FormattedIndexedMailMessage> TO_INDEXEDMAILMESSAGE = new Function<MessageMatch, FormattedIndexedMailMessage>() {
        @Override
        public FormattedIndexedMailMessage valueOf(MessageMatch messageMatch) {
            return messageMatch.getFormattedIndexedMailMessage();
        }
    };

    private FormattedIndexedMailMessage indexedMailMessage;
    private String bodyWithHighlights;
    private String subjectWithHiglights;
    private String attachmentWithHighlights;

    public MessageMatch(FormattedIndexedMailMessage indexedMailMessage, String bodyWithHighlights, String subjectWithHiglights, String attachmentWithHighlights) {
        this.indexedMailMessage = indexedMailMessage;
        this.bodyWithHighlights = bodyWithHighlights;
        this.subjectWithHiglights = subjectWithHiglights;
        this.attachmentWithHighlights = attachmentWithHighlights;
    }

    public String getAttachmentWithHighlights() {
        return attachmentWithHighlights;
    }

    public void setAttachmentWithHighlights(String attachmentWithHighlights) {
        this.attachmentWithHighlights = attachmentWithHighlights;
    }

    public String getSubjectWithHiglights() {
        return subjectWithHiglights;
    }

    public void setSubjectWithHiglights(String subjectWithHiglights) {
        this.subjectWithHiglights = subjectWithHiglights;
    }

    public String getBodyWithHighlights() {
        return bodyWithHighlights;
    }

    public void setBodyWithHighlights(String bodyWithHighlights) {
        this.bodyWithHighlights = bodyWithHighlights;
    }

    public FormattedIndexedMailMessage getFormattedIndexedMailMessage() {
        return indexedMailMessage;
    }

    public void setFormattedIndexedMailMessage(FormattedIndexedMailMessage indexedMailMessage) {
        this.indexedMailMessage = indexedMailMessage;
    }
}
