package com.reqo.ironhold.web.api;

import com.gs.collections.api.map.MutableMap;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.storage.interfaces.IMetaDataIndexService;
import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.web.domain.Attachment;
import com.reqo.ironhold.web.domain.LoginUser;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 9:01 AM
 */
@Controller
@RequestMapping(value = "/download")
public class DownloadController extends AbstractController {
    @Autowired
    protected IMessageIndexService messageIndexService;
    @Autowired
    protected IMiscIndexService miscIndexService;
    @Autowired
    protected IMetaDataIndexService metaDataIndexService;
    @Autowired
    protected IMimeMailMessageStorageService mimeMailMessageStorageService;

    private final ExecutorService backgroundExecutor;
    private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

    private class AttachmentRequest extends FullDownloadRequest {
        private final String attachment;

        private AttachmentRequest(String clientKey, String user, int year, int month, int day, String messageId, String attachment) {
            super(clientKey, user, year, month, day, messageId);
            this.attachment = attachment;
        }
    }

    private class FullDownloadRequest {
        protected final String clientKey;
        protected final String user;
        protected final int year;
        protected final int month;
        protected final int day;
        protected final String messageId;

        private FullDownloadRequest(String clientKey, String user, int year, int month, int day, String messageId) {
            this.clientKey = clientKey;
            this.user = user;
            this.year = year;
            this.month = month;
            this.day = day;
            this.messageId = messageId;
        }
    }

    private MutableMap<String, AttachmentRequest> attachmentRequests = UnifiedMap.newMap();
    private MutableMap<String, FullDownloadRequest> fullDownloadRequests = UnifiedMap.newMap();

    public DownloadController() {
        this.backgroundExecutor = Executors.newFixedThreadPool(10);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/{year}/{month}/{day}/{messageId:.+}")
    @Secured("ROLE_CAN_SEARCH")
    public synchronized
    @ResponseBody
    String getRawMessageRequest(@PathVariable("year") int year,
                                @PathVariable("month") int month,
                                @PathVariable("day") int day,
                                @PathVariable("messageId") String messageId) throws Exception {

        FullDownloadRequest fullDownloadRequest = new FullDownloadRequest(getClientKey(), getUserName(), year, month, day, messageId);

        String id = UUID.randomUUID().toString();

        fullDownloadRequests.put(id, fullDownloadRequest);

        return id;
    }

    // This is public on purpose
    @RequestMapping(method = RequestMethod.GET, value = "/full/{id}")
    public synchronized void getRawMessage(@PathVariable("id") String id,
                         HttpServletResponse response) throws Exception {
        if (!fullDownloadRequests.containsKey(id)) {
            throw new IllegalArgumentException("Unknown request id " + id);
        }
        try {
            FullDownloadRequest fullDownloadRequest = fullDownloadRequests.get(id);

            int year = fullDownloadRequest.year;
            int month = fullDownloadRequest.month;
            int day = fullDownloadRequest.day;
            String messageId = fullDownloadRequest.messageId;
            String clientKey = fullDownloadRequest.clientKey;

            logger.info(String.format("getRawMessage %d %d %d %s", year, month, day, messageId));

            String partition = String.format("%4d", year);
            String subPartition = String.format("%02d%02d", month, day);
            String result = mimeMailMessageStorageService.get(clientKey, partition, subPartition, messageId);

            byte[] byteArray = result.getBytes();
            response.setContentType("text/plain");
            response.setContentLength(byteArray.length);
            response.setHeader("Content-Disposition", "attachment; filename=" + FilenameUtils.normalize(messageId.replaceAll("<","").replaceAll(">","").replaceAll("-","").replaceAll("\\\\","").replaceAll("/","")) + ".eml");

            InputStream is = new ByteArrayInputStream(byteArray);
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } finally {
            if (fullDownloadRequests.containsKey(id)) {
                fullDownloadRequests.removeKey(id);
            }
        }
    }


    @RequestMapping(method = RequestMethod.POST, value = "/{year}/{month}/{day}/{messageId:.+}/{attachment:.+}")
    @Secured("ROLE_CAN_SEARCH")
    public synchronized
    @ResponseBody
    String getRawAttachmentRequest(@PathVariable("year") int year,
                                   @PathVariable("month") int month,
                                   @PathVariable("day") int day,
                                   @PathVariable("messageId") String messageId,
                                   @PathVariable("attachment") String attachment) throws Exception {

        AttachmentRequest attachmentRequest = new AttachmentRequest(getClientKey(), getUserName(), year, month, day, messageId, attachment);

        String id = UUID.randomUUID().toString();

        attachmentRequests.put(id, attachmentRequest);

        return id;
    }

    // This is public on purpose
    @RequestMapping(method = RequestMethod.GET, value = "/attachment/{id}")
    public synchronized void getRawAttachment(@PathVariable("id") String id,
                                              HttpServletResponse response) throws Exception {


        if (!attachmentRequests.containsKey(id)) {
            throw new IllegalArgumentException("Unknown request id " + id);
        }
        try {
            AttachmentRequest attachmentRequest = attachmentRequests.get(id);

            int year = attachmentRequest.year;
            int month = attachmentRequest.month;
            int day = attachmentRequest.day;
            String messageId = attachmentRequest.messageId;
            String attachment = attachmentRequest.attachment;
            String clientKey = attachmentRequest.clientKey;

            logger.info(String.format("getRawAttachment %d %d %d %s %s", year, month, day, messageId, attachment));

            String partition = String.format("%4d", year);
            String subPartition = String.format("%02d%02d", month, day);
            String source = mimeMailMessageStorageService.get(clientKey, partition, subPartition, messageId);
            MimeMailMessage message = new MimeMailMessage();
            message.loadMimeMessageFromSource(source);

            if (message.isHasAttachments()) {
                for (Attachment attachmentObject : message.getAttachments()) {
                    if (attachmentObject.getFileName().equals(attachment)) {
                        byte[] byteArray = Base64.decodeBase64(attachmentObject.getBody().getBytes());
                        response.setContentType(attachmentObject.getContentType());
                        response.setContentLength(byteArray.length);
                        response.setHeader("Content-Disposition", "attachment; filename=" + attachmentObject.getFileName());

                        InputStream is = new ByteArrayInputStream(byteArray);
                        IOUtils.copy(is, response.getOutputStream());
                        response.flushBuffer();
                        return;
                    }

                }
            }

            throw new IllegalArgumentException(attachment + " not found");
        } finally {
            if (attachmentRequests.containsKey(id)) {
                attachmentRequests.removeKey(id);
            }
        }
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

