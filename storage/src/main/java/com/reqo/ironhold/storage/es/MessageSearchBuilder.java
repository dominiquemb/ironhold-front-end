package com.reqo.ironhold.storage.es;

import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.storage.model.search.MessageTypeEnum;
import com.reqo.ironhold.web.domain.FacetGroupName;
import com.reqo.ironhold.web.domain.LoginUser;
import com.reqo.ironhold.web.domain.Recipient;
import com.reqo.ironhold.web.domain.RoleEnum;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacetBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageSearchBuilder {


    public static MessageSearchBuilder newBuilder(SearchRequestBuilder builder, LoginUser loginUser) {
        return new MessageSearchBuilder(builder, loginUser);
    }

    private List<String> fromFacetValues = new ArrayList<String>();
    private List<String> fromDomainFacetValues = new ArrayList<String>();
    private List<String> toFacetValues = new ArrayList<String>();
    private List<String> toDomainFacetValues = new ArrayList<String>();
    private List<String> fileExtFacetValues = new ArrayList<String>();
    private List<String> msgTypeFacetValues = new ArrayList<String>();
    private List<String> dateFacetValues = new ArrayList<String>();

    private final SearchRequestBuilder builder;
    private String criteria;
    private int from = 0;
    private int size = 10;
    private SortOrder sortOrder;
    private IndexFieldEnum sortField;
    private boolean fullBody;
    private String id;
    private boolean dateFacet;
    private boolean fromFacet;
    private boolean fromDomainFacet;
    private boolean toFacet;
    private boolean toDomainFacet;
    private boolean fileExtFacet;
    private boolean msgTypeFacet;
    private Date startDate;
    private Date endDate;
    private String sender;
    private String recipient;
    private String subject;
    private String body;
    private String attachment;
    private MessageTypeEnum messageType;

    private IndexedObjectType indexedObjectType;
    private LoginUser loginUser;

    private MessageSearchBuilder(SearchRequestBuilder builder, LoginUser loginUser) {
        this.builder = builder;
        this.loginUser = loginUser;
    }

    public MessageSearchBuilder withSender(String sender) {
        this.sender = sender;
        return this;
    }

    public MessageSearchBuilder withRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public MessageSearchBuilder withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public MessageSearchBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    public MessageSearchBuilder withAttachment(String attachment) {
        this.attachment = attachment;
        return this;
    }

    public MessageSearchBuilder withMessageType(MessageTypeEnum messageType) {
        this.messageType = messageType;
        return this;
    }

    public MessageSearchBuilder withStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public MessageSearchBuilder withEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public MessageSearchBuilder withCriteria(String criteria) {
        this.criteria = criteria;

        return this;
    }

    public MessageSearchBuilder withResultsLimit(int from, int size) {
        this.from = from;
        this.size = size;

        return this;
    }

    public MessageSearchBuilder withSort(IndexFieldEnum sortField,
                                         SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;

        return this;
    }

    public MessageSearchBuilder withFullBody() {
        this.fullBody = true;

        return this;
    }

    public MessageSearchBuilder withDateFacet() {
        this.dateFacet = true;

        return this;
    }

    public MessageSearchBuilder withFromFacet() {
        this.fromFacet = true;

        return this;
    }

    public MessageSearchBuilder withFromDomainFacet() {
        this.fromDomainFacet = true;

        return this;
    }

    public MessageSearchBuilder withToFacet() {
        this.toFacet = true;

        return this;
    }

    public MessageSearchBuilder withToDomainFacet() {
        this.toDomainFacet = true;

        return this;
    }

    public MessageSearchBuilder withFileExtFacet() {
        this.fileExtFacet = true;

        return this;
    }

    public MessageSearchBuilder withMsgTypeFacet() {
        this.msgTypeFacet = true;

        return this;
    }

    public MessageSearchBuilder withFromFacetValue(String term) {
        this.fromFacetValues.add(term);
        return this;
    }

    public MessageSearchBuilder withoutFromFacetValue(String term) {
        this.fromFacetValues.remove(term);
        return this;
    }

    public MessageSearchBuilder withFromDomainFacetValue(String term) {
        this.fromDomainFacetValues.add(term);
        return this;
    }

    public MessageSearchBuilder withoutFromDomainFacetValue(String term) {
        this.fromDomainFacetValues.remove(term);
        return this;
    }

    public MessageSearchBuilder withToFacetValue(String term) {
        this.toFacetValues.add(term);
        return this;
    }

    public MessageSearchBuilder withoutToFacetValue(String term) {
        this.toFacetValues.remove(term);
        return this;
    }

    public MessageSearchBuilder withToDomainFacetValue(String term) {
        this.toDomainFacetValues.add(term);
        return this;
    }

    public MessageSearchBuilder withoutToDomainFacetValue(String term) {
        this.toDomainFacetValues.remove(term);
        return this;
    }

    public MessageSearchBuilder withFileExtFacetValue(String term) {
        this.fileExtFacetValues.add(term);
        return this;
    }

    public MessageSearchBuilder withoutFileExtFacetValue(String term) {
        this.fileExtFacetValues.remove(term);
        return this;
    }

    public MessageSearchBuilder withMsgTypeFacetValue(String term) {
        this.msgTypeFacetValues.add(term);
        return this;
    }

    public MessageSearchBuilder withoutMsgTypeFacetValue(String term) {
        this.msgTypeFacetValues.remove(term);
        return this;
    }

    public MessageSearchBuilder withYearFacetValue(String term) {
        this.dateFacetValues.add(term);
        return this;
    }

    public MessageSearchBuilder withoutYearFacetValue(String term) {
        this.dateFacetValues.remove(term);
        return this;
    }

    public MessageSearchBuilder withId(String id, IndexedObjectType indexedObjectType) {
        this.id = id;
        this.indexedObjectType = indexedObjectType;
        return this;
    }

    public SearchRequestBuilder build() {
        if (id == null) {
            QueryStringQueryBuilder qb = QueryBuilders.queryString(criteria);
            qb.defaultOperator(Operator.AND);


            if (max(fromFacetValues.size(), fromDomainFacetValues.size(), toFacetValues.size(), toDomainFacetValues.size(),
                    dateFacetValues.size(), fileExtFacetValues.size(), msgTypeFacetValues.size()) > 0) {

                AndFilterBuilder andFilter = FilterBuilders.andFilter();
                if (fromFacetValues.size() > 0) {
                    andFilter.add(FilterBuilders.inFilter(
                            IndexFieldEnum.FROM_NAME.getValue(),
                            fromFacetValues.toArray(new String[fromFacetValues
                                    .size()])));
                }
                if (fromDomainFacetValues.size() > 0) {
                    andFilter.add(FilterBuilders.inFilter(
                            IndexFieldEnum.FROM_DOMAIN.getValue(),
                            fromDomainFacetValues
                                    .toArray(new String[fromDomainFacetValues
                                            .size()])));
                }

                if (toFacetValues.size() > 0) {
                    andFilter
                            .add(FilterBuilders.inFilter(IndexFieldEnum.TO_NAME
                                    .getValue(), toFacetValues
                                    .toArray(new String[toFacetValues.size()])));
                }

                if (toDomainFacetValues.size() > 0) {
                    andFilter.add(FilterBuilders.inFilter(
                            IndexFieldEnum.TO_DOMAIN.getValue(),
                            toDomainFacetValues
                                    .toArray(new String[toDomainFacetValues
                                            .size()])));
                }

                if (fileExtFacetValues.size() > 0) {
                    andFilter.add(FilterBuilders.inFilter(
                            IndexFieldEnum.FILEEXT.getValue(),
                            fileExtFacetValues
                                    .toArray(new String[fileExtFacetValues
                                            .size()])));
                }

                if (msgTypeFacetValues.size() > 0) {
                    andFilter.add(FilterBuilders.inFilter(
                            IndexFieldEnum.MSGTYPE.getValue(),
                            msgTypeFacetValues
                                    .toArray(new String[msgTypeFacetValues
                                            .size()])));
                }

                if (dateFacetValues.size() > 0) {
                    andFilter.add(FilterBuilders.inFilter(
                            IndexFieldEnum.YEAR.getValue(),
                            dateFacetValues
                                    .toArray(new String[dateFacetValues
                                            .size()])));
                }

                FilteredQueryBuilder fqb = QueryBuilders.filteredQuery(qb,
                        andFilter);

                builder.setQuery(fqb);
            } else {
                AndFilterBuilder andFilter = FilterBuilders.andFilter();
                andFilter.add(FilterBuilders.matchAllFilter());

                if (startDate != null || endDate != null) {
                    andFilter.add(FilterBuilders.rangeFilter(IndexFieldEnum.DATE.getValue()).from(startDate).to(endDate));
                }

                if (sender != null) {
                    andFilter.add(FilterBuilders.orFilter(
                            FilterBuilders.termFilter(IndexFieldEnum.FROM_NAME.getValue(), sender),
                            FilterBuilders.termFilter(IndexFieldEnum.FROM_ADDRESS.getValue(), sender),
                            FilterBuilders.termFilter(IndexFieldEnum.FROM_DOMAIN.getValue(), sender)));
                }

                if (recipient != null) {
                    andFilter.add(FilterBuilders.orFilter(
                            FilterBuilders.termFilter(IndexFieldEnum.TO_NAME.getValue(), recipient),
                            FilterBuilders.termFilter(IndexFieldEnum.TO_ADDRESS.getValue(), recipient),
                            FilterBuilders.termFilter(IndexFieldEnum.TO_DOMAIN.getValue(), recipient),
                            FilterBuilders.termFilter(IndexFieldEnum.CC_NAME.getValue(), recipient),
                            FilterBuilders.termFilter(IndexFieldEnum.CC_ADDRESS.getValue(), recipient),
                            FilterBuilders.termFilter(IndexFieldEnum.CC_DOMAIN.getValue(), recipient),
                            FilterBuilders.termFilter(IndexFieldEnum.BCC_NAME.getValue(), recipient),
                            FilterBuilders.termFilter(IndexFieldEnum.BCC_ADDRESS.getValue(), recipient),
                            FilterBuilders.termFilter(IndexFieldEnum.BCC_DOMAIN.getValue(), recipient) ));
                }

                if (subject != null) {
                    andFilter.add(FilterBuilders.termFilter(IndexFieldEnum.SUBJECT.getValue(), subject));
                }

                if (body != null) {
                    andFilter.add(FilterBuilders.termFilter(IndexFieldEnum.BODY.getValue(), body));
                }

                if (messageType != null) {
                    andFilter.add(FilterBuilders.termFilter(IndexFieldEnum.MSGTYPE.getValue(), messageType.getValue()));
                }

                if (attachment != null) {
                    andFilter.add(FilterBuilders.termFilter(IndexFieldEnum.ATTACHMENT.getValue(), attachment));
                }


                FilteredQueryBuilder fqb = QueryBuilders.filteredQuery(qb,
                        andFilter);
                builder.setQuery(fqb);
            }

        } else {
            FilteredQueryBuilder fqb = QueryBuilders.filteredQuery(
                    QueryBuilders.queryString(criteria), FilterBuilders
                    .idsFilter(indexedObjectType.getValue()).addIds(id));

            builder.setQuery(fqb);
        }

        builder.setFrom(from).setSize(size);
        builder.addHighlightedField(IndexFieldEnum.SUBJECT.getValue(), 0, 0);

        if (fullBody) {
            builder.addHighlightedField(IndexFieldEnum.BODY.getValue(), 0, 0);
        } else {
            builder.addHighlightedField(IndexFieldEnum.BODY.getValue(), 100, 2);
        }
        builder.addHighlightedField(IndexFieldEnum.ATTACHMENT.getValue(), 100,
                1);
        builder.setHighlighterPreTags("<span class=\"blue-hilite\">");
        builder.setHighlighterPostTags("</span>");

        if (sortField != null) {
            if (!("_score".equals(sortField.getValue()) && sortOrder
                    .equals(SortOrder.DESC))) {
                builder.addSort(sortField.getValue(), sortOrder);
            }
        }

        for (IndexFieldEnum field : IndexFieldEnum.values()) {
            if (!field.equals(IndexFieldEnum.ATTACHMENT)) {
                builder.addField(field.getValue());
            }

        }

        if (dateFacet) {
            builder.addFacet(applyLoginFilters(FacetBuilders.termsFacet(FacetGroupName.FACET_YEAR.getValue()).order(TermsFacet.ComparatorType.REVERSE_TERM)
                    .field(IndexFieldEnum.YEAR.getValue()), loginUser));
        }

        if (fromFacet) {
            builder.addFacet(applyLoginFilters(FacetBuilders.termsFacet(FacetGroupName.FACET_FROM_NAME.getValue()).exclude("unknown").field(
                    IndexFieldEnum.FROM_NAME.getValue()), loginUser));
        }

        if (fromDomainFacet) {
            builder.addFacet(applyLoginFilters(FacetBuilders.termsFacet(FacetGroupName.FACET_FROM_DOMAIN.getValue()).exclude("unknown").field(
                    IndexFieldEnum.FROM_DOMAIN.getValue()).script("term.toLowerCase()"), loginUser));
        }

        if (toFacet) {
            builder.addFacet(applyLoginFilters(FacetBuilders.termsFacet(FacetGroupName.FACET_TO_NAME.getValue()).exclude("unknown").field(
                    IndexFieldEnum.TO_NAME.getValue()), loginUser));
        }

        if (toDomainFacet) {
            builder.addFacet(applyLoginFilters(FacetBuilders.termsFacet(FacetGroupName.FACET_TO_DOMAIN.getValue()).exclude("unknown").field(
                    IndexFieldEnum.TO_DOMAIN.getValue()).script("term.toLowerCase()"), loginUser));
        }
        if (fileExtFacet) {
            builder.addFacet(applyLoginFilters(FacetBuilders.termsFacet(FacetGroupName.FACET_FILEEXT.getValue()).field(
                    IndexFieldEnum.FILEEXT.getValue()).script("term.toLowerCase()"), loginUser));
        }

        if (msgTypeFacet) {
            builder.addFacet(applyLoginFilters(FacetBuilders.termsFacet(FacetGroupName.FACET_MSGTYPE.getValue()).field(
                    IndexFieldEnum.MSGTYPE.getValue()), loginUser));
        }

        builder.addField("_source");
        return builder;
    }

    private int max(int... size) {
        int max = 0;
        for (int s : size) {
            max = Math.max(max, s);
        }
        return max;
    }

    private TermsFacetBuilder applyLoginFilters(TermsFacetBuilder termsFacetBuilder, LoginUser loginUser) {
        if (loginUser.hasRole(RoleEnum.CAN_SEARCH) || loginUser.hasRole(RoleEnum.CAN_SEARCH_ALL)) {
            if (!loginUser.hasRole(RoleEnum.CAN_SEARCH_ALL)) {
                OrFilterBuilder filterBuilders = FilterBuilders.orFilter();
                IndexClient.addLoginFilter(filterBuilders, loginUser.getMainRecipient().getAddress());
                if (loginUser.getRecipients() != null) {
                    for (Recipient recipient : loginUser.getRecipients()) {
                        IndexClient.addLoginFilter(filterBuilders, recipient.getAddress());
                    }
                }
                if (loginUser.getSources() != null) {
                    for (String source : loginUser.getSources()) {
                        IndexClient.addSourceFilter(filterBuilders, source);
                    }
                }
                termsFacetBuilder.facetFilter(filterBuilders);
            }
        } else {
            throw new SecurityException("This user does not have search role");
        }

        return termsFacetBuilder;
    }

    public MessageSearchBuilder buildFrom(MessageSearchBuilder oldBuilder) {
        this.criteria = oldBuilder.criteria;

        this.fromFacetValues = oldBuilder.fromFacetValues;
        this.fromDomainFacetValues = oldBuilder.fromDomainFacetValues;
        this.toFacetValues = oldBuilder.toFacetValues;
        this.toDomainFacetValues = oldBuilder.toDomainFacetValues;
        this.dateFacetValues = oldBuilder.dateFacetValues;
        this.fileExtFacetValues = oldBuilder.fileExtFacetValues;
        this.msgTypeFacetValues = oldBuilder.msgTypeFacetValues;

        return this;
    }


    public MessageSearchBuilder withNamedFacet(FacetGroupName facetName) {
        if (facetName.equals(FacetGroupName.FACET_FROM_NAME))
            return this.withFromFacet();
        if (facetName.equals(FacetGroupName.FACET_FROM_DOMAIN))
            return this.withFromDomainFacet();
        if (facetName.equals(FacetGroupName.FACET_TO_NAME))
            return this.withToFacet();
        if (facetName.equals(FacetGroupName.FACET_YEAR))
            return this.withDateFacet();
        if (facetName.equals(FacetGroupName.FACET_TO_DOMAIN))
            return this.withToDomainFacet();
        if (facetName.equals(FacetGroupName.FACET_FILEEXT))
            return this.withFileExtFacet();
        if (facetName.equals(FacetGroupName.FACET_MSGTYPE))
            return this.withMsgTypeFacet();

        throw new IllegalArgumentException("Unknown facet [" + facetName + "]");
    }

    public MessageSearchBuilder withNamedFacetValue(FacetGroupName facetName, String facetValue) {
        if (facetName.equals(FacetGroupName.FACET_FROM_NAME))
            return this.withFromFacetValue(facetValue);
        if (facetName.equals(FacetGroupName.FACET_FROM_DOMAIN))
            return this.withFromDomainFacetValue(facetValue);
        if (facetName.equals(FacetGroupName.FACET_TO_NAME))
            return this.withToFacetValue(facetValue);
        if (facetName.equals(FacetGroupName.FACET_YEAR))
            return this.withYearFacetValue(facetValue);
        if (facetName.equals(FacetGroupName.FACET_TO_DOMAIN))
            return this.withToDomainFacetValue(facetValue);
        if (facetName.equals(FacetGroupName.FACET_FILEEXT))
            return this.withFileExtFacetValue(facetValue);
        if (facetName.equals(FacetGroupName.FACET_MSGTYPE))
            return this.withMsgTypeFacetValue(facetValue);

        throw new IllegalArgumentException("Unknown facet [" + facetName + "]");
    }

    public boolean isDateFacet() {
        return dateFacet;
    }

    public void setDateFacet(boolean dateFacet) {
        this.dateFacet = dateFacet;
    }

    public boolean isFromFacet() {
        return fromFacet;
    }

    public void setFromFacet(boolean fromFacet) {
        this.fromFacet = fromFacet;
    }

    public boolean isFromDomainFacet() {
        return fromDomainFacet;
    }

    public void setFromDomainFacet(boolean fromDomainFacet) {
        this.fromDomainFacet = fromDomainFacet;
    }

    public boolean isToFacet() {
        return toFacet;
    }

    public void setToFacet(boolean toFacet) {
        this.toFacet = toFacet;
    }

    public boolean isToDomainFacet() {
        return toDomainFacet;
    }

    public void setToDomainFacet(boolean toDomainFacet) {
        this.toDomainFacet = toDomainFacet;
    }

    public boolean isFileExtFacet() {
        return fileExtFacet;
    }

    public void setFileExtFacet(boolean fileExtFacet) {
        this.fileExtFacet = fileExtFacet;
    }

    public boolean isMsgTypeFacet() {
        return msgTypeFacet;
    }

    public void setMsgTypeFacet(boolean msgTypeFacet) {
        this.msgTypeFacet = msgTypeFacet;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

}
