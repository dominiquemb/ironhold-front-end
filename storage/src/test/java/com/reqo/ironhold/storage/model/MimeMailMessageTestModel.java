package com.reqo.ironhold.storage.model;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.LocalMimeMailMessageStorageService;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import org.junit.Assert;

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


    public static MimeMailMessage verifyArchiveStorage(String client, LocalMimeMailMessageStorageService storageService, MimeMailMessage inputMessage) throws Exception {

        LocalMimeMailMessageStorageService archiveStorageService = new LocalMimeMailMessageStorageService(storageService.getArchiveStore(), storageService.getArchiveStore(), storageService.getKeyStoreService());
        MimeMailMessage storedMessage = new MimeMailMessage();
        storageService.getArchiveStore();
        storedMessage.loadMimeMessageFromSource(archiveStorageService.get(client, inputMessage.getPartition(), inputMessage.getSubPartition(),
                inputMessage.getMessageId()));
        Assert.assertEquals(inputMessage.getRawContents(),
                storedMessage.getRawContents());

        return storedMessage;
    }
}
