package com.reqo.ironhold.storage.utils;

import com.reqo.ironhold.model.utils.Compression;
import junit.framework.Assert;
import org.fluttercode.datafactory.impl.DataFactory;
import org.junit.Test;

import java.io.IOException;

public class CompressionTest {

	@Test
	public void testCompression() throws IOException {
		
		DataFactory df = new DataFactory();
		String input = "Ilya\n\n\nWednesday at 1pm works for me.  If anything changes let me know.  Thanks and I look forward to our meeting.";

		String compressedInput = Compression.compress(input);
		String decompressedInput = Compression.decompress(compressedInput);

		double percent = ((100 * (input.length() - compressedInput.length())) / (double)input.length());
		System.out
				.println(String.format("Compression savings: %.2f%%", percent));
		Assert.assertEquals(input, decompressedInput);
	}

}
