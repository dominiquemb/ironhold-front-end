package com.reqo.ironhold.service;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

public class ImportPSTResourceTest {

    private HttpServer server;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        // start the server
//        server = Main.startServer();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    @Ignore
    public void testUsingApacheClient() throws IOException {
        if (new File("/tmp/test.pst").exists()) {
            FileUtils.forceDelete(new File("/tmp/test.pst"));
        }
        Assert.assertFalse(new File("/tmp/test.pst").exists());

        String expected = Long.toString(new Date().getTime());

        String fileName = folder.getRoot().getAbsolutePath() + File.separator + "/test.pst";

        RandomAccessFile f = new RandomAccessFile(fileName, "rw");
        f.setLength(1024 * 1024 * 1024);

        long started = System.currentTimeMillis();


        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://localhost:8080/myapp/importpst/upload/test.pst");
        FileBody fileContent = new FileBody(new File(fileName));
        StringBody comment = new StringBody("Filename: " + fileName);
        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("file", fileContent);
        httppost.setEntity(reqEntity);
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();

        long finished = System.currentTimeMillis();
        System.out.println("testUsingApacheClient took " + (finished - started) + "ms");

        Assert.assertTrue(new File("/tmp/test.pst").exists());

        String actual = FileUtils.readFileToString(new File("/tmp/test.pst"));

        Assert.assertEquals(expected, actual);
    }

    @Test
    @Ignore
    public void testUsingJerseyClient() throws IOException {
        if (new File("/tmp/test.pst").exists()) {
            FileUtils.forceDelete(new File("/tmp/test.pst"));
        }
        Assert.assertFalse(new File("/tmp/test.pst").exists());
        String expected = Long.toString(new Date().getTime());

        String fileName = folder.getRoot().getAbsolutePath() + File.separator + "/test.pst";


        RandomAccessFile f = new RandomAccessFile(fileName, "rw");
        f.setLength(1024 * 1024 * 1024);

        long started = System.currentTimeMillis();
        Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .build();

        WebTarget webResource = client
                .target("http://localhost:8080/myapp/importpst/upload/test.pst");


        FileDataBodyPart fdp = new FileDataBodyPart("file", new File(fileName));

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

        formDataMultiPart.bodyPart(fdp);

        webResource.request().post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

        long finished = System.currentTimeMillis();
        System.out.println("testUsingJerseyClient took " + (finished - started) + "ms");

        Assert.assertTrue(new File("/tmp/test.pst").exists());

        String actual = FileUtils.readFileToString(new File("/tmp/test.pst"));

        Assert.assertEquals(expected, actual);
    }
}
