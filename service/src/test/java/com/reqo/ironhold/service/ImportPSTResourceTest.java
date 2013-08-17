package com.reqo.ironhold.service;

import com.reqo.ironhold.uploadclient.ImportFileClient;
import com.reqo.ironhold.utils.MD5CheckSum;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.RandomAccessFile;

public class ImportPSTResourceTest {

    private HttpServer server;

    @Rule
    public TemporaryFolder clientFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder serviceFolder = new TemporaryFolder();

    private String baseUrl;

    @Before
    public void setUp() throws Exception {
        // start the server
        baseUrl = "http://localhost:1111/myapp/";
        server = Main.startServer(baseUrl, serviceFolder.getRoot());
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testUsingJerseyClient() throws Exception {
        long fileSize = 1024 * 1024 * 200;
        File completeFile = new File(clientFolder.getRoot().getAbsolutePath() + File.separator + "/test.pst");
        RandomAccessFile f = new RandomAccessFile(completeFile.getAbsolutePath(), "rw");
        f.setLength(fileSize);

        Assert.assertTrue(completeFile.exists());
        Assert.assertEquals(fileSize, completeFile.length());

        ImportFileClient client = new ImportFileClient(baseUrl, "importpst", clientFolder.getRoot(), completeFile);
        client.upload();

        File targetFile = new File(serviceFolder.getRoot().getAbsolutePath() + File.separator + client.getSessionId() + File.separator + completeFile.getName());
        Assert.assertTrue(targetFile.exists());
        Assert.assertEquals(fileSize, targetFile.length());

        Assert.assertEquals(MD5CheckSum.getMD5Checksum(completeFile), MD5CheckSum.getMD5Checksum(targetFile));
    }

}
