package com.reqo.ironhold.storage.utils;

import junit.framework.Assert;
import org.fluttercode.datafactory.impl.DataFactory;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * User: ilya
 * Date: 3/23/13
 * Time: 12:23 PM
 */
public class ByteArrayUtilitiesTest {
    @Test
    public void testByteArraySplit() throws IOException {
        DataFactory df = new DataFactory();
        String testString = df.getRandomText(1000);
        int nbytes = testString.getBytes().length;
        byte[][] chunks = ByteArrayUtilities.divideArray(testString.getBytes(), 254);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (byte[] chunk : chunks) {
            bos.write(chunk);
        }
        bos.close();

        int actualBytes = bos.toByteArray().length;

        Assert.assertEquals(nbytes, actualBytes);
        String actualString = new String(bos.toByteArray());

        Assert.assertEquals(testString, actualString);
    }
}
