package com.reqo.ironhold.uploadclient;

import com.reqo.ironhold.servicemodel.LoginRequest;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * User: ilya
 * Date: 8/12/13
 * Time: 8:43 PM
 */
public class LoginClient {
    private static Logger logger = Logger.getLogger(LoginClient.class);
    private final Client restClient;
    private final String baseUrl;

    public LoginClient(String baseUrl) {
        this.baseUrl = baseUrl;

        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        this.restClient = Client.create(clientConfig);
    }


    public boolean login(String client, String username, String password, String loginChannel) throws IOException {
        logger.info("Attempting login for [" + client + "/" + username + "] via " + loginChannel + " by requesting " + baseUrl + "user/login");
        WebResource webTarget = restClient.resource(baseUrl + "user/login");

        LoginRequest loginRequest = new LoginRequest(client, username, password, loginChannel);

        String jsonString = loginRequest.serialize();

        ClientResponse response = webTarget.
                accept(MediaType.TEXT_PLAIN).
                type(MediaType.APPLICATION_JSON_TYPE).
                post(ClientResponse.class, jsonString);

        logger.info("Response recieved " + response.getStatus());

        return response.getStatus() == Response.Status.OK.getStatusCode();
    }


}
