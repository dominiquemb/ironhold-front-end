package com.reqo.ironhold.utils;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * User: ilya
 * Date: 8/7/13
 * Time: 11:46 AM
 */
public class FileSplitterTest {
    private static Logger logger = Logger.getLogger(FileSplitterTest.class);

    @Rule
    public TemporaryFolder in = new TemporaryFolder();

    @Rule
    public TemporaryFolder out = new TemporaryFolder();

    @Test
    public void testSplitAndJoinLargeFile() throws Exception {
        String fileName = in.getRoot().getAbsolutePath() + File.separator + "test.pst";

        logger.info("Writing large file " + fileName);
        RandomAccessFile f = new RandomAccessFile(fileName, "rw");
        int testLength = 1024 * 1024 * 1024;
        f.setLength(testLength); // 1GB

        logger.info("Finished writing large file " + fileName);

        splitAndJoinWithAssertions(new File(fileName));

    }

    @Test
    public void testSplitAndJoinSmallFile() throws Exception {
        String fileName = in.getRoot().getAbsolutePath() + File.separator + "test.pst";

        logger.info("Writing small file " + fileName);
        RandomAccessFile f = new RandomAccessFile(fileName, "rw");
        int testLength = 1024;
        f.setLength(testLength);

        logger.info("Finished writing small file " + fileName);

        splitAndJoinWithAssertions(new File(fileName));

    }

    private void splitAndJoinWithAssertions(File file) throws Exception {


        FileSplitter FileSplitter = new FileSplitter(file, in.getRoot());

        List<File> parts = FileSplitter.split();

        Assert.assertEquals(1 + file.length() / FileSplitter.DEFAULT_CHUNK_SIZE, parts.size());
        for (File part : parts) {
            Assert.assertTrue(part.exists());

            File checkSumFile = new File(part.getAbsolutePath() + ".md5");
            Assert.assertTrue(checkSumFile.exists());

            MD5CheckSum checkSum = new MD5CheckSum(checkSumFile);
            Assert.assertTrue(checkSum.verifyChecksum());

        }


        File destinationFile = FileSplitter.join(parts, out.getRoot());

        Assert.assertEquals(file.getName(), destinationFile.getName());
        Assert.assertTrue(destinationFile.exists());
        Assert.assertEquals(file.length(), destinationFile.length());

        logger.info("Starting to calculate checksums");
        String actualChecksum = MD5CheckSum.getMD5Checksum(destinationFile);
        String expectedChecksum = MD5CheckSum.getMD5Checksum(file);
        logger.info("Finished calculating checksums");

        Assert.assertEquals(expectedChecksum, actualChecksum);
    }
}
