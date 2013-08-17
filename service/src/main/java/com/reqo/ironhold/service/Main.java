package com.reqo.ironhold.service;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Main class.
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:9090/myapp/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     *
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer(String url, File workingDirectory) {
        Map<String, Object> properties = new HashMap();
        properties.put(ImportPSTResource.WORKING_DIR_PROPERTY, workingDirectory.getAbsolutePath());
        // create a resource config that scans for JAX-RS resources and providers
        // in com.reqo.ironhold.service package
        final ResourceConfig rc = new ResourceConfig().packages("com.reqo.ironhold.service");
        rc.register(MultiPartFeature.class);
        rc.addProperties(properties);
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(url), rc);
    }


    /**
     * Main method.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer(BASE_URI, new File("/tmp"));
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.stop();
    }
}

