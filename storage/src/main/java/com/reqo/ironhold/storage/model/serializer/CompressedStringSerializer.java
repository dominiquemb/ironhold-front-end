package com.reqo.ironhold.storage.model.serializer;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.reqo.ironhold.storage.utils.Compression;

public class CompressedStringSerializer extends JsonSerializer<String> {

	@Override
	public void serialize(String value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
        jgen.writeString(Compression.compress(value));
	}

}
