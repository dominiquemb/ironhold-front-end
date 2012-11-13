package com.reqo.ironhold.storage.utils;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

import java.io.IOException;

public class TabIndenter extends DefaultPrettyPrinter.Lf2SpacesIndenter {
    public TabIndenter() {
    }

    @Override
    public void writeIndentation(JsonGenerator jg, int level) throws IOException, JsonGenerationException {
        jg.writeRaw('\t');
    }

    @Override
    public boolean isInline() {
        return true;
    }


}
