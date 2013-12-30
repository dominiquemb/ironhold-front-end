package com.reqo.ironhold.web.domain;

/**
 * User: ilya
 * Date: 10/26/13
 * Time: 2:08 PM
 */
public enum LoginChannelEnum {
    WEB_APP("Web"), PST_UPLOAD("PST Upload utility");

    private final String value;

    LoginChannelEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LoginChannelEnum fromString(String value) {
        switch (value) {
            case "WEB_APP":
                return WEB_APP;
            case "PST_UPLOAD":
                return PST_UPLOAD;
        }
        throw new IllegalArgumentException("[" + value + "] is an unknown Login Channel");
    }
}
