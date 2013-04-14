package com.reqo.ironhold.storage.model;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import junit.framework.Assert;

public class MimeMailMessageTestModel extends CommonTestModel {


    public static MimeMailMessage verifyStorage(String client, IMimeMailMessageStorageService storageService,
                                                MimeMailMessage inputMessage) throws Exception {


        MimeMailMessage storedMessage = new MimeMailMessage();
        storedMessage.loadMimeMessageFromSource(storageService.get(client, inputMessage.getPartition(), inputMessage.getSubPartition(),
                inputMessage.getMessageId()));
        Assert.assertEquals(inputMessage.getRawContents(),
                storedMessage.getRawContents());

        return storedMessage;
    }


}
