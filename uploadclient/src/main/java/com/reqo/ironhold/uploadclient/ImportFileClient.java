package com.reqo.ironhold.uploadclient;

import com.reqo.ironhold.utils.FileSplitter;
import com.reqo.ironhold.utils.FileWithChecksum;
import com.reqo.ironhold.utils.MD5CheckSum;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: ilya
 * Date: 8/12/13
 * Time: 8:43 PM
 */
public class ImportFileClient {
    private static Logger logger = Logger.getLogger(ImportFileClient.class);
    private final Client client;
    private final String baseUrl;
    private final File workingDirectory;
    private final File completeFile;
    private final String resourceName;
    private String sessionId;

    public ImportFileClient(String baseUrl, String resourceName, File workingDirectory, File completeFile) {
        this.baseUrl = baseUrl;
        this.resourceName = resourceName;
        this.workingDirectory = workingDirectory;
        this.completeFile = completeFile;
        this.client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();

    }

    public void upload() throws Exception {
        startSession();
        splitAndUpload();

        List<String> missingParts = new ArrayList();

        /*do {
            missingParts = verify();
            for (String missingPart : missingParts) {
                uploadPart(new File(workingDirectory.getAbsolutePath() + File.separator + sessionId + File.separator + missingPart));
                uploadPart(new File(workingDirectory.getAbsolutePath() + File.separator + sessionId + File.separator + missingPart + ".md5"));
            }
        } while (missingParts.size() > 0);*/
        join();
    }


    private void startSession() {
        long started = System.currentTimeMillis();

        logger.info("Starting session");
        WebTarget webTarget = client.target(baseUrl + resourceName + "/session");
        Response response = webTarget.request().get();


        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus() + "[" + response.readEntity(String.class) + "]");
        }

        this.sessionId = response.readEntity(String.class);

        long finished = System.currentTimeMillis();

        logger.info("Session id: " + sessionId + " in " + (finished - started) + "ms");
    }

    private void splitAndUpload() throws Exception {

        logger.info("Starting upload of " + completeFile.getAbsolutePath());

        File completeCheckSumFile = MD5CheckSum.createMD5CheckSum(completeFile);

        File destinationDirectory = new File(workingDirectory.getAbsolutePath() + File.separator + sessionId);
        FileSplitter fileSplitter = new FileSplitter(completeFile, destinationDirectory);
        List<FileWithChecksum> parts = fileSplitter.split();


        uploadPart(completeCheckSumFile);
        for (FileWithChecksum part : parts) {
            uploadPart(part.getFile());
            uploadPart(part.getCheckSum().getCheckSumFile());
        }
    }

    private void join() {
        Response response = client
                .target(baseUrl + resourceName + "/session/join/").path(sessionId).path(completeFile.getName()).request().get();


        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus() + "[" + response.readEntity(String.class) + "]");
        }
    }


    private void uploadPart(File partialFile) throws IOException {
        long started = System.currentTimeMillis();
        logger.info("Starting upload " + partialFile.getName());

        WebTarget webResource = client
                .target(baseUrl + resourceName + "/session/upload").path(sessionId).path(partialFile.getName());

        FileDataBodyPart fdp = new FileDataBodyPart("file", partialFile);

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        formDataMultiPart.bodyPart(fdp);
        Response response = webResource.request().post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        long finished = System.currentTimeMillis();

        logger.info("Upload completed " + partialFile.getName() + " in " + (finished - started) + "ms");
    }


    public String getSessionId() {
        return sessionId;
    }


}
