package com.reqo.ironhold.service.resources;

import com.reqo.ironhold.service.beans.WorkingDir;
import com.reqo.ironhold.utils.FileSplitter;
import com.reqo.ironhold.utils.FileWithChecksum;
import com.reqo.ironhold.utils.MD5CheckSum;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("importpst")
@Component
public class ImportPSTResource {

    @Autowired
    WorkingDir workingDir;

    @Context
    Application application;


    @GET
    @Path("/session")
    public Response createSession() throws IOException {
        String sessionId = UUID.randomUUID().toString();
        FileUtils.forceMkdir(new File(workingDir.getWorkDir() + File.separator + sessionId));

        return Response.status(200).type(MediaType.TEXT_PLAIN_TYPE).entity(sessionId).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/session/upload/{sessionId}/{fileName}")
    public Response uploadFile(
            @PathParam("sessionId") String sessionId,
            @PathParam("fileName") String fileName,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {
        File directory = new File(workingDir.getWorkDir() + File.separator + sessionId);
        if (sessionId == null || !directory.exists() || !directory.isDirectory() || !directory.canWrite()) {
            return Response.status(400).entity("Session is not valid").build();
        }

        String uploadedFileLocation = directory.getAbsolutePath() + File.separator + fileName;

        // save it
        writeToFile(uploadedInputStream, uploadedFileLocation);

        String output = "File uploaded succesfully";

        return Response.status(200).entity(output).build();
    }

    @GET
    @Path("/session/verify/{sessionId}/{fileName}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> verify(@PathParam("sessionId") String sessionId,
                               @PathParam("fileName") String fileName) throws Exception {
        List<String> issues = new ArrayList();
        File directory = new File(workingDir.getWorkDir() + File.separator + sessionId);
        List<FileWithChecksum> parts = new ArrayList();
        for (File part : directory.listFiles()) {
            if (!part.getName().endsWith("md5")) {
                MD5CheckSum md5CheckSum = new MD5CheckSum(new File(part.getAbsolutePath() + ".md5"));
                if (!md5CheckSum.verifyChecksum()) {
                    issues.add(part.getName());
                }
                parts.add(new FileWithChecksum(part, md5CheckSum));
            }
        }

        String manifestContents = FileUtils.readFileToString(new File(directory.getAbsolutePath() + File.separator + "manifest"));
        for (String line : manifestContents.split("\n")) {
            String[] chunks = line.split("\t");
            File chunkFile = new File(directory.getAbsoluteFile() + File.separator + chunks[0]);
            if (!chunkFile.exists() || chunkFile.length() != Integer.parseInt(chunks[1])) {
                issues.add(chunks[0]);
            }
        }


        return issues;

    }

    @GET
    @Path("/session/join/{sessionId}/{fileName}")
    public Response join(@PathParam("sessionId") String sessionId,
                         @PathParam("fileName") String fileName) throws Exception {
        File directory = new File(workingDir.getWorkDir() + File.separator + sessionId);
        List<FileWithChecksum> parts = new ArrayList();
        for (File part : directory.listFiles()) {
            if (!part.getName().endsWith("md5")) {
                MD5CheckSum md5CheckSum = new MD5CheckSum(new File(part.getAbsolutePath() + ".md5"));
                if (!md5CheckSum.verifyChecksum()) {
                    return Response.status(500).entity("Part " + part.getName() + " failed checksum verification").build();
                }
                parts.add(new FileWithChecksum(part, md5CheckSum));
            }
        }

        FileSplitter fileSplitter = new FileSplitter(new File(directory.getAbsoluteFile() + File.separator + fileName), directory);
        fileSplitter.join(parts, directory);

        MD5CheckSum md5CheckSum = new MD5CheckSum(new File(directory.getAbsoluteFile() + File.separator + fileName + ".md5"));
        if (!md5CheckSum.verifyChecksum()) {
            return Response.status(500).entity("Joined file failed checksum verification").build();
        }

        return Response.status(200).entity("File join succeeded").build();

    }

    /*private String getProperty(String propertyName) {

        return application..getProperties().get(propertyName).toString();
    } */

    // save uploaded file to new location
    private void writeToFile(InputStream uploadedInputStream,
                             String uploadedFileLocation) {

        try {
            OutputStream out = new FileOutputStream(new File(
                    uploadedFileLocation));
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }


}
