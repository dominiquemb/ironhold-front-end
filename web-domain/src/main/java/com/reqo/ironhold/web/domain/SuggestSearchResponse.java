package com.reqo.ironhold.web.domain;

import com.gs.collections.api.list.ImmutableList;

/**
 * User: ilya
 * Date: 12/8/13
 * Time: 9:44 AM
 */
public class SuggestSearchResponse {
    public static final SuggestSearchResponse EMPTY_RESPONSE = new SuggestSearchResponse(-1, null);

    private long timeTaken;

    private ImmutableList<Suggestion> suggestions;

    public SuggestSearchResponse(long timeTaken, ImmutableList<Suggestion> suggestions) {
        this.timeTaken = timeTaken;
        this.suggestions = suggestions;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public ImmutableList<Suggestion> getSuggestions() {
        return suggestions;
    }
}
