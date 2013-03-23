package com.reqo.ironhold.storage;

/**
 * User: ilya
 * Date: 3/19/13
 * Time: 8:25 PM
 */
public class ThriftStorageService implements IMimeMailMessageStorageService {



    @Override
    public long store(String client, String messageId, String serializedMailMessage, String checkSum) throws Exception {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean exists(String client, String messageId) throws Exception {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String get(String client, String messageId) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
