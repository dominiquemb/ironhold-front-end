package com.reqo.ironhold.storage.model.mixin;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.reqo.ironhold.storage.model.serializer.CompressedStringDeserializer;
import com.reqo.ironhold.storage.model.serializer.CompressedStringSerializer;

public abstract class CompressedIMAPMailMessage {
	@JsonSerialize(using = CompressedStringSerializer.class)
	abstract String getBody();

	@JsonDeserialize(using = CompressedStringDeserializer.class)
	abstract void setBody(String body);
}
