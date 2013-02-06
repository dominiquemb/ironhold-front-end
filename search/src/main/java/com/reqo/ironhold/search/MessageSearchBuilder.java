package com.reqo.ironhold.search;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.sort.SortOrder;

import com.reqo.ironhold.search.model.IndexedObjectType;

public class MessageSearchBuilder {

	public static final String FACET_FROM_NAME = "from";
	public static final String FACET_FROM_DOMAIN = "from_domain";
	public static final String FACET_TO_NAME = "to";
	public static final String FACET_DATE = "date";
	public static final String FACET_TO_DOMAIN = "to_domain";
	public static final String FACET_FILENAME = "file";

	public static MessageSearchBuilder newBuilder(SearchRequestBuilder builder) {
		return new MessageSearchBuilder(builder);
	}

	private List<String> fromFacetValues = new ArrayList<String>();
	private List<String> fromDomainFacetValues = new ArrayList<String>();
	private List<String> toFacetValues = new ArrayList<String>();
	private List<String> toDomainFacetValues = new ArrayList<String>();
	private List<String> fileExtFacetValues = new ArrayList<String>();
	private List<Long> dateFacetValues = new ArrayList<Long>();

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
	private IndexedObjectType indexedObjectType;

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

	public MessageSearchBuilder withYearFacetValue(long term) {
		this.dateFacetValues.add(term);
		return this;
	}

	public MessageSearchBuilder withoutYearFacetValue(long term) {
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
			if (fromFacetValues.size() > 0 || fromDomainFacetValues.size() > 0
					|| toFacetValues.size() > 0
					|| toDomainFacetValues.size() > 0
					|| dateFacetValues.size() > 0
					|| fileExtFacetValues.size() > 0) {
				AndFilterBuilder andFilter = FilterBuilders.andFilter();
				if (fromFacetValues.size() > 0) {
					andFilter.add(FilterBuilders.inFilter(
							IndexFieldEnum.FROM_NAME.getValue(),
							fromFacetValues.toArray(new String[fromFacetValues
									.size()])));
				}
				if (fromDomainFacetValues.size() > 0) {
					andFilter.add(FilterBuilders.inFilter(
							IndexFieldEnum.FROM_ADDRESS.getValue(),
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
							IndexFieldEnum.TO_ADDRESS.getValue(),
							toDomainFacetValues
									.toArray(new String[toDomainFacetValues
											.size()])));
				}

				if (fileExtFacetValues.size() > 0) {
					andFilter.add(FilterBuilders.inFilter(
							IndexFieldEnum.FILENAME.getValue(),
							fileExtFacetValues
									.toArray(new String[fileExtFacetValues
											.size()])));
				}

				if (dateFacetValues.size() > 0) {

					OrFilterBuilder orFilter = FilterBuilders.orFilter();
					for (Long yearFacetValue : dateFacetValues) {
						DateTime from = new DateTime(yearFacetValue.longValue());
						DateTime to = from.plusYears(1);

						orFilter.add(FilterBuilders
								.numericRangeFilter(
										IndexFieldEnum.DATE.getValue())
								.from(from.toDate().getTime())
								.to(to.toDate().getTime()));
					}

					andFilter.add(orFilter);

					// ..inFilter(IndexFieldEnum.DATE.getValue(),
					// dateFacetValues.toArray(new Long[dateFacetValues
					// .size()])));
				}

				FilteredQueryBuilder fqb = QueryBuilders.filteredQuery(qb,
						andFilter);

				builder.setQuery(fqb);
			} else {
				builder.setQuery(qb);
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
			builder.addHighlightedField(IndexFieldEnum.BODY.getValue());
		}
		builder.addHighlightedField(IndexFieldEnum.ATTACHMENT.getValue(), 100,
				1);

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
		builder.setHighlighterPreTags("<b>").setHighlighterPostTags("</b>");

		if (dateFacet) {
			builder.addFacet(FacetBuilders.dateHistogramFacet(FACET_DATE)
					.interval("year").field(IndexFieldEnum.DATE.getValue()));
		}

		if (fromFacet) {
			builder.addFacet(FacetBuilders.termsFacet(FACET_FROM_NAME).exclude("unknown").field(
					IndexFieldEnum.FROM_NAME.getValue()));
		}

		if (fromDomainFacet) {
			builder.addFacet(FacetBuilders.termsFacet(FACET_FROM_DOMAIN).exclude("unknown").field(
					IndexFieldEnum.FROM_ADDRESS.getValue()));
		}

		if (toFacet) {
			builder.addFacet(FacetBuilders.termsFacet(FACET_TO_NAME).exclude("unknown").field(
					IndexFieldEnum.TO_NAME.getValue()));
		}

		if (toDomainFacet) {
			builder.addFacet(FacetBuilders.termsFacet(FACET_TO_DOMAIN).exclude("unknown").field(
					IndexFieldEnum.TO_ADDRESS.getValue()));
		}

		if (fileExtFacet) {
			builder.addFacet(FacetBuilders.termsFacet(FACET_FILENAME).field(
					IndexFieldEnum.FILENAME.getValue()));
		}

		return builder;
	}

	public MessageSearchBuilder buildFrom(MessageSearchBuilder oldBuilder) {
		this.criteria = oldBuilder.criteria;

		this.fromFacetValues = oldBuilder.fromFacetValues;
		this.fromDomainFacetValues = oldBuilder.fromDomainFacetValues;
		this.toFacetValues = oldBuilder.toFacetValues;
		this.toDomainFacetValues = oldBuilder.toDomainFacetValues;
		this.dateFacetValues = oldBuilder.dateFacetValues;
		this.fileExtFacetValues = oldBuilder.fileExtFacetValues;

		return this;
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
}
