package com.reqo.ironhold.web.api;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.storage.interfaces.IMetaDataIndexService;
import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.storage.model.message.source.MessageSource;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.web.domain.*;
import com.reqo.ironhold.web.domain.responses.CountSearchResponse;
import com.reqo.ironhold.web.domain.responses.MessageSearchResponse;
import com.reqo.ironhold.web.domain.responses.SuggestSearchResponse;
import com.reqo.ironhold.web.support.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 9:01 AM
 */
@Controller
@Secured("ROLE_CAN_LOGIN")
@RequestMapping(value = "/messages")
public class MessageController extends AbstractController {
    @Autowired
    protected IMessageIndexService messageIndexService;
    @Autowired
    protected IMiscIndexService miscIndexService;
    @Autowired
    protected IMetaDataIndexService metaDataIndexService;
    @Autowired
    protected IMimeMailMessageStorageService mimeMailMessageStorageService;

    private final ExecutorService backgroundExecutor;
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    public MessageController() {
        this.backgroundExecutor = Executors.newFixedThreadPool(10);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/count")
    public
    @ResponseBody
    ApiResponse<CountSearchResponse> getCount(@RequestParam(required = false, defaultValue = "*") String criteria) {
        ApiResponse<CountSearchResponse> apiResponse = new ApiResponse<CountSearchResponse>();

        CountSearchResponse result = messageIndexService.getMatchCount(getClientKey(), criteria, getLoginUser());
        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/suggest")
    public
    @ResponseBody
    ApiResponse<SuggestSearchResponse> getSuggestions(@RequestParam String criteria) {
        ApiResponse<SuggestSearchResponse> apiResponse = new ApiResponse<SuggestSearchResponse>();

        SuggestSearchResponse result = messageIndexService.getSuggestions(getClientKey(), criteria, getLoginUser());
        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessages(@RequestParam String criteria,
                                                   @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false, defaultValue = "0") int page,
                                                   @RequestBody FacetValue[] facetValues) {
        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(getClientKey(), getLoginUser());

        searchBuilder.withCriteria(criteria).withResultsLimit(page, pageSize);


        for (FacetValue facetValue : facetValues) {
            FacetGroupName facetGroupName = FacetGroupName.fromValue(facetValue.getFacetName());
            searchBuilder.withNamedFacetValue(facetGroupName, facetValue.getLabel());
        }

        MessageSearchResponse result = messageIndexService.search(searchBuilder);


        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessages(@RequestParam final String criteria,
                                                   @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false, defaultValue = "0") int page,
                                                   @RequestParam(required = false, defaultValue = "") String[] facets) {
        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        final LoginUser loginUser = getLoginUser();
        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(getClientKey(), loginUser);

        searchBuilder.withCriteria(criteria).withResultsLimit(page, pageSize);


        for (String facet : facets) {
            FacetGroupName facetGroupName = FacetGroupName.fromValue(facet);
            searchBuilder.withNamedFacet(facetGroupName);
        }

        MessageSearchResponse result = messageIndexService.search(searchBuilder);

        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                metaDataIndexService.store(getClientKey(), new AuditLogMessage(loginUser, AuditActionEnum.SEARCH, null, criteria));

            }
        });

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{messageId:.+}")
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessage(@PathVariable("messageId") String messageId, @RequestParam String criteria) {
        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(getClientKey(), getLoginUser());
        searchBuilder.withCriteria(criteria).withFullBody().withId(messageId, IndexedObjectType.MIME_MESSAGE);

        MessageSearchResponse result = messageIndexService.search(searchBuilder);

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{messageId:.+}/sources")
    public
    @ResponseBody
    ApiResponse<List<MessageSource>> getMessageSources(@PathVariable("messageId") String messageId) {
        ApiResponse<List<MessageSource>> apiResponse = new ApiResponse<>();

        List<MessageSource> result = metaDataIndexService.getSources(getClientKey(), messageId);

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{year}/{month}/{day}/{messageId:.+}/download")
    public
    @ResponseBody
    String getRawMessage(@PathVariable("year") int year,
                         @PathVariable("month") int month,
                         @PathVariable("day") int day,
                         @PathVariable("messageId") String messageId) throws Exception {
        String partition = String.format("%4d", year);
        String subPartition = String.format("%02d%02d", month, day);
        return mimeMailMessageStorageService.get(getClientKey(), partition, subPartition, messageId);

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{messageId:.+}/audit")
    public
    @ResponseBody
    ApiResponse<List<AuditLogMessage>> getMessageAudit(@PathVariable("messageId") String messageId) {
        ApiResponse<List<AuditLogMessage>> apiResponse = new ApiResponse<>();

        List<AuditLogMessage> result = metaDataIndexService.getAuditLogMessages(getClientKey(), messageId);

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

