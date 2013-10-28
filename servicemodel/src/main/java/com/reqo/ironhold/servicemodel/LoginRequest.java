package com.reqo.ironhold.servicemodel;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * User: ilya
 * Date: 10/26/13
 * Time: 11:50 AM
 */
public class LoginRequest {
    private String client;
    private String username;
    private String password;
    private String loginChannel;

    private static ObjectMapper mapper = new ObjectMapper();

    public LoginRequest() {

    }

    public LoginRequest(String client, String username, String password, String loginChannel) {
        this.client = client;
        this.username = username;
        this.password = password;
        this.loginChannel = loginChannel;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLoginChannel() {
        return loginChannel;
    }

    public void setLoginChannel(String loginChannel) {
        this.loginChannel = loginChannel;
    }

    public String serialize() throws IOException {
        return mapper.writeValueAsString(this);
    }

    public static LoginRequest deserialize(String json) throws IOException {
        return mapper.readValue(json, LoginRequest.class);
    }
}
