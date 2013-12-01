package com.reqo.ironhold.web.domain;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 10:05 AM
 */
public class CountSearchResponse {
    public static final CountSearchResponse EMPTY_RESPONSE = new CountSearchResponse(-1, -1);

    private long matches;
    private long timeTaken;

    public CountSearchResponse() {

    }

    public CountSearchResponse(long matches, long timeTaken) {
        this.matches = matches;
        this.timeTaken = timeTaken;
    }

    public long getMatches() {
        return matches;
    }

    public void setMatches(long matches) {
        this.matches = matches;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }
}
