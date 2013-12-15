package com.reqo.ironhold.storage;

import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.list.ImmutableList;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.api.set.ImmutableSet;
import com.gs.collections.api.set.MutableSet;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.set.mutable.UnifiedSet;
import com.gs.collections.impl.utility.ArrayIterate;
import com.gs.collections.impl.utility.ListIterate;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.storage.model.search.IndexedObjectType;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.domain.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.highlight.HighlightField;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestion;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MessageIndexService extends AbstractIndexService implements IMessageIndexService {
    private static Logger logger = Logger.getLogger(MessageIndexService.class);

    private static Map<IndexedObjectType, String> mappings;


    private static final Function<SearchHit, MessageMatch> SEARCHHIT_TO_MESSAGEMATCH = new Function<SearchHit, MessageMatch>() {
        @Override
        public MessageMatch valueOf(SearchHit searchHit) {
            FormattedIndexedMailMessage indexedMailMessage = FormattedIndexedMailMessage.deserialize(searchHit.getSourceAsString());

            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            String bodyWithHighlights = indexedMailMessage.getBody();
            String subjectWithHiglights = indexedMailMessage.getSubject();
            String attachmentsWithHighlights = StringUtils.EMPTY;


            if (highlightFields.containsKey(IndexFieldEnum.BODY.getValue())) {
                bodyWithHighlights = ArrayIterate.makeString(highlightFields.get(IndexFieldEnum.BODY.getValue()).getFragments(), " ... ");
            }

            if (highlightFields.containsKey(IndexFieldEnum.SUBJECT.getValue())) {
                subjectWithHiglights = ArrayIterate.makeString(highlightFields.get(IndexFieldEnum.SUBJECT.getValue()).getFragments(), " ... ");
            }

            if (highlightFields.containsKey(IndexFieldEnum.ATTACHMENT.getValue())) {
                attachmentsWithHighlights = ArrayIterate.makeString(highlightFields.get(IndexFieldEnum.ATTACHMENT.getValue()).getFragments(), " ... ");
            }


            return new MessageMatch(indexedMailMessage, bodyWithHighlights, subjectWithHiglights, attachmentsWithHighlights);
        }
    };

    private static final Function<Facet, String> FACET_TO_NAME = new Function<Facet, String>() {
        @Override
        public String valueOf(Facet facet) {
            return facet.getName();
        }
    };

    private static final Function<TermsFacet.Entry, FacetValue> TERMSFACETENTRY_TO_FACETVALUE = new Function<TermsFacet.Entry, FacetValue>() {
        @Override
        public FacetValue valueOf(TermsFacet.Entry entry) {
            return new FacetValue(entry.getTerm().toString(), entry.getCount());
        }
    };

    static {
        mappings = new HashMap<>();
        mappings.put(IndexedObjectType.MIME_MESSAGE, "messageIndexMapping.json");
    }

    public MessageIndexService(IndexClient client) {
        super(client, "messageIndexSettings.json", mappings);
    }

    public void store(String indexPrefix, IndexedMailMessage message) {
        store(indexPrefix, message, true);
    }

    public void store(String indexPrefix, IndexedMailMessage message, boolean checkIfExists) {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, message.getPartition());

        createIndexIfMissing(indexPrefix, message.getPartition());

        if (!checkIfExists || !client.itemExists(indexName, IndexedObjectType.MIME_MESSAGE, message.getMessageId())) {
            client.store(
                    indexName,
                    IndexedObjectType.MIME_MESSAGE,
                    message.getMessageId(),
                    message.serialize());
        }
    }

    public IndexedMailMessage getById(String indexPrefix, String partition, String messageId) {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, partition);

        createIndexIfMissing(indexPrefix, partition);


        GetResponse response = client.getById(indexName, IndexedObjectType.MIME_MESSAGE, messageId);
        if (response.isExists()) {
            IndexedMailMessage indexedMailMessage = new IndexedMailMessage();
            indexedMailMessage = indexedMailMessage.deserialize(response.getSourceAsString());

            return indexedMailMessage;
        }
        return null;
    }

    public boolean exists(String indexPrefix, String partition, String messageId) {
        String alias = getIndexAlias(indexPrefix);
        String indexName = getIndexName(alias, partition);

        createIndexIfMissing(indexPrefix, partition);

        return client.itemExists(indexName, IndexedObjectType.MIME_MESSAGE, messageId);
    }

    public MessageSearchBuilder getNewBuilder(String alias, LoginUser loginUser) {
        if (alias == null || StringUtils.isEmpty(alias)) {
            throw new IllegalArgumentException("Alias cannot be blank");
        }
        return MessageSearchBuilder.newBuilder(client.getSearchRequestBuilder(alias, loginUser), loginUser);
    }

    public MessageSearchBuilder getNewBuilder(String alias, MessageSearchBuilder oldBuilder, LoginUser loginUser) {
        MessageSearchBuilder newBuilder = MessageSearchBuilder
                .newBuilder(client.getSearchRequestBuilder(alias, loginUser), loginUser);
        return newBuilder.buildFrom(oldBuilder);
    }

    public MessageSearchResponse search(MessageSearchBuilder builder) {
        SearchRequestBuilder search = builder.build();

        TermSuggestionBuilder termSuggestion = new TermSuggestionBuilder("termSuggestion");
        termSuggestion.field(IndexFieldEnum.BODY.getValue()).text(builder.getCriteria());
        search.addSuggestion(termSuggestion);

        logger.info(search.toString());
        SearchResponse response = search.execute().actionGet();

        final Facets facets = response.getFacets();
        MutableList<FacetGroup> facetGroups = FastList.newList();
        if (facets != null) {
            final MutableList<String> names = ListIterate.collect(facets.facets(), FACET_TO_NAME);
            facetGroups = names.collect(new Function<String, FacetGroup>() {
                @Override
                public FacetGroup valueOf(String facetName) {
                    TermsFacet termsFacet = facets.facet(facetName);

                    MutableList<FacetValue> valueMap = ListIterate.collect(termsFacet.getEntries(), TERMSFACETENTRY_TO_FACETVALUE);

                    if (facetName.equals(FacetGroupName.FACET_YEAR.getValue())) {
                        valueMap = valueMap.sortThis(FacetValue.BY_NAME);
                    } else {
                        valueMap = valueMap.sortThis(FacetValue.BY_VALUE);
                    }

                    return new FacetGroup(FacetGroupName.fromValue(facetName), valueMap.toImmutable());
                }
            }).sortThis(FacetGroup.BY_ORDER);
        }

        ImmutableList<MessageMatch> messages = ArrayIterate.collect(response.getHits().getHits(), SEARCHHIT_TO_MESSAGEMATCH).toImmutable();

        TermSuggestion suggestion = response.getSuggest().getSuggestion("termSuggestion");
        MutableList<Suggestion> suggestions = ListIterate.collect(suggestion.getEntries(), new Function<TermSuggestion.Entry, Suggestion>() {
            @Override
            public Suggestion valueOf(TermSuggestion.Entry termSuggestion) {

                MutableList<String> options = ListIterate.collect(termSuggestion.getOptions(), new Function<TermSuggestion.Entry.Option, String>() {
                    @Override
                    public String valueOf(TermSuggestion.Entry.Option option) {
                        return option.getText().toString();
                    }
                });

                return new Suggestion(termSuggestion.getText().toString(), options.toImmutable());
            }
        });


        return new MessageSearchResponse(messages, facetGroups.toImmutable(), suggestions.toImmutable(), response.getHits().getTotalHits(), response.getTookInMillis());
    }

    public CountSearchResponse getMatchCount(String indexPrefix, String search, LoginUser loginUser) {
        try {
            SearchRequestBuilder builder = client.getSearchRequestBuilder(indexPrefix, loginUser);
            QueryBuilder qb = QueryBuilders.queryString(search)
                    .defaultOperator(Operator.AND);

            builder.setQuery(qb);
            builder.setSearchType(SearchType.COUNT);
            SearchResponse response = builder.execute().actionGet();

            return new CountSearchResponse(response.getHits().getTotalHits(), response.getTookInMillis());
        } catch (SearchPhaseExecutionException e) {
            logger.warn(e);
            return CountSearchResponse.EMPTY_RESPONSE;
        }
    }

    public CountSearchResponse getMatchCount(MessageSearchBuilder builder, LoginUser loginUser) {
        SearchResponse response = builder.build()
                .setSearchType(SearchType.COUNT).execute().actionGet();

        return new CountSearchResponse(response.getHits().getTotalHits(), response.getTookInMillis());
    }

    public CountSearchResponse getTotalMessageCount(String indexPrefix, LoginUser loginUser) {
        try {
            SearchRequestBuilder search = client.getSearchRequestBuilder(indexPrefix, loginUser);
            SearchResponse response = search.execute().get();
            logger.info(search.toString());
            return new CountSearchResponse(response.getHits().getTotalHits(), response.getTookInMillis());
        } catch (InterruptedException | ExecutionException e) {
            logger.warn(e);
            return null;
        }
    }


    public SuggestSearchResponse getSuggestions(String indexPrefix, String search, LoginUser loginUser) {
        try {

            SearchRequestBuilder builder = client.getSearchRequestBuilder(indexPrefix, loginUser);
            TermSuggestionBuilder termSuggestion = new TermSuggestionBuilder("termSuggestion");
            termSuggestion.field(IndexFieldEnum.BODY.getValue()).text(search);

            builder.addSuggestion(termSuggestion);
            builder.setSearchType(SearchType.COUNT);
            SearchResponse response = builder.execute().actionGet();

            TermSuggestion suggestion = response.getSuggest().getSuggestion("termSuggestion");
            MutableList<Suggestion> suggestions = ListIterate.collect(suggestion.getEntries(), new Function<TermSuggestion.Entry, Suggestion>() {
                @Override
                public Suggestion valueOf(TermSuggestion.Entry termSuggestion) {

                    MutableList<String> options = ListIterate.collect(termSuggestion.getOptions(), new Function<TermSuggestion.Entry.Option, String>() {
                        @Override
                        public String valueOf(TermSuggestion.Entry.Option option) {
                            return option.getText().toString();
                        }
                    });

                    return new Suggestion(termSuggestion.getText().toString(), options.toImmutable());
                }
            });

            return new SuggestSearchResponse(response.getTookInMillis(), suggestions.toImmutable());
        } catch (SearchPhaseExecutionException e) {
            logger.warn(e);
            return SuggestSearchResponse.EMPTY_RESPONSE;
        }
    }

}
