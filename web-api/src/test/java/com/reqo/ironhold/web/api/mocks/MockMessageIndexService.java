package com.reqo.ironhold.web.api.mocks;

import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.web.domain.IndexedMailMessage;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.domain.CountSearchResponse;
import com.reqo.ironhold.web.domain.MessageSearchResponse;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 12:31 PM
 */
public class MockMessageIndexService implements IMessageIndexService {

    @Override
    public void store(String indexPrefix, IndexedMailMessage message) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void store(String indexPrefix, IndexedMailMessage message, boolean checkIfExists) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IndexedMailMessage getById(String indexPrefix, String partition, String messageId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean exists(String indexPrefix, String partition, String messageId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MessageSearchBuilder getNewBuilder(String alias, LoginUser loginUser) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MessageSearchBuilder getNewBuilder(String alias, MessageSearchBuilder oldBuilder, LoginUser loginUser) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MessageSearchResponse search(MessageSearchBuilder builder) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CountSearchResponse getMatchCount(String indexPrefix, String search, LoginUser loginUser) {
        return new CountSearchResponse((long) (Math.random() * 1000), (long) (Math.random() * 100));
    }

    @Override
    public CountSearchResponse getMatchCount(MessageSearchBuilder builder, LoginUser loginUser) {
        return new CountSearchResponse((long) (Math.random() * 1000), (long) (Math.random() * 100));
    }

    @Override
    public CountSearchResponse getTotalMessageCount(String indexPrefix, LoginUser loginUser) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
