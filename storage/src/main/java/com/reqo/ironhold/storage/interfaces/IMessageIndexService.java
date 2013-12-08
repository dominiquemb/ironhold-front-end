package com.reqo.ironhold.storage.interfaces;

import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.web.domain.IndexedMailMessage;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.domain.CountSearchResponse;
import com.reqo.ironhold.web.domain.MessageSearchResponse;
import com.reqo.ironhold.web.domain.SuggestSearchResponse;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 12:21 PM
 */
public interface IMessageIndexService {
    void store(String indexPrefix, IndexedMailMessage message);

    void store(String indexPrefix, IndexedMailMessage message, boolean checkIfExists);


    IndexedMailMessage getById(String indexPrefix, String partition, String messageId);

    boolean exists(String indexPrefix, String partition, String messageId);

    MessageSearchBuilder getNewBuilder(String alias, LoginUser loginUser);

    MessageSearchBuilder getNewBuilder(String alias, MessageSearchBuilder oldBuilder, LoginUser loginUser);

    MessageSearchResponse search(MessageSearchBuilder builder);

    CountSearchResponse getMatchCount(String indexPrefix, String search, LoginUser loginUser);


    CountSearchResponse getMatchCount(MessageSearchBuilder builder, LoginUser loginUser);


    CountSearchResponse getTotalMessageCount(String indexPrefix, LoginUser loginUser);

    SuggestSearchResponse getSuggestions(String indexPrefix, String search, LoginUser loginUser);

}
