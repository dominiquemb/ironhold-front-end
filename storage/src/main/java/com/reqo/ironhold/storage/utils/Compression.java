package com.reqo.ironhold.storage.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.elasticsearch.common.Base64;

public class Compression {
    public static String compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        
        byte[] bytes = str.getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream os = new GZIPOutputStream(baos);
        os.write(bytes, 0, bytes.length);
        os.close();
        
        return Base64.encodeBytes(baos.toByteArray());
     }
    
    public static String decompress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(str.getBytes()));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        GZIPInputStream is = new GZIPInputStream(bais);
        byte[] tmp = new byte[256];
        while (true)
        {
            int r = is.read(tmp);
            if (r < 0)
            {
                break;
            }
            buffer.write(tmp, 0, r);
        }
        is.close();

        byte[] content = buffer.toByteArray();
        return new String(content, 0, content.length);
     }
}
