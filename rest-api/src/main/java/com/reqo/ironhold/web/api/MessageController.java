package com.reqo.ironhold.web.api;

import com.gs.collections.impl.utility.ArrayIterate;
import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.storage.interfaces.IMetaDataIndexService;
import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.MessageSource;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.web.domain.*;
import com.reqo.ironhold.web.domain.responses.CountSearchResponse;
import com.reqo.ironhold.web.domain.responses.MessageSearchResponse;
import com.reqo.ironhold.web.domain.responses.SuggestSearchResponse;
import com.reqo.ironhold.web.support.ApiResponse;
import org.elasticsearch.search.sort.SortOrder;
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
    @Secured("ROLE_CAN_SEARCH")
    public
    @ResponseBody
    ApiResponse<CountSearchResponse> getCount(@RequestParam(required = false, defaultValue = "*") String criteria) {
        logger.info(String.format("getCount %s", criteria));
        ApiResponse<CountSearchResponse> apiResponse = new ApiResponse<CountSearchResponse>();

        CountSearchResponse result = messageIndexService.getMatchCount(getClientKey(), criteria, getLoginUser());
        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/suggest")
    @Secured("ROLE_CAN_SEARCH")
    public
    @ResponseBody
    ApiResponse<SuggestSearchResponse> getSuggestions(@RequestParam String criteria) {
        logger.info(String.format("getSuggestions %s", criteria));

        ApiResponse<SuggestSearchResponse> apiResponse = new ApiResponse<SuggestSearchResponse>();

        SuggestSearchResponse result = messageIndexService.getSuggestions(getClientKey(), criteria, getLoginUser());
        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.POST)
    @Secured("ROLE_CAN_SEARCH")
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessagesWithFacetValues(@RequestParam String criteria,
                                                   @RequestParam(required = false, defaultValue = "SCORE") String sortField,
                                                   @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
                                                   @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false, defaultValue = "0") int page,
                                                   @RequestBody FacetValue[] facetValues) {
        logger.info(String.format("getMessagesWithFacetValues %s, %s, %s, %d, %d, %s", criteria, sortField, sortOrder, pageSize, page, ArrayIterate.makeString(facetValues, ",")));
        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(getClientKey(), getLoginUser());

        searchBuilder.withCriteria(criteria).withResultsLimit(page*pageSize, pageSize);

        searchBuilder.withSort(IndexFieldEnum.valueOf(sortField), SortOrder.valueOf(sortOrder));
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
    @Secured("ROLE_CAN_SEARCH")
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessages(@RequestParam final String criteria,
                                                   @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false, defaultValue = "0") int page,
                                                   @RequestParam(required = false, defaultValue = "SCORE") String sortField,
                                                   @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
                                                   @RequestParam(required = false, defaultValue = "") String[] facets) {
        logger.info(String.format("getMessages %s, %s, %s, %d, %d, %s", criteria, sortField, sortOrder, pageSize, page, ArrayIterate.makeString(facets, ",")));

        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        final LoginUser loginUser = getLoginUser();
        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(getClientKey(), loginUser);

        searchBuilder.withCriteria(criteria).withResultsLimit(page*pageSize, pageSize);

        searchBuilder.withSort(IndexFieldEnum.valueOf(sortField), SortOrder.valueOf(sortOrder));
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
        logger.info(String.format("getMessage %s %s", messageId, criteria));

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
        logger.info(String.format("getMessageSources %s", messageId));

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
        logger.info(String.format("getRawMessage %d %d %d %s", year, month, day, messageId));

        String partition = String.format("%4d", year);
        String subPartition = String.format("%02d%02d", month, day);
        return mimeMailMessageStorageService.get(getClientKey(), partition, subPartition, messageId);

    }


    @RequestMapping(method = RequestMethod.GET, value = "/{year}/{month}/{day}/{messageId:.+}/download/{attachment:.+}")
    public
    @ResponseBody
    String getRawAttachment(@PathVariable("year") int year,
                         @PathVariable("month") int month,
                         @PathVariable("day") int day,
                         @PathVariable("messageId") String messageId,
                         @PathVariable("attachment") String attachment) throws Exception {
        logger.info(String.format("getRawAttachment %d %d %d %s %s", year, month, day, messageId, attachment));

        String partition = String.format("%4d", year);
        String subPartition = String.format("%02d%02d", month, day);
        String source = mimeMailMessageStorageService.get(getClientKey(), partition, subPartition, messageId);
        MimeMailMessage message = new MimeMailMessage();
        message.loadMimeMessageFromSource(source);

        if (message.isHasAttachments()) {
            for (Attachment attachmentObject : message.getAttachments()) {
                if (attachmentObject.getFileName().equals(attachment)) {
                    return attachmentObject.getBody();
                }

            }
        }

        throw new IllegalArgumentException(attachment + " not found");
    }


    @RequestMapping(method = RequestMethod.GET, value = "/{year}/{month}/{day}/{messageId:.+}/headers")
    public
    @ResponseBody
    ApiResponse<Map<String, String>> getHeaders(@PathVariable("year") int year,
                                                @PathVariable("month") int month,
                                                @PathVariable("day") int day,
                                                @PathVariable("messageId") String messageId) throws Exception {
        logger.info(String.format("getHeaders %d %d %d %s", year, month, day, messageId));

        ApiResponse<Map<String, String>> response = new ApiResponse<>();

        String partition = String.format("%4d", year);
        String subPartition = String.format("%02d%02d", month, day);
        String source = mimeMailMessageStorageService.get(getClientKey(), partition, subPartition, messageId);
        MimeMailMessage message = new MimeMailMessage();
        message.loadMimeMessageFromSource(source);


        response.setPayload(message.getHeaders());
        response.setStatus(ApiResponse.STATUS_SUCCESS);
        return response;
    }



    @RequestMapping(method = RequestMethod.GET, value = "/{year}/{month}/{day}/{messageId:.+}/body")
    public
    @ResponseBody
    ApiResponse<String> getBody(@PathVariable("year") int year,
                                                @PathVariable("month") int month,
                                                @PathVariable("day") int day,
                                                @PathVariable("messageId") String messageId) throws Exception {
        logger.info(String.format("getBody %d %d %d %s", year, month, day, messageId));

        ApiResponse<String> response = new ApiResponse<>();

        String partition = String.format("%4d", year);
        String subPartition = String.format("%02d%02d", month, day);
        String source = mimeMailMessageStorageService.get(getClientKey(), partition, subPartition, messageId);
        MimeMailMessage message = new MimeMailMessage();
        message.loadMimeMessageFromSource(source);


        response.setPayload(message.getBodyHTML() != null ? message.getBodyHTML() : message.getBody());
        response.setStatus(ApiResponse.STATUS_SUCCESS);
        return response;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{messageId:.+}/audit")
    public
    @ResponseBody
    ApiResponse<List<AuditLogMessage>> getMessageAudit(@PathVariable("messageId") String messageId) {
        logger.info(String.format("getMessageAudit %s", messageId));

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

