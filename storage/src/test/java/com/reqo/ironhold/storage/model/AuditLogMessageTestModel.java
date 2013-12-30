package com.reqo.ironhold.storage.model;

import com.reqo.ironhold.web.domain.AuditActionEnum;
import com.reqo.ironhold.web.domain.AuditLogMessage;
import com.reqo.ironhold.web.domain.RoleEnum;

import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class AuditLogMessageTestModel extends CommonTestModel {

    public static AuditLogMessage generate() throws UnknownHostException, NoSuchAlgorithmException {
        AuditLogMessage testMessage = new AuditLogMessage();
        testMessage.setHost(df.getName());
        testMessage.setAction(AuditActionEnum.SEARCH);
        testMessage.setMessageId(UUID.randomUUID().toString());
        testMessage.setTimestamp(df.getDateBetween(getMinDate(), getMaxDate()));
        testMessage.setLoginUser(LoginUserTestModel.generate(RoleEnum.CAN_SEARCH));
        return testMessage;
    }


}
