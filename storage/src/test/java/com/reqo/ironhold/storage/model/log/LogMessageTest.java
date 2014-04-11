package com.reqo.ironhold.storage.model.log;

import com.reqo.ironhold.web.domain.LogMessage;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * User: ilya
 * Date: 3/21/13
 * Time: 12:15 AM
 */
public class LogMessageTest {

    @Test
    public void testSerialization() throws IOException {

        String source = "{\"messageId\":\"954073c7-24a7-434b-acf5-9b4ea56c38a1\",\"host\":\"Lindsey Craft\",\"timestamp\":\"2001-02-23T07:49:16.097+0000\",\"message\":\"Erica Larsen Ryan Levine Erika Smith\",\"level\":\"Failure\",\"partition\":\"2001\"}";
        LogMessage logMessage = new LogMessage();
        LogMessage logMessage2 = logMessage.deserialize(source);


        Assert.assertEquals(source, logMessage2.serialize());

    }
}
