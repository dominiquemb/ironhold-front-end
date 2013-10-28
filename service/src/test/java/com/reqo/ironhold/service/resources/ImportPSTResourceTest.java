package com.reqo.ironhold.service.resources;

import com.reqo.ironhold.service.beans.WorkingDir;
import com.reqo.ironhold.storage.LocalMimeMailMessageStorageService;
import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.es.IndexClient;
import com.reqo.ironhold.storage.security.IKeyStoreService;
import com.reqo.ironhold.storage.security.LocalKeyStoreService;
import com.reqo.ironhold.uploadclient.ImportFileClient;
import com.reqo.ironhold.utils.MD5CheckSum;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean;
import fr.pilato.spring.elasticsearch.ElasticsearchTransportClientFactoryBean;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.ContextLoaderListener;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:testContextClient.xml")
public class ImportPSTResourceTest extends JerseyTest {

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
    private ElasticsearchTransportClientFactoryBean esClient;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder clientFolder = new TemporaryFolder();


    @Autowired
    private WorkingDir workingDir;

    public ImportPSTResourceTest() {
        super(new WebAppDescriptor.Builder("com.reqo.ironhold.service")
                .servletPath("service")
                .contextPath("webapi")
                .contextParam("contextConfigLocation", "classpath:/testContext.xml")
                .servletClass(SpringServlet.class)
                .initParam("javax.ws.rs.Application", "com.reqo.ironhold.service.JerseyApplication")
                .initParam("com.sun.jersey.api.json.POJOMappingFeature", "true")
                .contextListenerClass(ContextLoaderListener.class).build());
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        FileUtils.deleteDirectory(new File(workingDir.getWorkDir()));
        FileUtils.forceMkdir(new File(workingDir.getWorkDir()));

        deleteIfExists(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore().getParentFile());
        deleteIfExists(new File("/tmp/es/data"));
        FileUtils.forceMkdir(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore());
    }

    private void deleteIfExists(File file) throws IOException {
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.forceDelete(new File(workingDir.getWorkDir()));
        deleteIfExists(((LocalKeyStoreService) keyStoreService).getKeyStore());
        deleteIfExists(((LocalMimeMailMessageStorageService) mimeMailMessageStorageService).getDataStore().getParentFile());
    }


    @Test
    public void testUsingJerseyClient() throws Exception {
        long fileSize = 1024 * 1024 * 200;
        File completeFile = new File(clientFolder.getRoot().getAbsolutePath() + File.separator + "/test.pst");
        RandomAccessFile f = new RandomAccessFile(completeFile.getAbsolutePath(), "rw");
        f.setLength(fileSize);

        Assert.assertTrue(completeFile.exists());
        Assert.assertEquals(fileSize, completeFile.length());

        ImportFileClient client = new ImportFileClient(getBaseURI().toString() + "webapi/", "importpst", clientFolder.getRoot(), completeFile);
        client.upload();

        File targetFile = new File(new File(workingDir.getWorkDir()).getAbsolutePath() + File.separator + client.getSessionId() + File.separator + completeFile.getName());
        Assert.assertTrue(targetFile.exists());
        Assert.assertEquals(fileSize, targetFile.length());

        Assert.assertEquals(MD5CheckSum.getMD5Checksum(completeFile), MD5CheckSum.getMD5Checksum(targetFile));
    }

}
