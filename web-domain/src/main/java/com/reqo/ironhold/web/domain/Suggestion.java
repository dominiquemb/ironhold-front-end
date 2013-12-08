package com.reqo.ironhold.web.domain;

import com.gs.collections.api.list.ImmutableList;

/**
 * User: ilya
 * Date: 12/8/13
 * Time: 9:46 AM
 */
public class Suggestion {
    private final String text;
    private final ImmutableList<String> options;

    public Suggestion(String text, ImmutableList<String> options) {
        this.text = text;
        this.options = options;
    }

    public String getText() {
        return text;
    }

    public ImmutableList<String> getOptions() {
        return options;
    }
}
