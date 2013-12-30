package com.reqo.ironhold.web.domain;


/**
 * User: ilya
 * Date: 4/12/13
 * Time: 12:34 PM
 */
public enum RoleEnum {
    SUPER_USER(Integer.MAX_VALUE), CAN_LOGIN(1), NONE(0), CAN_SEARCH(2), CAN_MANAGE_USERS(4), CAN_SEARCH_ALL(8), CAN_VIEW_AUDIT(16), PST_UPLOAD(32);

    private final int value;

    RoleEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getAttribute() {
        return "ROLE_" + this.name();
    }
}
