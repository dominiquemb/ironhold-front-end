package com.reqo.ironhold.web.api;

import com.gs.collections.api.block.predicate.Predicate;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.utility.ArrayIterate;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.storage.interfaces.IMetaDataIndexService;
import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.web.domain.*;
import com.reqo.ironhold.web.domain.responses.AuditLogResponse;
import com.reqo.ironhold.web.domain.responses.LoginResponse;
import com.reqo.ironhold.web.domain.responses.UserDetailsResponse;
import com.reqo.ironhold.web.support.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 9:01 AM
 */
@Controller
@RequestMapping(value = "/users")
public class UserController {
    private IMessageIndexService messageIndexService;
    private IMiscIndexService miscIndexService;
    private IMetaDataIndexService metaDataIndexService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Inject
    public UserController(IMessageIndexService messageIndexService, IMiscIndexService miscIndexService, IMetaDataIndexService metaDataIndexService) {
        this.messageIndexService = messageIndexService;
        this.miscIndexService = miscIndexService;
        this.metaDataIndexService = metaDataIndexService;
    }


    private LoginUser getDefaultUser() {
        return miscIndexService.authenticate("demo", "demo", "demo", LoginChannelEnum.WEB_APP, "127.0.0.1");
    }


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


    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}/{username}/searchHistory")
    public
    @ResponseBody
    ApiResponse<AuditLogResponse> getHistory(@PathVariable("clientKey") String clientKey,
                                             @PathVariable("username") String username) {

        ApiResponse<AuditLogResponse> apiResponse = new ApiResponse<>();


        List<AuditLogMessage> history = metaDataIndexService.getAuditLogMessages(clientKey, getDefaultUser(), AuditActionEnum.SEARCH);
        MutableList<AuditLogMessage> messages = FastList.newList(history);

        AuditLogResponse result = new AuditLogResponse(messages.toSortedSetBy(AuditLogMessage.SORT_BY_CONTEXT));
        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }


    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}/{username}")
    public
    @ResponseBody
    ApiResponse<UserDetailsResponse> getUserDetails(@PathVariable("clientKey") String clientKey,
                                                    @PathVariable("username") String username) {

        ApiResponse<UserDetailsResponse> apiResponse = new ApiResponse<>();
        final LoginUser loginUser = miscIndexService.getLoginUser(clientKey, username);
        if (loginUser == null) {
            apiResponse.setPayload(null);
            apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);
            apiResponse.setMessage("User not found");
        } else {
            List<RoleEnum> roles =
                    ArrayIterate.select(RoleEnum.values(), new Predicate<RoleEnum>() {
                        @Override
                        public boolean accept(RoleEnum roleEnum) {
                            if (loginUser.hasRole(RoleEnum.SUPER_USER)) {
                                return roleEnum == RoleEnum.SUPER_USER;
                            }
                            return roleEnum != RoleEnum.NONE && loginUser.hasRole(roleEnum);
                        }
                    });

            loginUser.setHashedPassword("********");
            UserDetailsResponse result = new UserDetailsResponse(loginUser, roles);
            apiResponse.setPayload(result);
            apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);
        }

        return apiResponse;

    }


    @RequestMapping(method = RequestMethod.GET, value = "/roles")
    public
    @ResponseBody
    ApiResponse<RoleEnum[]> getRoles() {
        ApiResponse<RoleEnum[]> apiResponse = new ApiResponse<>();

        apiResponse.setPayload(RoleEnum.values());
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

}

