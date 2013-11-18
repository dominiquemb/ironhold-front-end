package com.reqo.ironhold.serviceintegrationtests;

import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.message.Recipient;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import com.reqo.ironhold.storage.security.CheckSumHelper;

/**
 * User: ilya
 * Date: 11/17/13
 * Time: 1:11 PM
 */
public class SampleMiscIndexService extends MiscIndexService {
    public static final String samplePassword = "secret";
    public static final String sampleUsername = "testUser";
    public static final String sampleClientKey = "test";

    public SampleMiscIndexService(IndexClient client) {
        super(client);
        init();
    }

    private void init() {
        try {
            LoginUser sampleUser = new LoginUser();
            sampleUser.setUsername(sampleUsername);

            sampleUser.setHashedPassword(CheckSumHelper.getCheckSum(samplePassword.getBytes()));
            sampleUser.setRolesBitMask(RoleEnum.SUPER_USER.getValue());
            sampleUser.setMainRecipient(new Recipient("Sample User", "sample@user.net"));

            this.store(sampleClientKey, sampleUser);

            this.client.refresh(sampleClientKey + "." + MiscIndexService.SUFFIX);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
