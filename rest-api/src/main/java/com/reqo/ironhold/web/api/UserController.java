package com.reqo.ironhold.web.api;

import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.block.predicate.Predicate;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.utility.ArrayIterate;
import com.gs.collections.impl.utility.ListIterate;
import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.storage.interfaces.IMetaDataIndexService;
import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.web.domain.AuditActionEnum;
import com.reqo.ironhold.web.domain.AuditLogMessage;
import com.reqo.ironhold.web.domain.LoginUser;
import com.reqo.ironhold.web.domain.RoleEnum;
import com.reqo.ironhold.web.domain.responses.AuditLogResponse;
import com.reqo.ironhold.web.domain.responses.UserDetailsResponse;
import com.reqo.ironhold.web.support.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 9:01 AM
 */
@Controller
@Secured("ROLE_CAN_LOGIN")
@RequestMapping(value = "/users")
public class UserController extends AbstractController {
    @Autowired
    protected IMessageIndexService messageIndexService;
    @Autowired
    protected IMiscIndexService miscIndexService;
    @Autowired
    protected IMetaDataIndexService metaDataIndexService;
    @Autowired
    protected IMimeMailMessageStorageService mimeMailMessageStorageService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController() {
        super();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/searchHistory")
    public
    @ResponseBody
    ApiResponse<AuditLogResponse> getHistory() {

        ApiResponse<AuditLogResponse> apiResponse = new ApiResponse<>();


        List<AuditLogMessage> history = metaDataIndexService.getAuditLogMessages(getClientKey(), getLoginUser(), AuditActionEnum.SEARCH);
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

    @Secured("ROLE_MANAGE_USERS")
    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    ApiResponse<List<UserDetailsResponse>> getMatches(@RequestParam final String criteria,
                                                      @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                      @RequestParam(required = false, defaultValue = "0") int page) {

        ApiResponse<List<UserDetailsResponse>> apiResponse = new ApiResponse<>();

        List<LoginUser> matches = miscIndexService.getLoginUsers(getClientKey(), criteria, page * pageSize, pageSize);
        MutableList<UserDetailsResponse> result = ListIterate.collect(matches, new Function<LoginUser, UserDetailsResponse>() {
            @Override
            public UserDetailsResponse valueOf(final LoginUser loginUser) {
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
                return new UserDetailsResponse(loginUser, roles);
            }
        });

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }


    @RequestMapping(method = RequestMethod.GET, value = "/roles")
    public
    @ResponseBody
    ApiResponse<RoleEnum[]> getRoles() {
        logger.info("Current user is " + getUserName());
        ApiResponse<RoleEnum[]> apiResponse = new ApiResponse<>();

        apiResponse.setPayload(RoleEnum.values());
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

