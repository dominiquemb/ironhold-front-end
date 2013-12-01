package com.reqo.ironhold.storage.interfaces;

import com.reqo.ironhold.storage.model.metadata.BloombergMeta;
import com.reqo.ironhold.storage.model.metadata.IMAPBatchMeta;
import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;
import com.reqo.ironhold.storage.model.user.LoginChannelEnum;
import com.reqo.ironhold.storage.model.user.LoginUser;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

    List<PSTFileMeta> getPSTFileMetas(String indexPrefix, int from, int limit);

    List<IMAPBatchMeta> getIMAPBatchMeta(String indexPrefix, int from, int limit) throws IOException, ExecutionException, InterruptedException;

    void store(String indexPrefix, LoginUser loginUser);

    List<LoginUser> getLoginUsers(String indexPrefix, int start, int limit) throws IOException, ExecutionException, InterruptedException;

    LoginUser authenticate(String indexPrefix, String username, String password, LoginChannelEnum channel, String loginContext);

    long getLoginUserCount(String indexPrefix) throws ExecutionException, InterruptedException;

    LoginUser usernameExists(String indexPrefix, String username) throws ExecutionException, InterruptedException, IOException;
}
