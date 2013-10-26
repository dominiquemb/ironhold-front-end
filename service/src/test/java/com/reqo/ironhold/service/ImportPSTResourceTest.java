package com.reqo.ironhold.service;

import com.reqo.ironhold.storage.LocalMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.security.IKeyStoreService;
import com.reqo.ironhold.storage.security.LocalKeyStoreService;
import com.reqo.ironhold.uploadclient.ImportFileClient;
import com.reqo.ironhold.utils.MD5CheckSum;
import fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean;
import fr.pilato.spring.elasticsearch.ElasticsearchNodeFactoryBean;
import org.apache.commons.io.FileUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class ImportPSTResourceTest  extends AbstractJUnit4SpringContextTests {

    @Autowired
    private LocalMimeMailMessageStorageService mimeMailMessageStorageService;

    @Autowired
    private MetaDataIndexService metaDataIndexService;

    @Autowired
    private MiscIndexService miscIndexService;

    @Autowired
    private MessageIndexService messageIndexService;

    @Autowired
    private IndexClient indexClient;

    @Autowired
    private IKeyStoreService keyStoreService;

    @Autowired
    private ElasticsearchClientFactoryBean esClient;

    @Autowired
    private ElasticsearchNodeFactoryBean esNode;


    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder clientFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder serviceFolder = new TemporaryFolder();

    private HttpServer server;
    private String baseUrl;

    @Before
    public void setUp() throws Exception {


        deleteIfExists(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore().getParentFile());
        deleteIfExists(new File("/tmp/es/data"));
        FileUtils.forceMkdir(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore());

        // start the server
        baseUrl = "http://localhost:1111/myapp/";
        server = Main.startServer(baseUrl, serviceFolder.getRoot());

    }

    private void deleteIfExists(File file) throws IOException {
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        deleteIfExists(((LocalKeyStoreService) keyStoreService).getKeyStore());
        deleteIfExists(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore().getParentFile());
        esClient.getObject().admin().indices().prepareDelete("_all").execute().actionGet();
        this.metaDataIndexService.clearCache();
        this.miscIndexService.clearCache();
        this.messageIndexService.clearCache();
    }


    @Test
    public void testUsingJerseyClient() throws Exception {
        long fileSize = 1024 * 1024 * 200;
        File completeFile = new File(clientFolder.getRoot().getAbsolutePath() + File.separator + "/test.pst");
        RandomAccessFile f = new RandomAccessFile(completeFile.getAbsolutePath(), "rw");
        f.setLength(fileSize);

        Assert.assertTrue(completeFile.exists());
        Assert.assertEquals(fileSize, completeFile.length());

        ImportFileClient client = new ImportFileClient(baseUrl, "importpst", clientFolder.getRoot(), completeFile);
        client.upload();

        File targetFile = new File(serviceFolder.getRoot().getAbsolutePath() + File.separator + client.getSessionId() + File.separator + completeFile.getName());
        Assert.assertTrue(targetFile.exists());
        Assert.assertEquals(fileSize, targetFile.length());

        Assert.assertEquals(MD5CheckSum.getMD5Checksum(completeFile), MD5CheckSum.getMD5Checksum(targetFile));
    }

}
