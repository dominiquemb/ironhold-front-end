package com.reqo.ironhold.web.components.validators;

import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.vaadin.data.validator.AbstractStringValidator;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * User: ilya
 * Date: 5/23/13
 * Time: 10:10 PM
 */
public class UniqueUsernameValidator extends AbstractStringValidator {
    private final MiscIndexService miscIndexService;
    private final String client;
    private LoginUser loginUser;

    public UniqueUsernameValidator(LoginUser loginUser, String client, MiscIndexService miscIndexService) {
        super("Username is already used");
        this.loginUser = loginUser;
        this.client = client;
        this.miscIndexService = miscIndexService;
    }

    @Override
    protected boolean isValidValue(String value) {
        if (value.isEmpty()) return true;
        LoginUser matchUser = miscIndexService.usernameExists(client, value);
        return matchUser == null || (loginUser != null && loginUser.getId().equals(matchUser.getId()));
    }

    public LoginUser getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(LoginUser loginUser) {
        this.loginUser = loginUser;
    }
}
