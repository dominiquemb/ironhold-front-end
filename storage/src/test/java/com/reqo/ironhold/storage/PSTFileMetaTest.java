package com.reqo.ironhold.storage;

import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

import com.reqo.ironhold.storage.model.PSTFileMeta;

public class PSTFileMetaTest {

	@Test
	public void testUpdateSizeStatistics() {
		double sizes[] = { 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000,
				100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 100, 200,
				300, 400, 500, 600, 700, 800, 900, 1000 };

		PSTFileMeta metaData = new PSTFileMeta("pstFileName", "mailBoxName",
				"originalFilePath", "commentary", "md5", "hostname", 0,
				new Date());
		for (double dsize : sizes) {
			int size = (int) dsize;
			metaData.updateSizeStatistics(size, size / 10);
			metaData.updateAttachmentSizeStatistics(size * 10, size);
		}

		metaData.persistCalculations();
		Assert.assertEquals(550, Math.round(metaData.getAverageSize()));
		Assert.assertEquals(55, Math.round(metaData.getCompressedAverageSize()));
		Assert.assertEquals(5500,
				Math.round(metaData.getAverageAttachmentSize()));
		Assert.assertEquals(550,
				Math.round(metaData.getCompressedAverageAttachmentSize()));

		Assert.assertEquals(550, Math.round(metaData.getMedianSize()));
		Assert.assertEquals(55, Math.round(metaData.getMedianCompressedSize()));
		Assert.assertEquals(5500,
				Math.round(metaData.getMedianAttachmentSize()));
		Assert.assertEquals(550,
				Math.round(metaData.getMedianCompressedAttachmentSize()));

		Assert.assertEquals(1000, metaData.getMaxSize());
		Assert.assertEquals(10000, metaData.getMaxAttachmentSize());
		Assert.assertEquals(100, metaData.getCompressedMaxSize());
		Assert.assertEquals(1000, metaData.getCompressedMaxAttachmentSize());
	}

	@Test
	public void testJSON() throws JsonGenerationException,
			JsonMappingException, IOException {
		double sizes[] = { 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000,
				100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 100, 200,
				300, 400, 500, 600, 700, 800, 900, 1000 };

		PSTFileMeta metaData = new PSTFileMeta("pstFileName", "mailBoxName",
				"originalFilePath", "commentary", "md5", "hostname", 0,
				new Date());
		for (double dsize : sizes) {
			int size = (int) dsize;
			metaData.updateSizeStatistics(size, size / 10);
			metaData.updateAttachmentSizeStatistics(size * 10, size);
		}
		String jsonString = PSTFileMeta.toJSON(metaData);
		Assert.assertTrue(jsonString.length() > 0);
		PSTFileMeta metaData2 = PSTFileMeta.fromJSON(jsonString);
		Assert.assertTrue(metaData2 != null);
		String jsonString2 = PSTFileMeta.toJSON(metaData2);
		Assert.assertEquals(jsonString, jsonString2);

		System.out.println(jsonString2);
	}

	@Test
	public void testFolderMap() throws JsonGenerationException,
			JsonMappingException, IOException {

		String folders[] = { "a", "b", "c", "d" };
		int counts[] = { 100, 200, 300, 400 };
		PSTFileMeta metaData = new PSTFileMeta("pstFileName", "mailBoxName",
				"originalFilePath", "commentary", "md5", "hostname", 0,
				new Date());
		for (int i = 0; i < folders.length; i++) {
			metaData.addFolder(folders[i], counts[i]);
		}
		String jsonString = PSTFileMeta.toJSON(metaData);
		Assert.assertTrue(jsonString.length() > 0);
		PSTFileMeta metaData2 = PSTFileMeta.fromJSON(jsonString);
		Assert.assertTrue(metaData2 != null);
		String jsonString2 = PSTFileMeta.toJSON(metaData2);
		Assert.assertEquals(jsonString, jsonString2);

		Assert.assertEquals(metaData.getFolderMap().size(), metaData
				.getFolderMap().size());
		System.out.println(jsonString2);
	}
}
