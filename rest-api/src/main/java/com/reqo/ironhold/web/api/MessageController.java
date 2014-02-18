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
@RequestMapping(value = "/messages")
public class MessageController {
    private final ExecutorService backgroundExecutor;
    private IMessageIndexService messageIndexService;
    private IMiscIndexService miscIndexService;
    private IMetaDataIndexService metaDataIndexService;
    private IMimeMailMessageStorageService mimeMailMessageStorageService;

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    private LoginUser getDefaultUser(String clientKey, String username) {
        return miscIndexService.getLoginUser(clientKey, username);
    }

    @Inject
    public MessageController(IMessageIndexService messageIndexService, IMiscIndexService miscIndexService, IMetaDataIndexService metaDataIndexService, IMimeMailMessageStorageService mimeMailMessageStorageService) {
        this.messageIndexService = messageIndexService;
        this.miscIndexService = miscIndexService;
        this.metaDataIndexService = metaDataIndexService;
        this.mimeMailMessageStorageService = mimeMailMessageStorageService;

        this.backgroundExecutor = Executors.newFixedThreadPool(10);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}/{username}/count")
    public
    @ResponseBody
    ApiResponse<CountSearchResponse> getCount(@PathVariable("clientKey") String clientKey, @PathVariable("username") String username, @RequestParam(required = false, defaultValue = "*") String criteria) {
        ApiResponse<CountSearchResponse> apiResponse = new ApiResponse<CountSearchResponse>();

        CountSearchResponse result = messageIndexService.getMatchCount(clientKey, criteria, getDefaultUser(clientKey, username));
        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}/{username}/suggest")
    public
    @ResponseBody
    ApiResponse<SuggestSearchResponse> getSuggestions(@PathVariable("clientKey") String clientKey, @PathVariable("username") String username, @RequestParam String criteria) {
        ApiResponse<SuggestSearchResponse> apiResponse = new ApiResponse<SuggestSearchResponse>();

        SuggestSearchResponse result = messageIndexService.getSuggestions(clientKey, criteria, getDefaultUser(clientKey, username));
        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.POST, value = "/{clientKey}/{username}")
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessages(@PathVariable("clientKey") String clientKey,
                                                   @PathVariable("username") String username,
                                                   @RequestParam String criteria,
                                                   @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false, defaultValue = "0") int page,
                                                   @RequestBody FacetValue[] facetValues) {
        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(clientKey, getDefaultUser(clientKey, username));

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

    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}/{username}")
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessages(@PathVariable("clientKey") final String clientKey,
                                                   @PathVariable("username") String username,
                                                   @RequestParam final String criteria,
                                                   @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false, defaultValue = "0") int page,
                                                   @RequestParam(required = false, defaultValue = "") String[] facets) {
        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        final LoginUser loginUser = getDefaultUser(clientKey, username);
        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(clientKey, loginUser);

        searchBuilder.withCriteria(criteria).withResultsLimit(page, pageSize);


        for (String facet : facets) {
            FacetGroupName facetGroupName = FacetGroupName.fromValue(facet);
            searchBuilder.withNamedFacet(facetGroupName);
        }

        MessageSearchResponse result = messageIndexService.search(searchBuilder);

        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                metaDataIndexService.store(clientKey, new AuditLogMessage(loginUser, AuditActionEnum.SEARCH, null, criteria));

            }
        });

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}/{username}/{messageId:.+}")
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessage(@PathVariable("clientKey") String clientKey, @PathVariable("username") String username, @PathVariable("messageId") String messageId, @RequestParam String criteria) {
        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(clientKey, getDefaultUser(clientKey, username));
        searchBuilder.withCriteria(criteria).withFullBody().withId(messageId, IndexedObjectType.MIME_MESSAGE);

        MessageSearchResponse result = messageIndexService.search(searchBuilder);

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}/{username}/{messageId:.+}/sources")
    public
    @ResponseBody
    ApiResponse<List<MessageSource>> getMessageSources(@PathVariable("clientKey") String clientKey, @PathVariable("username") String username, @PathVariable("messageId") String messageId) {
        ApiResponse<List<MessageSource>> apiResponse = new ApiResponse<>();

        List<MessageSource> result = metaDataIndexService.getSources(clientKey, messageId);

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}/{username}/{year}/{month}/{day}/{messageId:.+}/download")
    public
    @ResponseBody
    String getRawMessage(@PathVariable("clientKey") String clientKey,
                         @PathVariable("username") String username,
                         @PathVariable("year") int year,
                         @PathVariable("month") int month,
                         @PathVariable("day") int day,
                         @PathVariable("messageId") String messageId) throws Exception {
        String partition = String.format("%4d", year);
        String subPartition = String.format("%02d%02d", month, day);
        return mimeMailMessageStorageService.get(clientKey, partition, subPartition, messageId);

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}/{username}/{messageId:.+}/audit")
    public
    @ResponseBody
    ApiResponse<List<AuditLogMessage>> getMessageAudit(@PathVariable("clientKey") String clientKey,
                                                       @PathVariable("username") String username,
                                                       @PathVariable("messageId") String messageId) {
        ApiResponse<List<AuditLogMessage>> apiResponse = new ApiResponse<>();

        List<AuditLogMessage> result = metaDataIndexService.getAuditLogMessages(clientKey, messageId);

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

}

