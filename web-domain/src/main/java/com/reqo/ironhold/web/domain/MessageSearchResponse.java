package com.reqo.ironhold.web.domain;

import com.gs.collections.api.list.ImmutableList;
import com.gs.collections.api.set.ImmutableSet;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 10:08 AM
 */
public class MessageSearchResponse {
    private ImmutableList<MessageMatch> messages;
    private ImmutableList<FacetGroup> facets;
    private long timeTaken;

    public MessageSearchResponse() {
    }

    public MessageSearchResponse(ImmutableList<MessageMatch> messages, ImmutableList<FacetGroup> facets, long timeTaken) {
        this.messages = messages;
        this.facets = facets;
        this.timeTaken = timeTaken;
    }

    public ImmutableList<MessageMatch> getMessages() {
        return messages;
    }

    public void setMessages(ImmutableList<MessageMatch> messages) {
        this.messages = messages;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public ImmutableList<FacetGroup> getFacets() {
        return facets;
    }

    public void setFacets(ImmutableList<FacetGroup> facets) {
        this.facets = facets;
    }
}
