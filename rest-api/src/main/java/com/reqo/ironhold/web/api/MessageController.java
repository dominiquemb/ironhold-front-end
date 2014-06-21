package com.reqo.ironhold.web.api;

import com.gs.collections.impl.utility.ArrayIterate;
import com.gs.collections.impl.utility.ListIterate;
import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.storage.interfaces.IMetaDataIndexService;
import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.MessageSource;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.storage.model.search.MessageTypeEnum;
import com.reqo.ironhold.web.domain.*;
import com.reqo.ironhold.web.domain.responses.CountSearchResponse;
import com.reqo.ironhold.web.domain.responses.MessageSearchResponse;
import com.reqo.ironhold.web.domain.responses.SuggestSearchResponse;
import com.reqo.ironhold.web.support.ApiResponse;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
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
public class MessageController {
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
    ApiResponse<MessageSearchResponse> getMessagesWithFacetValues(@RequestParam final String criteria,
                                                                  @RequestParam(required = false, defaultValue = "SCORE") String sortField,
                                                                  @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
                                                                  @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                                  @RequestParam(required = false, defaultValue = "0") int page,
                                                                  @RequestBody FacetValue[] facetValues) {
        logger.info(String.format("getMessagesWithFacetValues %s, %s, %s, %d, %d, %s", criteria, sortField, sortOrder, pageSize, page, ArrayIterate.makeString(facetValues, ",")));
        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(getClientKey(), getLoginUser());

        searchBuilder.withCriteria(criteria).withResultsLimit(page * pageSize, pageSize);

        searchBuilder.withSort(IndexFieldEnum.valueOf(sortField), SortOrder.valueOf(sortOrder));
        for (FacetValue facetValue : facetValues) {
            FacetGroupName facetGroupName = FacetGroupName.fromValue(facetValue.getFacetName());
            searchBuilder.withNamedFacetValue(facetGroupName, facetValue.getLabel());
        }

        MessageSearchResponse result = messageIndexService.search(searchBuilder);

        final String clientKey = getClientKey();
        final LoginUser loginUser = getLoginUser();

        for (final MessageMatch match : result.getMessages()) {
            match.optimize();
            backgroundExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    metaDataIndexService.store(clientKey, new AuditLogMessage(loginUser, AuditActionEnum.PREVIEW, match.getFormattedIndexedMailMessage().getMessageId(), criteria));
                }
            });

        }

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

        final String clientKey = getClientKey();
        final LoginUser loginUser = getLoginUser();
        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(getClientKey(), loginUser);

        searchBuilder.withCriteria(criteria).withResultsLimit(page * pageSize, pageSize);

        searchBuilder.withSort(IndexFieldEnum.valueOf(sortField), SortOrder.valueOf(sortOrder));
        for (String facet : facets) {
            FacetGroupName facetGroupName = FacetGroupName.fromValue(facet);
            searchBuilder.withNamedFacet(facetGroupName);
        }

        MessageSearchResponse result = messageIndexService.search(searchBuilder);

        for (final MessageMatch match : result.getMessages()) {
            match.optimize();
            backgroundExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    metaDataIndexService.store(clientKey, new AuditLogMessage(loginUser, AuditActionEnum.PREVIEW, match.getFormattedIndexedMailMessage().getMessageId(), criteria));
                }
            });
        }

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


    @RequestMapping(method = RequestMethod.GET, value = "/advanced/{messageId:.+}")
    @Secured("ROLE_CAN_SEARCH")
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessageWithAdvancedCriteria(@RequestParam(required = false, defaultValue = "*") final String criteria,
                                                                       @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                                       @RequestParam(required = false, defaultValue = "0") int page,
                                                                       @RequestParam(required = false, defaultValue = "SCORE") String sortField,
                                                                       @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
                                                                       @RequestParam(required = false, defaultValue = "") String startDate,
                                                                       @RequestParam(required = false, defaultValue = "") String endDate,
                                                                       @RequestParam(required = false, defaultValue = "") String sender,
                                                                       @RequestParam(required = false, defaultValue = "") String recipient,
                                                                       @RequestParam(required = false, defaultValue = "") String subject,
                                                                       @RequestParam(required = false, defaultValue = "") String body,
                                                                       @RequestParam(required = false, defaultValue = "") String messageType,
                                                                       @RequestParam(required = false, defaultValue = "") String attachment,
                                                                       @PathVariable("messageId") String messageId
    ) {
        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        final String clientKey = getClientKey();
        final LoginUser loginUser = getLoginUser();
        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(getClientKey(), loginUser);

        searchBuilder.withResultsLimit(page * pageSize, pageSize);

        searchBuilder.withSort(IndexFieldEnum.valueOf(sortField), SortOrder.valueOf(sortOrder));

        final StringBuilder context = new StringBuilder();
        if (criteria.length()>0) {
            searchBuilder.withCriteria(criteria);
            context.append(criteria);
        }
        if (startDate.length()>0) {
            String[] chunks = startDate.split("/");
            searchBuilder.withStartDate(new DateTime(Integer.parseInt(chunks[2]), Integer.parseInt(chunks[0]), Integer.parseInt(chunks[1]), 0, 0).toDate());
            context.append(" from " + startDate);
        }

        if (endDate.length()>0) {
            String[] chunks = endDate.split("/");
            searchBuilder.withEndDate(new DateTime(Integer.parseInt(chunks[2]), Integer.parseInt(chunks[0]), Integer.parseInt(chunks[1]), 0, 0).toDate());
            context.append(" to " + endDate);
        }

        if (sender.length()>0) {
            searchBuilder.withSender(sender);
            context.append(" from " + sender);
        }

        if (recipient.length()>0) {
            searchBuilder.withRecipient(recipient);
            context.append(" to " + recipient);

        }

        if (subject.length()>0) {
            searchBuilder.withSubject(subject);
            context.append(" subject " + subject);

        }

        if (body.length()>0) {
            searchBuilder.withBody(body);
            context.append(" body " + body);

        }

        if (attachment.length()>0) {
            searchBuilder.withAttachment(attachment);
            context.append(" attachment " + attachment);

        }

        if (messageType.length()>0) {
            searchBuilder.withMessageType(MessageTypeEnum.getByValue(messageType));
            context.append(" message type " + messageType);

        }
        searchBuilder.withFullBody().withId(messageId, IndexedObjectType.MIME_MESSAGE);
        MessageSearchResponse result = messageIndexService.search(searchBuilder);

        for (final MessageMatch match : result.getMessages()) {
            match.optimize();
            backgroundExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    metaDataIndexService.store(clientKey, new AuditLogMessage(loginUser, AuditActionEnum.PREVIEW, match.getFormattedIndexedMailMessage().getMessageId(), context.toString()));
                }
            });
        }

        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                metaDataIndexService.store(clientKey, new AuditLogMessage(loginUser, AuditActionEnum.SEARCH, null, context.toString()));
            }
        });

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/advanced")
    @Secured("ROLE_CAN_SEARCH")
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessagesWithAdvancedCriteria(@RequestParam(required = false, defaultValue = "*") final String criteria,
                                                   @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false, defaultValue = "0") int page,
                                                   @RequestParam(required = false, defaultValue = "SCORE") String sortField,
                                                   @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
                                                   @RequestParam(required = false, defaultValue = "") String startDate,
                                                   @RequestParam(required = false, defaultValue = "") String endDate,
                                                   @RequestParam(required = false, defaultValue = "") String sender,
                                                   @RequestParam(required = false, defaultValue = "") String recipient,
                                                   @RequestParam(required = false, defaultValue = "") String subject,
                                                   @RequestParam(required = false, defaultValue = "") String body,
                                                   @RequestParam(required = false, defaultValue = "") String messageType,
                                                   @RequestParam(required = false, defaultValue = "") String attachment
                                                   ) {
        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        final String clientKey = getClientKey();
        final LoginUser loginUser = getLoginUser();
        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(getClientKey(), loginUser);

        searchBuilder.withResultsLimit(page * pageSize, pageSize);

        searchBuilder.withSort(IndexFieldEnum.valueOf(sortField), SortOrder.valueOf(sortOrder));

        final StringBuilder context = new StringBuilder();
        if (criteria.length()>0) {
            searchBuilder.withCriteria(criteria);
            context.append(criteria);
        }
        if (startDate.length()>0) {
            String[] chunks = startDate.split("/");
            searchBuilder.withStartDate(new DateTime(Integer.parseInt(chunks[2]), Integer.parseInt(chunks[0]), Integer.parseInt(chunks[1]), 0, 0).toDate());
            context.append(" from " + startDate);
        }

        if (endDate.length()>0) {
            String[] chunks = endDate.split("/");
            searchBuilder.withEndDate(new DateTime(Integer.parseInt(chunks[2]), Integer.parseInt(chunks[0]), Integer.parseInt(chunks[1]), 0, 0).toDate());
            context.append(" to " + endDate);
        }

        if (sender.length()>0) {
            searchBuilder.withSender(sender);
            context.append(" from " + sender);
        }

        if (recipient.length()>0) {
            searchBuilder.withRecipient(recipient);
            context.append(" to " + recipient);

        }

        if (subject.length()>0) {
            searchBuilder.withSubject(subject);
            context.append(" subject " + subject);

        }

        if (body.length()>0) {
            searchBuilder.withBody(body);
            context.append(" body " + body);

        }

        if (attachment.length()>0) {
            searchBuilder.withAttachment(attachment);
            context.append(" attachment " + attachment);

        }

        if (messageType.length()>0) {
            searchBuilder.withMessageType(MessageTypeEnum.getByValue(messageType));
            context.append(" message type " + messageType);

        }

        MessageSearchResponse result = messageIndexService.search(searchBuilder);

        for (final MessageMatch match : result.getMessages()) {
            match.optimize();
            backgroundExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    metaDataIndexService.store(clientKey, new AuditLogMessage(loginUser, AuditActionEnum.PREVIEW, match.getFormattedIndexedMailMessage().getMessageId(), context.toString()));
                }
            });
        }

        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                metaDataIndexService.store(clientKey, new AuditLogMessage(loginUser, AuditActionEnum.SEARCH, null, context.toString()));
            }
        });

        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/count/advanced")
    @Secured("ROLE_CAN_SEARCH")
    public
    @ResponseBody
    ApiResponse<CountSearchResponse> getMessagesWithAdvancedCriteriaCount(@RequestParam(required = false, defaultValue = "*") final String criteria,
                                                                       @RequestParam(required = false, defaultValue = "10") int pageSize,
                                                                       @RequestParam(required = false, defaultValue = "0") int page,
                                                                       @RequestParam(required = false, defaultValue = "SCORE") String sortField,
                                                                       @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
                                                                       @RequestParam(required = false, defaultValue = "") String startDate,
                                                                       @RequestParam(required = false, defaultValue = "") String endDate,
                                                                       @RequestParam(required = false, defaultValue = "") String sender,
                                                                       @RequestParam(required = false, defaultValue = "") String recipient,
                                                                       @RequestParam(required = false, defaultValue = "") String subject,
                                                                       @RequestParam(required = false, defaultValue = "") String body,
                                                                       @RequestParam(required = false, defaultValue = "") String messageType,
                                                                       @RequestParam(required = false, defaultValue = "") String attachment
    ) {
        ApiResponse<CountSearchResponse> apiResponse = new ApiResponse<>();

        final String clientKey = getClientKey();
        final LoginUser loginUser = getLoginUser();
        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(getClientKey(), loginUser);

        searchBuilder.withResultsLimit(page * pageSize, pageSize);

        searchBuilder.withSort(IndexFieldEnum.valueOf(sortField), SortOrder.valueOf(sortOrder));

        final StringBuilder context = new StringBuilder();
        if (criteria.length()>0) {
            searchBuilder.withCriteria(criteria);
            context.append(criteria);
        }
        if (startDate.length()>0) {
            String[] chunks = startDate.split("/");
            searchBuilder.withStartDate(new DateTime(Integer.parseInt(chunks[2]), Integer.parseInt(chunks[0]), Integer.parseInt(chunks[1]), 0, 0).toDate());
            context.append(" from " + startDate);
        }

        if (endDate.length()>0) {
            String[] chunks = endDate.split("/");
            searchBuilder.withEndDate(new DateTime(Integer.parseInt(chunks[2]), Integer.parseInt(chunks[0]), Integer.parseInt(chunks[1]), 0, 0).toDate());
            context.append(" to " + endDate);
        }

        if (sender.length()>0) {
            searchBuilder.withSender(sender);
            context.append(" from " + sender);
        }

        if (recipient.length()>0) {
            searchBuilder.withRecipient(recipient);
            context.append(" to " + recipient);

        }

        if (subject.length()>0) {
            searchBuilder.withSubject(subject);
            context.append(" subject " + subject);

        }

        if (body.length()>0) {
            searchBuilder.withBody(body);
            context.append(" body " + body);

        }

        if (attachment.length()>0) {
            searchBuilder.withAttachment(attachment);
            context.append(" attachment " + attachment);

        }

        if (messageType.length()>0) {
            searchBuilder.withMessageType(MessageTypeEnum.getByValue(messageType));
            context.append(" message type " + messageType);

        }

        CountSearchResponse result = messageIndexService.getMatchCount(searchBuilder, getLoginUser());
        apiResponse.setPayload(result);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{messageId:.+}")
    public
    @ResponseBody
    ApiResponse<MessageSearchResponse> getMessage(@PathVariable("messageId") String messageId, @RequestParam final String criteria) {
        logger.info(String.format("getMessage %s %s", messageId, criteria));

        ApiResponse<MessageSearchResponse> apiResponse = new ApiResponse<>();

        MessageSearchBuilder searchBuilder = messageIndexService.getNewBuilder(getClientKey(), getLoginUser());
        searchBuilder.withCriteria(criteria).withFullBody().withId(messageId, IndexedObjectType.MIME_MESSAGE);

        MessageSearchResponse result = messageIndexService.search(searchBuilder);

        final String clientKey = getClientKey();
        final LoginUser loginUser = getLoginUser();

        for (final MessageMatch match : result.getMessages()) {
            match.optimize();
            backgroundExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    metaDataIndexService.store(clientKey, new AuditLogMessage(loginUser, AuditActionEnum.VIEW, match.getFormattedIndexedMailMessage().getMessageId(), criteria));
                }
            });

        }

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
                                @PathVariable("messageId") final String messageId) throws Exception {
        logger.info(String.format("getBody %d %d %d %s", year, month, day, messageId));

        ApiResponse<String> response = new ApiResponse<>();

        String partition = String.format("%4d", year);
        String subPartition = String.format("%02d%02d", month, day);
        String source = mimeMailMessageStorageService.get(getClientKey(), partition, subPartition, messageId);
        MimeMailMessage message = new MimeMailMessage();
        message.loadMimeMessageFromSource(source);


        if (message.getBodyHTML() == null || message.getBodyHTML().trim().length() == 0) {
            response.setPayload("<pre>"+message.getBody()+"</pre>");
        } else {
            response.setPayload(message.getBodyHTML().replaceAll("http://","https://"));
        }

        final String clientKey = getClientKey();
        final LoginUser loginUser = getLoginUser();

        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                metaDataIndexService.store(clientKey, new AuditLogMessage(loginUser, AuditActionEnum.VIEW, messageId));
            }
        });

        response.setStatus(ApiResponse.STATUS_SUCCESS);
        return response;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{messageId:.+}/audit")
    public
    @ResponseBody
    ApiResponse<List<ViewableAuditLogMessage>> getMessageAudit(@PathVariable("messageId") String messageId) {
        logger.info(String.format("getMessageAudit %s", messageId));

        ApiResponse<List<ViewableAuditLogMessage>> apiResponse = new ApiResponse<>();

        List<AuditLogMessage> result = metaDataIndexService.getAuditLogMessages(getClientKey(), messageId);

        List<ViewableAuditLogMessage> response = ListIterate.collect(result, ViewableAuditLogMessage.FROM_AUDIT_LOG_MESSAGE);
        apiResponse.setPayload(response);
        apiResponse.setStatus(ApiResponse.STATUS_SUCCESS);

        return apiResponse;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{messageId:.+}/logs")
    public
    @ResponseBody
    ApiResponse<List<LogMessage>> getMessageLogs(@PathVariable("messageId") String messageId) {
        logger.info(String.format("getMessageLogs %s", messageId));

        ApiResponse<List<LogMessage>> apiResponse = new ApiResponse<>();

        List<LogMessage> result = metaDataIndexService.getLogMessages(getClientKey(), messageId);

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

