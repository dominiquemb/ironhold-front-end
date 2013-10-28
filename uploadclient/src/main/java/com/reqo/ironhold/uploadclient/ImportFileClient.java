package com.reqo.ironhold.uploadclient;

import com.reqo.ironhold.utils.FileSplitter;
import com.reqo.ironhold.utils.FileWithChecksum;
import com.reqo.ironhold.utils.MD5CheckSum;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.apache.log4j.Logger;

import javax.ws.rs.core.EntityTag;
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
    private final Client restClient;
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
        this.restClient = Client.create();

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
        WebResource webTarget = restClient.resource(baseUrl + resourceName + "/session");


        ClientResponse response = webTarget.get(ClientResponse.class);
        EntityTag e = response.getEntityTag();
        String entity = response.getEntity(String.class);

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus() + "[" + entity + "]");
        }

        this.sessionId = entity;

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
        WebResource webTarget = restClient
                .resource(baseUrl + resourceName + "/session/join/").path(sessionId).path(completeFile.getName());

        ClientResponse response = webTarget.get(ClientResponse.class);
        EntityTag e = response.getEntityTag();
        String entity = response.getEntity(String.class);


        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus() + "[" + entity + "]");
        }
    }


    private void uploadPart(File partialFile) throws IOException {
        long started = System.currentTimeMillis();
        logger.info("Starting upload " + partialFile.getName());

        WebResource webTarget = restClient
                .resource(baseUrl + resourceName + "/session/upload").path(sessionId).path(partialFile.getName());

        FileDataBodyPart fdp = new FileDataBodyPart("file", partialFile);

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        formDataMultiPart.bodyPart(fdp);

        ClientResponse response = webTarget.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class, formDataMultiPart);

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
