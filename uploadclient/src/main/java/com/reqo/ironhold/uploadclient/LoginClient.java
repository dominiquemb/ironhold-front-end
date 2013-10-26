package com.reqo.ironhold.uploadclient;

import com.reqo.ironhold.servicemodel.LoginRequest;
import org.apache.log4j.Logger;

import javax.ws.rs.client.*;
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
        this.restClient = ClientBuilder.newBuilder().build();

    }


    public boolean login(String client, String username, String password) throws IOException {
        WebTarget webTarget = restClient.target(baseUrl + "/login");
        Invocation.Builder builder = webTarget.request();
        LoginRequest loginRequest = new LoginRequest(client, username, password);

        String jsonString = loginRequest.serialize();

        Response response = builder.post(Entity.json(jsonString));



        return response.getStatus() == Response.Status.OK.getStatusCode();
    }






}
