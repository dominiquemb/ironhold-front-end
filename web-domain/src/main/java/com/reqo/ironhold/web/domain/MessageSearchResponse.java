package com.reqo.ironhold.web.domain;

import com.gs.collections.api.list.ImmutableList;
import com.gs.collections.api.set.ImmutableSet;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 10:08 AM
 */
public class MessageSearchResponse {
    private final ImmutableList<MessageMatch> messages;
    private final ImmutableList<FacetGroup> facets;
    private final ImmutableList<Suggestion> suggestions;
    private final long timeTaken;
    private final long matches;


    public MessageSearchResponse(ImmutableList<MessageMatch> messages, ImmutableList<FacetGroup> facets, ImmutableList<Suggestion> suggestions, long matches, long timeTaken) {
        this.messages = messages;
        this.facets = facets;
        this.suggestions = suggestions;
        this.matches = matches;
        this.timeTaken = timeTaken;
    }

    public ImmutableList<MessageMatch> getMessages() {
        return messages;
    }

    public ImmutableList<FacetGroup> getFacets() {
        return facets;
    }

    public ImmutableList<Suggestion> getSuggestions() {
        return suggestions;
    }

    public long getTimeTaken() {
        return timeTaken;
    }


    public long getMatches() {
        return matches;
    }
}
