package com.reqo.ironhold.storage.interfaces;

import com.reqo.ironhold.storage.model.metadata.BloombergMeta;
import com.reqo.ironhold.storage.model.metadata.IMAPBatchMeta;
import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;
import com.reqo.ironhold.web.domain.LoginChannelEnum;
import com.reqo.ironhold.web.domain.LoginUser;

import java.util.List;

/**
 * User: ilya
 * Date: 11/24/13
 * Time: 8:43 AM
 */
public interface IMiscIndexService {
    void store(String indexPrefix, PSTFileMeta meta);

    void store(String indexPrefix, IMAPBatchMeta meta);

    void store(String indexPrefix, BloombergMeta meta);

    PSTFileMeta findExisting(String indexPrefix, PSTFileMeta meta);

    PSTFileMeta getPSTFileMeta(String indexPrefix, String id);

    List<PSTFileMeta> getPSTFileMetas(String indexPrefix, String criteria, int from, int limit);

    List<IMAPBatchMeta> getIMAPBatchMeta(String indexPrefix, String criteria, int from, int limit);

    void store(String indexPrefix, LoginUser loginUser);

    List<LoginUser> getLoginUsers(String indexPrefix, String criteria, int start, int limit);

    LoginUser authenticate(String indexPrefix, String username, String password, LoginChannelEnum channel, String loginContext);

    LoginUser getLoginUser(String indexPrefix, String username);

    long getLoginUserCount(String indexPrefix);

    LoginUser usernameExists(String indexPrefix, String username);
}
