package com.reqo.ironhold.web.api.mocks;

import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.storage.model.metadata.BloombergMeta;
import com.reqo.ironhold.storage.model.metadata.IMAPBatchMeta;
import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;
import com.reqo.ironhold.web.domain.LoginChannelEnum;
import com.reqo.ironhold.web.domain.LoginUser;
import com.reqo.ironhold.web.domain.RoleEnum;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * User: ilya
 * Date: 11/24/13
 * Time: 9:18 AM
 */
public class MockMiscIndexService implements IMiscIndexService {
    @Override
    public void store(String indexPrefix, PSTFileMeta meta) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void store(String indexPrefix, IMAPBatchMeta meta) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void store(String indexPrefix, BloombergMeta meta) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PSTFileMeta findExisting(String indexPrefix, PSTFileMeta meta) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PSTFileMeta getPSTFileMeta(String indexPrefix, String id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PSTFileMeta> getPSTFileMetas(String indexPrefix, int from, int limit) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<IMAPBatchMeta> getIMAPBatchMeta(String indexPrefix, int from, int limit)  {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void store(String indexPrefix, LoginUser loginUser) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LoginUser> getLoginUsers(String indexPrefix, int start, int limit)  {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LoginUser authenticate(String indexPrefix, String username, String password, LoginChannelEnum channel, String loginContext) {
        LoginUser result = new LoginUser();
        result.setUsername(username);
        result.setLastLogin(new Date());
        result.setLastLoginChannel(channel.getValue());
        result.setLastLoginContext(loginContext);
        result.setRolesBitMask(RoleEnum.SUPER_USER.getValue());
        return result;
    }

    @Override
    public LoginUser getLoginUser(String indexPrefix, String username) {
        return null;
    }

    @Override
    public long getLoginUserCount(String indexPrefix) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LoginUser usernameExists(String indexPrefix, String username) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
