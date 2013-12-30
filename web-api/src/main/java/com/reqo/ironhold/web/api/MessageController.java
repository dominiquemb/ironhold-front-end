package com.reqo.ironhold.web.api;

import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.web.domain.FacetGroupName;
import com.reqo.ironhold.web.domain.FacetValue;
import com.reqo.ironhold.web.domain.LoginChannelEnum;
import com.reqo.ironhold.web.domain.LoginUser;
import com.reqo.ironhold.web.domain.responses.CountSearchResponse;
import com.reqo.ironhold.web.domain.responses.MessageSearchResponse;
import com.reqo.ironhold.web.domain.responses.SuggestSearchResponse;
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

    @RequestMapping(method = RequestMethod.POST, value = "/{clientKey}")
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessages(@PathVariable("clientKey") String clientKey,
                                                   @RequestParam String criteria,
                                                   @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false, defaultValue = "0") int page,
                                                   @RequestBody FacetValue[] facetValues) {
        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(clientKey, getDefaultUser());

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
            FacetGroupName facetGroupName = FacetGroupName.fromValue(facet);
            searchBuilder.withNamedFacet(facetGroupName);
        }

        MessageSearchResponse result = messageIndexService.search(searchBuilder);

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{clientKey}/{messageId:.+}")
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessage(@PathVariable("clientKey") String clientKey, @PathVariable("messageId") String messageId, @RequestParam String criteria) {
        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(clientKey, getDefaultUser());
        searchBuilder.withCriteria(criteria).withFullBody().withId(messageId, IndexedObjectType.MIME_MESSAGE);

        MessageSearchResponse result = messageIndexService.search(searchBuilder);

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

}

