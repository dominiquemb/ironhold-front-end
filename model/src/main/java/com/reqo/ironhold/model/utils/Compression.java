package com.reqo.ironhold.model.utils;

import org.apache.log4j.Logger;
import org.elasticsearch.common.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compression {
	private static Logger logger = Logger.getLogger(Compression.class);

	public static String compress(String str) throws IOException {
		long started = System.currentTimeMillis();
		try {
			if (str == null || str.length() == 0) {
				return str;
			}

			byte[] bytes = str.getBytes();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream os = new GZIPOutputStream(baos);
			os.write(bytes, 0, bytes.length);
			os.close();

			return Base64.encodeBytes(baos.toByteArray());
		} finally {
			long finished = System.currentTimeMillis();
			logger.debug("Compressed " + str.length() + " bytes in "
					+ (finished - started) + "ms");
		}
	}

	public static String decompress(String str) throws IOException {
		long started = System.currentTimeMillis();
		try {
			if (str == null || str.length() == 0) {
				return str;
			}

			ByteArrayInputStream bais = new ByteArrayInputStream(
					Base64.decode(str.getBytes()));
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			GZIPInputStream is = new GZIPInputStream(bais);
			byte[] tmp = new byte[256];
			while (true) {
				int r = is.read(tmp);
				if (r < 0) {
					break;
				}
				buffer.write(tmp, 0, r);
			}
			is.close();

			byte[] content = buffer.toByteArray();
			return new String(content, 0, content.length);
		} finally {
			long finished = System.currentTimeMillis();
			logger.debug("Decompressed " + str.length() + " bytes in "
					+ (finished - started) + "ms");
		}
	}
}
