package com.reqo.ironhold.web.api;

import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.block.predicate.Predicate;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.gs.collections.impl.utility.ArrayIterate;
import com.gs.collections.impl.utility.ListIterate;
import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.storage.interfaces.IMetaDataIndexService;
import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;
import com.reqo.ironhold.storage.security.CheckSumHelper;
import com.reqo.ironhold.web.domain.*;
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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 9:01 AM
 */
@Controller
@Secured("ROLE_CAN_LOGIN")
@RequestMapping(value = "/users")
public class UserController {
    public static final String EMPTY_PASSWORD = "********";
    @Autowired
    protected IMessageIndexService messageIndexService;
    @Autowired
    protected IMiscIndexService miscIndexService;
    @Autowired
    protected IMetaDataIndexService metaDataIndexService;
    @Autowired
    protected IMimeMailMessageStorageService mimeMailMessageStorageService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final ExecutorService backgroundExecutor;

    public UserController() {
        super();
        this.backgroundExecutor = Executors.newFixedThreadPool(10);
    }

    @Secured("ROLE_CAN_MANAGE_USERS")
    @RequestMapping(method = RequestMethod.GET, value="/psts")
    public
    @ResponseBody
    ApiResponse<List<PSTFileMeta>> getAvailablePSTs(@RequestParam(required = false, defaultValue = "*") final String criteria,
                                                      @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                      @RequestParam(required = false, defaultValue = "0") int page) {

        ApiResponse<List<PSTFileMeta>> apiResponse = new ApiResponse<>();

        List<PSTFileMeta> metas = miscIndexService.getPSTFileMetas(getClientKey(), criteria, page * pageSize, pageSize);

        apiResponse.setPayload(metas);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }


    @RequestMapping(method = RequestMethod.GET, value = "/searchHistory")
    public
    @ResponseBody
    ApiResponse<List<ViewableAuditLogMessage>> getHistory() {

        ApiResponse<List<ViewableAuditLogMessage>> apiResponse = new ApiResponse<>();


        List<AuditLogMessage> history = metaDataIndexService.getAuditLogMessages(getClientKey(), getLoginUser(), AuditActionEnum.SEARCH);
        MutableList<ViewableAuditLogMessage> messages = FastList.newList(history).collect(ViewableAuditLogMessage.FROM_AUDIT_LOG_MESSAGE);


        List<ViewableAuditLogMessage> result = messages.toSortedListBy(ViewableAuditLogMessage.SORT_BY_CONTEXT);
        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }


    @RequestMapping(method = RequestMethod.GET, value = "/{username}")
    public
    @ResponseBody
    ApiResponse<UserDetailsResponse> getUserDetails(@PathVariable("username") String username) {

        ApiResponse<UserDetailsResponse> apiResponse = new ApiResponse<>();
        final LoginUser loginUser = miscIndexService.getLoginUser(getClientKey(), username);
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

            loginUser.setHashedPassword(EMPTY_PASSWORD);
            UserDetailsResponse result = new UserDetailsResponse(loginUser, roles);
            apiResponse.setPayload(result);
            apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);
        }

        return apiResponse;

    }

    @Secured("ROLE_CAN_MANAGE_USERS")
    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    void updateUser(@RequestBody LoginUser userDetails) {
        final String clientKey = getClientKey();
        final LoginUser loginUser = miscIndexService.getLoginUser(getClientKey(), userDetails.getUsername());
        if (!userDetails.getHashedPassword().equals(EMPTY_PASSWORD)) {
            userDetails.setHashedPassword(CheckSumHelper.getCheckSum(userDetails.getHashedPassword().getBytes()));
            miscIndexService.store(clientKey, userDetails);
        } else {
            userDetails.setHashedPassword(loginUser.getHashedPassword());
            miscIndexService.store(clientKey, userDetails);
        }
    }

    @Secured("ROLE_CAN_MANAGE_USERS")
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
    ApiResponse<Map<String, Integer>> getRoles() {
        logger.info("Current user is " + getUserName());
        ApiResponse<Map<String, Integer>> apiResponse = new ApiResponse<>();

        Map<String, Integer> roles = UnifiedMap.newMap();

        for (RoleEnum role : RoleEnum.values()) {
            roles.put(role.name(), role.getValue());
        }

        apiResponse.setPayload(roles);
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

