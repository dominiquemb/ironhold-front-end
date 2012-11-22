package com.reqo.ironhold.storage.model.serializer;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.reqo.ironhold.storage.utils.Compression;

public class CompressedStringDeserializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String value = jp.readValueAs(String.class);
		return Compression.decompress(value);
	}

}
