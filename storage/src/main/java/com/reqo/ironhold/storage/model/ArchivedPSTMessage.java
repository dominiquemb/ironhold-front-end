package com.reqo.ironhold.storage.model;

import com.reqo.ironhold.storage.utils.Compression;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Arrays;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArchivedPSTMessage {
    private long priority;
    private String internetMessageId;
    private String messageClass;
    private String subject;
    private long importance;
    private Date clientSubmitTime;
    private String receivedByName;
    private String sentRepresentingName;
    private String sentRepresentingAddressType;
    private String sentRepresentingEmailAddress;
    private String conversationTopic;
    private String receivedByAddressType;
    private String receivedByAddress;
    private String transportMessageHeaders;
    private Boolean read;
    private Boolean unmodified;
    private Boolean submitted;
    private Boolean unsent;
    private Boolean fromMe;
    private Boolean associated;
    private Boolean resent;
    private long acknowledgementMode;
    private Boolean originatorDeliveryReportRequested;
    private Boolean readReceiptRequested;
    private Boolean recipientReassignmentProhibited;
    private long originalSensitivity;
    private long sensitivity;
    private String pidTagSentRepresentingSearchKey;
    private String rcvdRepresentingName;
    private String originalSubject;
    private String replyRecipientNames;
    private Boolean messageToMe;
    private Boolean messageCcMe;
    private Boolean responseRequested;
    private String sentRepresentingAddrtype;
    private String originalDisplayBcc;
    private Boolean rtfinSync;
    private String originalDisplayCc;
    private String originalDisplayTo;
    private String rcvdRepresentingAddrtype;
    private String rcvdRepresentingEmailAddress;
    private Boolean nonReceiptNotificationRequested;
    private Boolean originatorNonDeliveryReportRequested;
    private long recipientType;
    private Boolean replyRequested;
    private String senderEntryId;
    private String senderName;
    private String senderAddrtype;
    private String senderEmailAddress;
    private long messageSize;
    private int internetArticleNumber;
    private String primarySendAccount;
    private String nextSendAcct;
    private long urlcompNamePostfix;
    private long objectType;
    private Boolean deleteAfterSubmit;
    private Boolean responsibility;
    private Boolean urlcompNameSet;
    private String displayBCC;
    private String displayCC;
    private String displayTo;
    private Date messageDeliveryTime;
    private String body;
    private String bodyPrefix;
    private long rtfsyncBodyCRC;
    private long rtfsyncBodyCount;
    private String rtfsyncBodyTag;
    private long rtfsyncPrefixCount;
    private long rtfsyncTrailingCount;
    private String bodyHTML;
    private String inReplyToId;
    private String returnPath;
    private long iconIndex;
    private long actionFlag;
    private Date actionDate;
    private Boolean disableFullFidelity;
    private String urlcompName;
    private Boolean attrHidden;
    private Boolean attrSystem;
    private Boolean attrReadonly;
    private long numberOfRecipients;
    private Date taskStartDate;
    private Date taskDueDate;
    private Boolean reminderSet;
    private long reminderDelta;
    private Boolean flagged;
    private long numberOfAttachments;
    private String recipientsString;
    private String rtfbody;
    private String displayName;
    private String comment;
    private long descriptorNodeId;
    private long nodeType;
    private String addrType;
    private String emailAddress;
    private Date creationTime;
    private Date lastModificationTime;

    private Recipient[] to = new Recipient[0];
    private Recipient[] cc = new Recipient[0];
    private Recipient[] bcc = new Recipient[0];


    public ArchivedPSTMessage() {

    }


    public void addTo(Recipient recipient) {
        Recipient[] copy = Arrays.copyOf(to, to.length + 1);
        copy[to.length] = recipient;
        to = copy;

    }

    public void addCc(Recipient recipient) {
        Recipient[] copy = Arrays.copyOf(cc, cc.length + 1);
        copy[cc.length] = recipient;
        cc = copy;

    }

    public void addBcc(Recipient recipient) {
        Recipient[] copy = Arrays.copyOf(bcc, bcc.length + 1);
        copy[bcc.length] = recipient;
        bcc = copy;

    }

    public Recipient[] getTo() {
        return to;
    }

    public Recipient[] getCc() {
        return cc;
    }

    public Recipient[] getBcc() {
        return bcc;
    }

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public String getInternetMessageId() {
        return internetMessageId;
    }

    public void setInternetMessageId(String internetMessageId) {
        this.internetMessageId = internetMessageId;
    }

    public String getMessageClass() {
        return messageClass;
    }

    public void setMessageClass(String messageClass) {
        this.messageClass = messageClass;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public long getImportance() {
        return importance;
    }

    public void setImportance(long importance) {
        this.importance = importance;
    }

    public Date getClientSubmitTime() {
        return clientSubmitTime;
    }

    public void setClientSubmitTime(Date clientSubmitTime) {
        this.clientSubmitTime = clientSubmitTime;
    }

    public String getReceivedByName() {
        return receivedByName;
    }

    public void setReceivedByName(String receivedByName) {
        this.receivedByName = receivedByName;
    }

    public String getSentRepresentingName() {
        return sentRepresentingName;
    }

    public void setSentRepresentingName(String sentRepresentingName) {
        this.sentRepresentingName = sentRepresentingName;
    }

    public String getSentRepresentingAddressType() {
        return sentRepresentingAddressType;
    }

    public void setSentRepresentingAddressType(String sentRepresentingAddressType) {
        this.sentRepresentingAddressType = sentRepresentingAddressType;
    }

    public String getSentRepresentingEmailAddress() {
        return sentRepresentingEmailAddress;
    }

    public void setSentRepresentingEmailAddress(String sentRepresentingEmailAddress) {
        this.sentRepresentingEmailAddress = sentRepresentingEmailAddress;
    }

    public String getConversationTopic() {
        return conversationTopic;
    }

    public void setConversationTopic(String conversationTopic) {
        this.conversationTopic = conversationTopic;
    }

    public String getReceivedByAddressType() {
        return receivedByAddressType;
    }

    public void setReceivedByAddressType(String receivedByAddressType) {
        this.receivedByAddressType = receivedByAddressType;
    }

    public String getReceivedByAddress() {
        return receivedByAddress;
    }

    public void setReceivedByAddress(String receivedByAddress) {
        this.receivedByAddress = receivedByAddress;
    }

    public String getTransportMessageHeaders() {
        return transportMessageHeaders;
    }

    public void setTransportMessageHeaders(String transportMessageHeaders) {
        this.transportMessageHeaders = transportMessageHeaders;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getUnmodified() {
        return unmodified;
    }

    public void setUnmodified(Boolean unmodified) {
        this.unmodified = unmodified;
    }

    public Boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    public Boolean getUnsent() {
        return unsent;
    }

    public void setUnsent(Boolean unsent) {
        this.unsent = unsent;
    }

    public Boolean getFromMe() {
        return fromMe;
    }

    public void setFromMe(Boolean fromMe) {
        this.fromMe = fromMe;
    }

    public Boolean getAssociated() {
        return associated;
    }

    public void setAssociated(Boolean associated) {
        this.associated = associated;
    }

    public Boolean getResent() {
        return resent;
    }

    public void setResent(Boolean resent) {
        this.resent = resent;
    }

    public long getAcknowledgementMode() {
        return acknowledgementMode;
    }

    public void setAcknowledgementMode(long acknowledgementMode) {
        this.acknowledgementMode = acknowledgementMode;
    }

    public Boolean getOriginatorDeliveryReportRequested() {
        return originatorDeliveryReportRequested;
    }

    public void setOriginatorDeliveryReportRequested(Boolean originatorDeliveryReportRequested) {
        this.originatorDeliveryReportRequested = originatorDeliveryReportRequested;
    }

    public Boolean getReadReceiptRequested() {
        return readReceiptRequested;
    }

    public void setReadReceiptRequested(Boolean readReceiptRequested) {
        this.readReceiptRequested = readReceiptRequested;
    }

    public Boolean getRecipientReassignmentProhibited() {
        return recipientReassignmentProhibited;
    }

    public void setRecipientReassignmentProhibited(Boolean recipientReassignmentProhibited) {
        this.recipientReassignmentProhibited = recipientReassignmentProhibited;
    }

    public long getOriginalSensitivity() {
        return originalSensitivity;
    }

    public void setOriginalSensitivity(long originalSensitivity) {
        this.originalSensitivity = originalSensitivity;
    }

    public long getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(long sensitivity) {
        this.sensitivity = sensitivity;
    }

    public String getPidTagSentRepresentingSearchKey() {
        return pidTagSentRepresentingSearchKey;
    }

    public void setPidTagSentRepresentingSearchKey(String pidTagSentRepresentingSearchKey) {
        this.pidTagSentRepresentingSearchKey = pidTagSentRepresentingSearchKey;
    }

    public String getRcvdRepresentingName() {
        return rcvdRepresentingName;
    }

    public void setRcvdRepresentingName(String rcvdRepresentingName) {
        this.rcvdRepresentingName = rcvdRepresentingName;
    }

    public String getOriginalSubject() {
        return originalSubject;
    }

    public void setOriginalSubject(String originalSubject) {
        this.originalSubject = originalSubject;
    }

    public String getReplyRecipientNames() {
        return replyRecipientNames;
    }

    public void setReplyRecipientNames(String replyRecipientNames) {
        this.replyRecipientNames = replyRecipientNames;
    }

    public Boolean getMessageToMe() {
        return messageToMe;
    }

    public void setMessageToMe(Boolean messageToMe) {
        this.messageToMe = messageToMe;
    }

    public Boolean getMessageCcMe() {
        return messageCcMe;
    }

    public void setMessageCcMe(Boolean messageCcMe) {
        this.messageCcMe = messageCcMe;
    }

    public Boolean getResponseRequested() {
        return responseRequested;
    }

    public void setResponseRequested(Boolean responseRequested) {
        this.responseRequested = responseRequested;
    }

    public String getSentRepresentingAddrtype() {
        return sentRepresentingAddrtype;
    }

    public void setSentRepresentingAddrtype(String sentRepresentingAddrtype) {
        this.sentRepresentingAddrtype = sentRepresentingAddrtype;
    }

    public String getOriginalDisplayBcc() {
        return originalDisplayBcc;
    }

    public void setOriginalDisplayBcc(String originalDisplayBcc) {
        this.originalDisplayBcc = originalDisplayBcc;
    }

    public Boolean getRtfinSync() {
        return rtfinSync;
    }

    public void setRtfinSync(Boolean rtfinSync) {
        this.rtfinSync = rtfinSync;
    }

    public String getOriginalDisplayCc() {
        return originalDisplayCc;
    }

    public void setOriginalDisplayCc(String originalDisplayCc) {
        this.originalDisplayCc = originalDisplayCc;
    }

    public String getOriginalDisplayTo() {
        return originalDisplayTo;
    }

    public void setOriginalDisplayTo(String originalDisplayTo) {
        this.originalDisplayTo = originalDisplayTo;
    }

    public String getRcvdRepresentingAddrtype() {
        return rcvdRepresentingAddrtype;
    }

    public void setRcvdRepresentingAddrtype(String rcvdRepresentingAddrtype) {
        this.rcvdRepresentingAddrtype = rcvdRepresentingAddrtype;
    }

    public String getRcvdRepresentingEmailAddress() {
        return rcvdRepresentingEmailAddress;
    }

    public void setRcvdRepresentingEmailAddress(String rcvdRepresentingEmailAddress) {
        this.rcvdRepresentingEmailAddress = rcvdRepresentingEmailAddress;
    }

    public Boolean getNonReceiptNotificationRequested() {
        return nonReceiptNotificationRequested;
    }

    public void setNonReceiptNotificationRequested(Boolean nonReceiptNotificationRequested) {
        this.nonReceiptNotificationRequested = nonReceiptNotificationRequested;
    }

    public Boolean getOriginatorNonDeliveryReportRequested() {
        return originatorNonDeliveryReportRequested;
    }

    public void setOriginatorNonDeliveryReportRequested(Boolean originatorNonDeliveryReportRequested) {
        this.originatorNonDeliveryReportRequested = originatorNonDeliveryReportRequested;
    }

    public long getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(long recipientType) {
        this.recipientType = recipientType;
    }

    public Boolean getReplyRequested() {
        return replyRequested;
    }

    public void setReplyRequested(Boolean replyRequested) {
        this.replyRequested = replyRequested;
    }

    public String getSenderEntryId() {
        return senderEntryId;
    }

    public void setSenderEntryId(String senderEntryId) {
        this.senderEntryId = senderEntryId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAddrtype() {
        return senderAddrtype;
    }

    public void setSenderAddrtype(String senderAddrtype) {
        this.senderAddrtype = senderAddrtype;
    }

    public String getSenderEmailAddress() {
        return senderEmailAddress;
    }

    public void setSenderEmailAddress(String senderEmailAddress) {
        this.senderEmailAddress = senderEmailAddress;
    }

    public long getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(long messageSize) {
        this.messageSize = messageSize;
    }

    public long getInternetArticleNumber() {
        return internetArticleNumber;
    }

    public void setInternetArticleNumber(int internetArticleNumber) {
        this.internetArticleNumber = internetArticleNumber;
    }

    public String getPrimarySendAccount() {
        return primarySendAccount;
    }

    public void setPrimarySendAccount(String primarySendAccount) {
        this.primarySendAccount = primarySendAccount;
    }

    public String getNextSendAcct() {
        return nextSendAcct;
    }

    public void setNextSendAcct(String nextSendAcct) {
        this.nextSendAcct = nextSendAcct;
    }

    public long getUrlcompNamePostfix() {
        return urlcompNamePostfix;
    }

    public void setUrlcompNamePostfix(long urlcompNamePostfix) {
        this.urlcompNamePostfix = urlcompNamePostfix;
    }

    public long getObjectType() {
        return objectType;
    }

    public void setObjectType(long objectType) {
        this.objectType = objectType;
    }

    public Boolean getDeleteAfterSubmit() {
        return deleteAfterSubmit;
    }

    public void setDeleteAfterSubmit(Boolean deleteAfterSubmit) {
        this.deleteAfterSubmit = deleteAfterSubmit;
    }

    public Boolean getResponsibility() {
        return responsibility;
    }

    public void setResponsibility(Boolean responsibility) {
        this.responsibility = responsibility;
    }

    public Boolean getUrlcompNameSet() {
        return urlcompNameSet;
    }

    public void setUrlcompNameSet(Boolean urlcompNameSet) {
        this.urlcompNameSet = urlcompNameSet;
    }

    public String getDisplayBCC() {
        return displayBCC;
    }

    public void setDisplayBCC(String displayBCC) {
        this.displayBCC = displayBCC;
    }

    public String getDisplayCC() {
        return displayCC;
    }

    public void setDisplayCC(String displayCC) {
        this.displayCC = displayCC;
    }

    public String getDisplayTo() {
        return displayTo;
    }

    public void setDisplayTo(String displayTo) {
        this.displayTo = displayTo;
    }

    public Date getMessageDeliveryTime() {
        return messageDeliveryTime;
    }

    public void setMessageDeliveryTime(Date messageDeliveryTime) {
        this.messageDeliveryTime = messageDeliveryTime;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBodyPrefix() {
        return bodyPrefix;
    }

    public void setBodyPrefix(String bodyPrefix) {
        this.bodyPrefix = bodyPrefix;
    }

    public long getRtfsyncBodyCRC() {
        return rtfsyncBodyCRC;
    }

    public void setRtfsyncBodyCRC(long rtfsyncBodyCRC) {
        this.rtfsyncBodyCRC = rtfsyncBodyCRC;
    }

    public long getRtfsyncBodyCount() {
        return rtfsyncBodyCount;
    }

    public void setRtfsyncBodyCount(long rtfsyncBodyCount) {
        this.rtfsyncBodyCount = rtfsyncBodyCount;
    }

    public String getRtfsyncBodyTag() {
        return rtfsyncBodyTag;
    }

    public void setRtfsyncBodyTag(String rtfsyncBodyTag) {
        this.rtfsyncBodyTag = rtfsyncBodyTag;
    }

    public long getRtfsyncPrefixCount() {
        return rtfsyncPrefixCount;
    }

    public void setRtfsyncPrefixCount(long rtfsyncPrefixCount) {
        this.rtfsyncPrefixCount = rtfsyncPrefixCount;
    }

    public long getRtfsyncTrailingCount() {
        return rtfsyncTrailingCount;
    }

    public void setRtfsyncTrailingCount(long rtfsyncTrailingCount) {
        this.rtfsyncTrailingCount = rtfsyncTrailingCount;
    }

    public String getBodyHTML() {
        return bodyHTML;
    }

    public void setBodyHTML(String bodyHTML) {
        this.bodyHTML = bodyHTML;
    }

    public String getInReplyToId() {
        return inReplyToId;
    }

    public void setInReplyToId(String inReplyToId) {
        this.inReplyToId = inReplyToId;
    }

    public String getReturnPath() {
        return returnPath;
    }

    public void setReturnPath(String returnPath) {
        this.returnPath = returnPath;
    }

    public long getIconIndex() {
        return iconIndex;
    }

    public void setIconIndex(long iconIndex) {
        this.iconIndex = iconIndex;
    }

    public long getActionFlag() {
        return actionFlag;
    }

    public void setActionFlag(long actionFlag) {
        this.actionFlag = actionFlag;
    }

    public Date getActionDate() {
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }

    public Boolean getDisableFullFidelity() {
        return disableFullFidelity;
    }

    public void setDisableFullFidelity(Boolean disableFullFidelity) {
        this.disableFullFidelity = disableFullFidelity;
    }

    public String getUrlcompName() {
        return urlcompName;
    }

    public void setUrlcompName(String urlcompName) {
        this.urlcompName = urlcompName;
    }

    public Boolean getAttrHidden() {
        return attrHidden;
    }

    public void setAttrHidden(Boolean attrHidden) {
        this.attrHidden = attrHidden;
    }

    public Boolean getAttrSystem() {
        return attrSystem;
    }

    public void setAttrSystem(Boolean attrSystem) {
        this.attrSystem = attrSystem;
    }

    public Boolean getAttrReadonly() {
        return attrReadonly;
    }

    public void setAttrReadonly(Boolean attrReadonly) {
        this.attrReadonly = attrReadonly;
    }

    public long getNumberOfRecipients() {
        return numberOfRecipients;
    }

    public void setNumberOfRecipients(long numberOfRecipients) {
        this.numberOfRecipients = numberOfRecipients;
    }

    public Date getTaskStartDate() {
        return taskStartDate;
    }

    public void setTaskStartDate(Date taskStartDate) {
        this.taskStartDate = taskStartDate;
    }

    public Date getTaskDueDate() {
        return taskDueDate;
    }

    public void setTaskDueDate(Date taskDueDate) {
        this.taskDueDate = taskDueDate;
    }

    public Boolean getReminderSet() {
        return reminderSet;
    }

    public void setReminderSet(Boolean reminderSet) {
        this.reminderSet = reminderSet;
    }

    public long getReminderDelta() {
        return reminderDelta;
    }

    public void setReminderDelta(long reminderDelta) {
        this.reminderDelta = reminderDelta;
    }

    public Boolean getFlagged() {
        return flagged;
    }

    public void setFlagged(Boolean flagged) {
        this.flagged = flagged;
    }

    public long getNumberOfAttachments() {
        return numberOfAttachments;
    }

    public void setNumberOfAttachments(long numberOfAttachments) {
        this.numberOfAttachments = numberOfAttachments;
    }

    public String getRecipientsString() {
        return recipientsString;
    }

    public void setRecipientsString(String recipientsString) {
        this.recipientsString = recipientsString;
    }

    public String getRtfbody() {
        return rtfbody;
    }

    public void setRtfbody(String rtfbody) {
        this.rtfbody = rtfbody;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getDescriptorNodeId() {
        return descriptorNodeId;
    }

    public void setDescriptorNodeId(long descriptorNodeId) {
        this.descriptorNodeId = descriptorNodeId;
    }

    public long getNodeType() {
        return nodeType;
    }

    public void setNodeType(long nodeType) {
        this.nodeType = nodeType;
    }

    public String getAddrType() {
        return addrType;
    }

    public void setAddrType(String addrType) {
        this.addrType = addrType;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getLastModificationTime() {
        return lastModificationTime;
    }

    public void setLastModificationTime(Date lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);

    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
