package com.reqo.ironhold.web.api;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.storage.interfaces.IMetaDataIndexService;
import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.web.domain.LoginChannelEnum;
import com.reqo.ironhold.web.domain.LoginUser;
import com.reqo.ironhold.web.domain.responses.LoginResponse;
import com.reqo.ironhold.web.support.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by ilya on 3/8/14.
 */
@Controller
@RequestMapping(value = "/login")
public class LoginController {
    @Autowired
    protected IMessageIndexService messageIndexService;
    @Autowired
    protected IMiscIndexService miscIndexService;
    @Autowired
    protected IMetaDataIndexService metaDataIndexService;
    @Autowired
    protected IMimeMailMessageStorageService mimeMailMessageStorageService;

    @RequestMapping(method = RequestMethod.POST, value = "/{clientKey}/{username}")
    public
    @ResponseBody
    ApiResponse<LoginResponse> login(@PathVariable("clientKey") String clientKey,
                                     @PathVariable("username") String username,
                                     @RequestBody String password) {

        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>();
        LoginResponse result = null;
        try {
            LoginUser loginUser = miscIndexService.authenticate(clientKey, username, password, LoginChannelEnum.WEB_APP, "127.0.0.1");
            boolean success = loginUser != null;
            String message = success ? "Login succesful" : "Login failed";
            result = new LoginResponse(success, message);
        } catch (Exception e) {
            result = new LoginResponse(false, e.getMessage());
        }

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    protected final String getUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().split("/")[1];
    }

    protected final String getClientKey() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().split("/")[0];
    }

    protected final LoginUser getLoginUser() {
        return miscIndexService.getLoginUser(getClientKey(), getUserName());
    }
}
