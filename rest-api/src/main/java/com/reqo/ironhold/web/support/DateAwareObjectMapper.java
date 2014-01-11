package com.reqo.ironhold.web.support;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

import java.util.Date;

public class DateAwareObjectMapper extends ObjectMapper{
    public DateAwareObjectMapper() {
        SimpleModule module = new SimpleModule("JSONModule", new Version(2, 0, 0, null));
        module.addSerializer(Date.class, new DateSerializer());
        module.addDeserializer(Date.class, new DateDeserializer());
        this.registerModule(module);
    }

}
