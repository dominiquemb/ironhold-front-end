package com.reqo.ironhold.web.api;

import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.storage.model.user.LoginChannelEnum;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.domain.*;
import com.reqo.ironhold.web.support.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 9:01 AM
 */
@Controller
@RequestMapping(value = "/messages")
public class MessageController {
    private IMessageIndexService messageIndexService;
    private IMiscIndexService miscIndexService;

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Inject
    public MessageController(IMessageIndexService messageIndexService, IMiscIndexService miscIndexService) {
        this.messageIndexService = messageIndexService;
        this.miscIndexService = miscIndexService;
    }


    private LoginUser getDefaultUser() {
        return miscIndexService.authenticate("demo", "demo", "demo", LoginChannelEnum.WEB_APP, "127.0.0.1");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}/count")
    public
    @ResponseBody
    ApiResponse<CountSearchResponse> getCount(@PathVariable("clientKey") String clientKey, @RequestParam(required = false, defaultValue = "*") String criteria) {
        ApiResponse<CountSearchResponse> apiResponse = new ApiResponse<CountSearchResponse>();

        CountSearchResponse result = messageIndexService.getMatchCount(clientKey, criteria, getDefaultUser());
        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}/suggest")
    public
    @ResponseBody
    ApiResponse<SuggestSearchResponse> getSuggestions(@PathVariable("clientKey") String clientKey, @RequestParam String criteria) {
        ApiResponse<SuggestSearchResponse> apiResponse = new ApiResponse<SuggestSearchResponse>();

        SuggestSearchResponse result = messageIndexService.getSuggestions(clientKey, criteria, getDefaultUser());
        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}")
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessages(@PathVariable("clientKey") String clientKey,
                                                   @RequestParam String criteria,
                                                   @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false, defaultValue = "0") int page,
                                                   @RequestParam(required = false, defaultValue = "") String[] facets) {
        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(clientKey, getDefaultUser());

        searchBuilder.withCriteria(criteria).withResultsLimit(page, pageSize);


        for (String facet : facets) {
            String[] chunks = facet.split(":");
            String facetName = chunks[0];
            FacetGroupName facetGroupName = FacetGroupName.fromValue(facetName);
            searchBuilder.withNamedFacet(facetGroupName);

            if (chunks.length > 1) {
                // has facet values
                String facetValuesString = chunks[1];
                String[] facetValues = facetValuesString.split(",");
                for (String facetValue : facetValues) {
                    searchBuilder.withNamedFacetValue(facetGroupName, facetValue);
                }
            }
        }

        MessageSearchResponse result = messageIndexService.search(searchBuilder);

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}/{partition}/{messageId:.+}")
    public
    @ResponseBody
    ApiResponse<IndexedMailMessage> getMessage(@PathVariable("clientKey") String clientKey, @PathVariable("partition") String partition, @PathVariable("messageId") String messageId) {
        ApiResponse<IndexedMailMessage> apiResponse = new ApiResponse<>();

        IndexedMailMessage message = messageIndexService.getById(clientKey, partition, messageId);
        apiResponse.setPayload(message);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

}

