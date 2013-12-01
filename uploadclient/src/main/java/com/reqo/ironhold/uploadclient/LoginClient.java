package com.reqo.ironhold.uploadclient;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.apache.log4j.Logger;

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


}
