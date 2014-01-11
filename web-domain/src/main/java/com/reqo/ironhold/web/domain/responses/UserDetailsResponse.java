package com.reqo.ironhold.web.domain.responses;

import com.reqo.ironhold.web.domain.LoginUser;
import com.reqo.ironhold.web.domain.RoleEnum;

import java.util.List;

/**
 * Created by ilya on 1/11/14.
 */
public class UserDetailsResponse {
    private LoginUser loginUser;
    private List<RoleEnum> roles;

    public UserDetailsResponse() {

    }

    public UserDetailsResponse(LoginUser loginUser, List<RoleEnum> roles) {
        this.loginUser = loginUser;
        this.roles = roles;
    }

    public List<RoleEnum> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleEnum> roles) {
        this.roles = roles;
    }

    public LoginUser getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(LoginUser loginUser) {
        this.loginUser = loginUser;
    }
}
