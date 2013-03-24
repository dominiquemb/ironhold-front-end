package com.reqo.ironhold.storage.model.metadata;

import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

public class PSTFileMetaTest {

    @Test
    public void testUpdateSizeStatistics() {
        double sizes[] = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000,
                100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 100, 200,
                300, 400, 500, 600, 700, 800, 900, 1000};

        PSTFileMeta metaData = new PSTFileMeta("pstFileName", "mailBoxName",
                "originalFilePath", "commentary", "md5", "hostname", 0,
                new Date());
        for (double dsize : sizes) {
            int size = (int) dsize;
            metaData.updateSizeStatistics(size, size / 10);
        }

        metaData.persistCalculations();
        Assert.assertEquals(550, Math.round(metaData.getAverageSize()));
        Assert.assertEquals(55, Math.round(metaData.getCompressedAverageSize()));

        Assert.assertEquals(550, Math.round(metaData.getMedianSize()));
        Assert.assertEquals(55, Math.round(metaData.getMedianCompressedSize()));

        Assert.assertEquals(1000, metaData.getMaxSize());
        Assert.assertEquals(100, metaData.getCompressedMaxSize());
    }

    @Test
    public void testJSON() throws IOException {
        double sizes[] = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000,
                100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 100, 200,
                300, 400, 500, 600, 700, 800, 900, 1000};

        PSTFileMeta metaData = new PSTFileMeta("pstFileName", "mailBoxName",
                "originalFilePath", "commentary", "md5", "hostname", 0,
                new Date());
        for (double dsize : sizes) {
            int size = (int) dsize;
            metaData.updateSizeStatistics(size, size / 10);
        }
        String jsonString = metaData.serialize();
        Assert.assertTrue(jsonString.length() > 0);
        PSTFileMeta metaData2 = new PSTFileMeta().deserialize(jsonString);
        Assert.assertTrue(metaData2 != null);
        String jsonString2 = metaData2.serialize();
        Assert.assertEquals(jsonString, jsonString2);

        System.out.println(jsonString2);
    }

    @Test
    public void testFolderMap() throws IOException {

        String folders[] = {"a", "b", "c", "d"};
        int counts[] = {100, 200, 300, 400};
        PSTFileMeta metaData = new PSTFileMeta("pstFileName", "mailBoxName",
                "originalFilePath", "commentary", "md5", "hostname", 0,
                new Date());
        for (int i = 0; i < folders.length; i++) {
            metaData.addFolder(folders[i], counts[i]);
        }
        String jsonString = metaData.serialize();
        Assert.assertTrue(jsonString.length() > 0);
        PSTFileMeta metaData2 = new PSTFileMeta().deserialize(jsonString);
        Assert.assertTrue(metaData2 != null);
        String jsonString2 = metaData2.serialize();
        Assert.assertEquals(jsonString, jsonString2);

        Assert.assertEquals(metaData.getFolderMap().size(), metaData
                .getFolderMap().size());
        System.out.println(jsonString2);
    }
}
