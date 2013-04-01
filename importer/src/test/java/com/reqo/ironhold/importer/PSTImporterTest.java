package com.reqo.ironhold.importer;

import com.reqo.ironhold.importer.watcher.checksum.MD5CheckSum;
import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.LocalMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageMetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.model.message.MimeMailMessage;
import com.reqo.ironhold.storage.model.message.source.MessageSource;
import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;
import com.reqo.ironhold.storage.security.IKeyStoreService;
import com.reqo.ironhold.storage.security.LocalKeyStoreService;
import fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean;
import fr.pilato.spring.elasticsearch.ElasticsearchNodeFactoryBean;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.File;
import java.io.IOException;
import java.util.List;


@ContextConfiguration(locations = "classpath:PSTImporterTest_context.xml")
public class PSTImporterTest extends AbstractJUnit4SpringContextTests {

    private static final String TEST_CLIENT = "test";
    private static final String PST_TEST_FILE = "/data.pst";
    private static final String PST_TEST_FILE2 = "/data2.pst";
    private static final String mailBoxName = "test mailbox";
    private static final String originalFilePath = "test path";
    private static final String commentary = "test commentary";
    private File pstfile;
    private String md5;
    private File pstfile2;
    private String md52;

    @Autowired
    private IMimeMailMessageStorageService mimeMailMessageStorageService;

    @Autowired
    private MessageMetaDataIndexService messageMetaDataIndexService;

    @Autowired
    private MiscIndexService miscIndexService;

    @Autowired
    private PSTImporter pstImporter;

    @Autowired
    private IndexClient indexClient;

    @Autowired
    private IKeyStoreService keyStoreService;

    @Autowired
    private ElasticsearchClientFactoryBean esClient;

    @Autowired
    private ElasticsearchNodeFactoryBean esNode;

    @Before
    public void setUp() throws Exception {


        deleteIfExists(((LocalKeyStoreService) keyStoreService).getKeyStore());
        deleteIfExists(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getParent().getParentFile());
        FileUtils.forceMkdir(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getParent());

        pstfile = FileUtils.toFile(PSTImporterTest.class
                .getResource(PST_TEST_FILE));

        md5 = MD5CheckSum.getMD5Checksum(pstfile);
        pstfile2 = FileUtils.toFile(PSTImporterTest.class
                .getResource(PST_TEST_FILE2));

        md52 = MD5CheckSum.getMD5Checksum(pstfile2);

        pstImporter.setCommentary(commentary);
        pstImporter.setMailBoxName(mailBoxName);
        pstImporter.setMd5(md5);
        pstImporter.setOriginalFilePath(originalFilePath);
        pstImporter.setFile(pstfile);
    }

    private void deleteIfExists(File file) throws IOException {
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }

    @After
    public void tearDown() throws Exception {
        deleteIfExists(((LocalKeyStoreService) keyStoreService).getKeyStore());
        deleteIfExists(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getParent().getParentFile());
        esClient.getObject().admin().indices().prepareDelete("_all").execute().actionGet();
        this.messageMetaDataIndexService.clearCache();
        this.miscIndexService.clearCache();

    }


