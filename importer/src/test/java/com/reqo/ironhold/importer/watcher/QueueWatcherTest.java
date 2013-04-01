package com.reqo.ironhold.importer.watcher;

import com.reqo.ironhold.importer.PSTImporter;
import com.reqo.ironhold.importer.notification.EmailNotification;
import com.reqo.ironhold.importer.watcher.checksum.MD5CheckSum;
import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.LocalMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageMetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.security.IKeyStoreService;
import com.reqo.ironhold.storage.security.LocalKeyStoreService;
import fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean;
import fr.pilato.spring.elasticsearch.ElasticsearchNodeFactoryBean;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ContextConfiguration(locations = "classpath:QueueWatcherTest_context.xml")
public class QueueWatcherTest  extends AbstractJUnit4SpringContextTests {

    private static final String PST_TEST_FILE = "/data.pst";
    private static final String PST_TEST_FILE2 = "/data2.pst";


    @Rule
    public TemporaryFolder inFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder queueFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder outFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder quarantineFolder = new TemporaryFolder();

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

    @Autowired
    private QueueWatcher queueWatcher;


    @Before
    public void setUp() throws Exception {
        deleteIfExists(((LocalKeyStoreService) keyStoreService).getKeyStore());
        deleteIfExists(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getParent().getParentFile());
        FileUtils.forceMkdir(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getParent());

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
    public void testQueueWatcherValid() throws Exception {

        EmailNotification.disableNotification();

        queueWatcher.setInputDirName(queueFolder.getRoot().getAbsolutePath());
        queueWatcher.setQuarantineDirName(quarantineFolder.getRoot().getAbsolutePath());
        queueWatcher.setOutputDirName(outFolder.getRoot().getAbsolutePath());

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(new Runnable() {

            @Override
            public void run() {

                try {
                    queueWatcher.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        // Make sure watcher is fully started
        while (!queueWatcher.isStarted())

        {
            Thread.sleep(100);
        }

        Thread.sleep(100);

        File pstFile = FileUtils.toFile(QueueWatcherTest.class
                .getResource(PST_TEST_FILE));

        FileUtils.copyFileToDirectory(pstFile, queueFolder.getRoot());
        Map<String, String> metaData = new HashMap<>();
        metaData.put("mailboxname", "testmailbox");
        metaData.put("originalfilepath", "testfilepath");
        metaData.put("commentary","line1\n'line2");
        File md5File = MD5CheckSum.createMD5CheckSum(pstFile, metaData);

        FileUtils.copyFileToDirectory(md5File, queueFolder.getRoot());

        Thread.sleep(20000);

        Assert.assertEquals(0, queueFolder.getRoot().

                listFiles()

                .length);
        Assert.assertEquals(2, outFolder.getRoot().

                listFiles()

                .length);

        queueWatcher.deActivate();
        executorService.shutdown();

        while (!executorService.isTerminated())

        {
            executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        }

    }

}
