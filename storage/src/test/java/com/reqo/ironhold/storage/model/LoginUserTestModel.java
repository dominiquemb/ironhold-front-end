package com.reqo.ironhold.storage.model;

import com.reqo.ironhold.storage.security.CheckSumHelper;
import com.reqo.ironhold.web.domain.LoginUser;
import com.reqo.ironhold.web.domain.Recipient;
import com.reqo.ironhold.web.domain.RoleEnum;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: ilya
 * Date: 4/11/13
 * Time: 11:50 AM
 */
public class LoginUserTestModel extends CommonTestModel {

    public static LoginUser generate(RoleEnum role) throws NoSuchAlgorithmException {
        LoginUser loginUser = new LoginUser();
        loginUser.setCreated(df.getDateBetween(getMinDate(), getMaxDate()));
        loginUser.setLastLogin(df.getDateBetween(getMinDate(), getMaxDate()));
        loginUser.setName(df.getName());
        loginUser.setUsername(df.getFirstName());
        List<RoleEnum> roles = new ArrayList<>();
        roles.add(role);
        loginUser.setRolesBitMask(role.getValue());

        loginUser.setHashedPassword(CheckSumHelper.getCheckSum(loginUser.getUsername().getBytes()));
        loginUser.setMainRecipient(Recipient.build(df.getName(), df.getEmailAddress()));
        List<Recipient> recipientList = new ArrayList<>();
        for (int i = 0; i < df.getNumberBetween(0, 10); i++) {
            Recipient recipient = Recipient.build(df.getName(), df.getEmailAddress());
            recipientList.add(recipient);
        }
        loginUser.setRecipients(recipientList);
        return loginUser;
    }

}
