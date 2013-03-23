package com.reqo.ironhold.storage.utils;

import java.util.Arrays;

/**
 * User: ilya
 * Date: 3/23/13
 * Time: 10:11 AM
 */
public class ByteArrayUtilities {

    public static byte[][] divideArray(byte[] source, int chunksize) {


        byte[][] ret = new byte[(int) Math.ceil(source.length / (double) chunksize)][chunksize];

        int start = 0;

        for (int i = 0; i < ret.length; i++) {
            if (start + chunksize < source.length) {
                ret[i] = Arrays.copyOfRange(source, start, start + chunksize);
                start += chunksize;
            } else {
                int topLimit = source.length - start;
                ret[i] = new byte[topLimit];
                ret[i] = Arrays.copyOfRange(source, start, start + topLimit);
                start += topLimit;
            }
        }

        return ret;
    }
}
