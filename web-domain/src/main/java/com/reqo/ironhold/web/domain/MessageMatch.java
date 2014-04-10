package com.reqo.ironhold.web.domain;

import com.gs.collections.api.block.function.Function;
import org.apache.commons.lang3.StringUtils;

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
    private String subjectWithHighlights;
    private String attachmentWithHighlights;

    public MessageMatch(FormattedIndexedMailMessage indexedMailMessage, String bodyWithHighlights, String subjectWithHighlights, String attachmentWithHighlights) {
        this.indexedMailMessage = indexedMailMessage;
        this.bodyWithHighlights = bodyWithHighlights;
        this.subjectWithHighlights = subjectWithHighlights;
        this.attachmentWithHighlights = attachmentWithHighlights;
    }

    public String getAttachmentWithHighlights() {
        return attachmentWithHighlights;
    }

    public void setAttachmentWithHighlights(String attachmentWithHighlights) {
        this.attachmentWithHighlights = attachmentWithHighlights;
    }

    public String getSubjectWithHighlights() {
        return subjectWithHighlights;
    }

    public void setSubjectWithHighlights(String subjectWithHighlights) {
        this.subjectWithHighlights = subjectWithHighlights;
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

    public void optimize() {
        if (getBodyWithHighlights().length() > 0) {
            getFormattedIndexedMailMessage().setBody(null);
        }

        for (Attachment attachment : getFormattedIndexedMailMessage().getAttachments()) {
            if (attachment.getBody().length() > 200) {
                attachment.setBody(StringUtils.abbreviate(attachment.getBody(), 200) + "...");
            }
        }

    }
}
