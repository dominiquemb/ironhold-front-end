package com.reqo.ironhold.web.domain.responses;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 10:05 AM
 */
public class LoginResponse {
    private boolean success;
    private String message;


    public LoginResponse() {

    }

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
