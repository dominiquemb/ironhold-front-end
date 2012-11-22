package com.reqo.ironhold.storage.model.mixin;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.reqo.ironhold.storage.model.serializer.CompressedStringDeserializer;
import com.reqo.ironhold.storage.model.serializer.CompressedStringSerializer;

public abstract class CompressedPSTMessageMixin {
	@JsonIgnore
	abstract int getMessageRecipMe();

	@JsonIgnore
	abstract String[] getColorCategories();

	@JsonIgnore
	abstract String getItemsString();

	@JsonSerialize(using = CompressedStringSerializer.class)
	abstract String getBody();

	@JsonSerialize(using = CompressedStringSerializer.class)
	abstract String getBodyHTML();

	@JsonSerialize(using = CompressedStringSerializer.class)
	abstract String getRtfbody();

	@JsonDeserialize(using = CompressedStringDeserializer.class)
	abstract void setBody(String body);

	@JsonDeserialize(using = CompressedStringDeserializer.class)
	abstract void setBodyHTML(String bodyHTML);

	@JsonDeserialize(using = CompressedStringDeserializer.class)
	abstract void setRtfbody(String rtfbody);
}
