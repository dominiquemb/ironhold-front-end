package com.reqo.ironhold.utils;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.UUID;

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
        int testLength = 1024 * 1024 * 200;
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

        String workingDirectory = in.getRoot().getAbsolutePath() + File.separator + UUID.randomUUID().toString();

        FileSplitter fileSplitter = new FileSplitter(file, new File(workingDirectory));

        List<FileWithChecksum> parts = fileSplitter.split();

        for (FileWithChecksum part : parts) {
            Assert.assertTrue(part.getFile().exists());

            Assert.assertTrue(part.getCheckSum().getCheckSumFile().exists());

            MD5CheckSum checkSum = new MD5CheckSum(part.getCheckSum().getCheckSumFile());
            Assert.assertTrue(checkSum.verifyChecksum());

        }


        File destinationFile = fileSplitter.join(parts, out.getRoot());

        Assert.assertEquals(file.getName(), destinationFile.getName());
        Assert.assertTrue(destinationFile.exists());
        Assert.assertEquals(file.length(), destinationFile.length());

        logger.info("Starting to calculate checksums");
        String actualChecksum = MD5CheckSum.getMD5Checksum(destinationFile);
        String expectedChecksum = MD5CheckSum.getMD5Checksum(file);
        logger.info("Finished calculating checksums");

        Assert.assertEquals(expectedChecksum, actualChecksum);

        File manifestFile = new File(workingDirectory + File.separator + "manifest");
        Assert.assertTrue(manifestFile.exists());

        String manifest = FileUtils.readFileToString(manifestFile);
        String[] lines = manifest.split("\n");
        Assert.assertEquals(parts.size(), lines.length);

        for (String line : lines) {
            String[] chunks = line.split("\t");
            Assert.assertTrue(new File(workingDirectory + File.separator + chunks[0]).exists());
            Assert.assertEquals(Integer.parseInt(chunks[1]), new File(workingDirectory + File.separator + chunks[0]).length());

        }
    }
}
