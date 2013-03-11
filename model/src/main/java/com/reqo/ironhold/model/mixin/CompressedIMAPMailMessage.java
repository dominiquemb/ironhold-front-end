package com.reqo.ironhold.model.mixin;

import com.reqo.ironhold.model.serializer.CompressedStringDeserializer;
import com.reqo.ironhold.model.serializer.CompressedStringSerializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public abstract class CompressedIMAPMailMessage {
    @JsonSerialize(using = CompressedStringSerializer.class)
    abstract String getBody();

    @JsonDeserialize(using = CompressedStringDeserializer.class)
    abstract void setBody(String body);
}
