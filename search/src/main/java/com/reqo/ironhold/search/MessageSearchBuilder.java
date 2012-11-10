package com.reqo.ironhold.search;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.sort.SortOrder;

public class MessageSearchBuilder {

	public static MessageSearchBuilder newBuilder(SearchRequestBuilder builder) {
		return new MessageSearchBuilder(builder);
	}

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

	private MessageSearchBuilder(SearchRequestBuilder builder) {
		this.builder = builder;
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

	public MessageSearchBuilder withId(String id) {
		this.id = id;

		return this;
	}

	public SearchRequestBuilder build() {
		if (id == null) {
			QueryStringQueryBuilder qb = QueryBuilders.queryString(criteria);
			builder.setQuery(qb);
		} else {
			FilteredQueryBuilder qb = QueryBuilders.filteredQuery(QueryBuilders
					.queryString(criteria), FilterBuilders.idsFilter("message")
					.addIds(id));
			builder.setQuery(qb);
		}

		builder.setFrom(from).setSize(size);
		builder.addHighlightedField("pstMessage.subject", 0, 0);

		if (fullBody) {
			builder.addHighlightedField("pstMessage.body", 0, 0);
		} else {
			builder.addHighlightedField("pstMessage.body");
		}
		builder.addHighlightedField("pstMessage.attachments.body");
		if (sortField != null) {
			builder.addSort(sortField.getValue(), sortOrder);
		}

		builder.addFields("pstMessage.subject", "pstMessage.body",
				"pstMessage.messageDeliveryTime", "pstMessage.messageSize",
				"pstMessage.sentRepresentingName",
				"pstMessage.sentRepresentingEmailAddress",
				"pstMessage.displayTo", "pstMessage.displayCc");
		builder.setHighlighterPreTags("<b>").setHighlighterPostTags("</b>");

		if (dateFacet) {
			builder.addFacet(FacetBuilders.dateHistogramFacet("date").interval("year").field(
					IndexFieldEnum.DATE.getValue()));
		}
		
		if (fromFacet) {
			builder.addFacet(FacetBuilders.termsFacet("from").field(
					IndexFieldEnum.FROM.getValue()));
		}
		System.out.println(builder.toString());

		return builder;
	}
}
