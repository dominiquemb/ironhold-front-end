package com.reqo.ironhold.storage.model.mixin;

import com.reqo.ironhold.storage.model.Attachment;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * User: ilya
 * Date: 3/2/13
 * Time: 9:20 PM
 */
public abstract class AttachmentMixin {
    @JsonIgnore
    abstract Attachment[] getAttachments();
}