    @Test
    public void testPSTImporter() throws Exception {
        String results = pstImporter.processMessages();

        indexClient.refresh(TEST_CLIENT + "." + MiscIndexService.SUFFIX);

        List<PSTFileMeta> pstFiles = miscIndexService.getPSTFileMeta(TEST_CLIENT, 0, 10);


        Assert.assertEquals(1, pstFiles.size());

        PSTFileMeta pstFileMeta = pstFiles.get(0);
        Assert.assertNotNull(pstFileMeta);
        Assert.assertEquals(0, pstFileMeta.getFailures());
        Assert.assertEquals(48, pstFileMeta.getMessages());
        Assert.assertEquals(2, pstFileMeta.getDuplicates());


        Assert.assertEquals(
                1L,
                pstFileMeta
                        .getFolderMap()
                        .get("/Top of Outlook data file/Inbox/Resumes/data/sent test")
                        .longValue());
        Assert.assertEquals(
                7L,
                pstFileMeta
                        .getFolderMap()
                        .get("/Top of Outlook data file/Inbox/Resumes/data/in person.interview"
                                .replace(".", PSTFileMeta.DOT_REPLACEMENT))
                        .longValue());
        Assert.assertEquals(
                7L,
                pstFileMeta.getFolderMap()
                        .get("/Top of Outlook data file/Inbox/Resumes/data")
                        .longValue());
        Assert.assertEquals(
                2L,
                pstFileMeta
                        .getFolderMap()
                        .get("/Top of Outlook data file/Inbox/Resumes/data/Withdrew")
                        .longValue());
        Assert.assertEquals(
                30L,
                pstFileMeta
                        .getFolderMap()
                        .get("/Top of Outlook data file/Inbox/Resumes/data/reject after test")
                        .longValue());
        Assert.assertEquals(
                1L,
                pstFileMeta
                        .getFolderMap()
                        .get("/Top of Outlook data file/Inbox/Resumes/data/recieved test")
                        .longValue());

        MimeMailMessage testMailMessage = new MimeMailMessage();
        testMailMessage.loadMimeMessageFromSource(mimeMailMessageStorageService
                .get(TEST_CLIENT, "2008", "<984867.51882.qm@web30305.mail.mud.yahoo.com>"));
        Assert.assertNotNull(testMailMessage);

        indexClient.refresh(TEST_CLIENT + "." + MessageMetaDataIndexService.SUFFIX);
        List<MessageSource> sources = messageMetaDataIndexService.getSources(TEST_CLIENT, "<984867.51882.qm@web30305.mail.mud.yahoo.com>");

        for (MessageSource source : sources) {
            System.out.println(source.toString());
        }
        Assert.assertEquals(2, sources.size());

        List<MessageSource> sources2 = messageMetaDataIndexService.getSources(TEST_CLIENT, "<fb57d8a0811071645n76f4c2e6o10d5aa19c78b49bf@mail.gmail.com>");

        for (MessageSource source : sources2) {
            System.out.println(source.toString());
        }
        Assert.assertEquals(1, sources2.size());


    }

    @Test
    public void testPSTImporterDupes() throws Exception {
        String results = pstImporter.processMessages();

        indexClient.refresh(TEST_CLIENT + "." + MiscIndexService.SUFFIX);

        List<PSTFileMeta> pstFiles = miscIndexService.getPSTFileMeta(TEST_CLIENT, 0, 10);


        Assert.assertEquals(1, pstFiles.size());

        pstImporter.setFile(pstfile2);
        pstImporter.setMd5(md52);

        pstImporter.processMessages();

        indexClient.refresh(TEST_CLIENT + "." + MiscIndexService.SUFFIX);

        List<PSTFileMeta> pstFiles2 = miscIndexService.getPSTFileMeta(TEST_CLIENT, 0, 10);

        Assert.assertEquals(2, pstFiles2.size());

        PSTFileMeta pstFileMeta0 = pstFiles2.get(0);
        PSTFileMeta pstFileMeta1 = pstFiles2.get(1);
        Assert.assertNotNull(pstFileMeta1);
        Assert.assertEquals(0, pstFileMeta1.getFailures());

        /*long totalCount = storageService.getTotalMessageCount();

        Assert.assertEquals(
                pstFileMeta1.getDuplicates() - pstFileMeta0.getDuplicates(),
                totalCount);
                                         */

        MimeMailMessage testMailMessage = new MimeMailMessage();
        testMailMessage.loadMimeMessageFromSource(mimeMailMessageStorageService
                .get(TEST_CLIENT, "2008", "<984867.51882.qm@web30305.mail.mud.yahoo.com>"));
        Assert.assertNotNull(testMailMessage);

        indexClient.refresh(TEST_CLIENT + "." + MessageMetaDataIndexService.SUFFIX);
        List<MessageSource> sources = messageMetaDataIndexService.getSources(TEST_CLIENT, "<984867.51882.qm@web30305.mail.mud.yahoo.com>");

        for (MessageSource source : sources) {
            System.out.println(source.toString());
        }
        Assert.assertEquals(4, sources.size());

    }

    @Test
    public void testPSTImporterAlreadyProcessed() throws Exception {
        String results = pstImporter.processMessages();

        indexClient.refresh(TEST_CLIENT + "." + MiscIndexService.SUFFIX);

        List<PSTFileMeta> pstFiles = miscIndexService.getPSTFileMeta(TEST_CLIENT, 0, 10);


        Assert.assertEquals(1, pstFiles.size());

        try {
            pstImporter.processMessages();

            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertEquals("This file has been processed already",
                    e.getMessage());
        }
    }
}
