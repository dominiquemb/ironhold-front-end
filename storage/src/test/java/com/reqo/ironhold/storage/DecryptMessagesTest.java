package com.reqo.ironhold.storage;

import com.pff.PSTMessage;
import com.reqo.ironhold.storage.model.MimeMailMessageTestModel;
import com.reqo.ironhold.storage.model.PSTMessageTestModel;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.security.IKeyStoreService;
import com.reqo.ironhold.storage.security.LocalKeyStoreService;
import com.reqo.ironhold.storage.utils.DecryptMessages;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.File;
import java.io.IOException;
import java.util.List;

@ContextConfiguration(locations = "classpath:DecryptMessagesTest_context.xml")
public class DecryptMessagesTest extends AbstractJUnit4SpringContextTests {
    private PSTMessageTestModel testModel;
    private static final String TEST_CLIENT = "test";


    @Autowired
    private LocalMimeMailMessageStorageService storageService;

    @Autowired
    private IKeyStoreService keyStoreService;

    @Autowired
    private DecryptMessages decryptMessages;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        testModel = new PSTMessageTestModel("/data.pst");
        deleteIfExists(((LocalMimeMailMessageStorageService) storageService).getDataStore().getParentFile());
        FileUtils.forceMkdir(((LocalMimeMailMessageStorageService) storageService).getDataStore());
    }

    private void deleteIfExists(File file) throws IOException {
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }

    @After
    public void tearDown() throws Exception {
        deleteIfExists(((LocalKeyStoreService) keyStoreService).getKeyStore());
        deleteIfExists(((LocalMimeMailMessageStorageService) storageService).getDataStore().getParentFile());
    }


    @Test
    public void testDecryptMessages() throws Exception {
        List<PSTMessage> pstMessages = testModel.generateOriginalPSTMessages();

        for (PSTMessage pstMessage : pstMessages) {

            MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(pstMessage);

            storageService.store(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId(), inputMessage.getRawContents(), inputMessage.getCheckSum(), true);

            MimeMailMessageTestModel.verifyStorage(TEST_CLIENT, storageService, inputMessage);

            Assert.assertTrue(storageService.exists(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId()));

            Assert.assertTrue(storageService.isEncrypted(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId()));
        }

        decryptMessages.run(TEST_CLIENT, null, null);

        for (PSTMessage pstMessage : pstMessages) {
            MimeMailMessage inputMessage = MimeMailMessage.getMimeMailMessage(pstMessage);

            Assert.assertTrue(storageService.exists(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId()));

            Assert.assertFalse(storageService.isEncrypted(TEST_CLIENT, inputMessage.getPartition(), inputMessage.getSubPartition(), inputMessage.getMessageId()));
        }

    }
}
