package com.reqo.ironhold.exporter;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.reqo.ironhold.exporter.model.MailMessageTestModel;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.MailMessage;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.List;

/**
 * User: ilya
 * Date: 3/2/13
 * Time: 4:46 PM
 */
public class PSTExporterTest {
    @Rule
    public TemporaryFolder backupFolder = new TemporaryFolder();

    private MongodExecutable mongodExe;
    private MongodProcess mongod;

    private Mongo mongo;
    private DB db;
    private MailMessageTestModel testModel;
    private static final String DATABASENAME = "MongoServiceTest";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        mongodExe = runtime.prepare(new MongodConfig(Version.Main.V2_0, 12345,
                Network.localhostIsIPv6()));
        mongod = mongodExe.start();
        mongo = new Mongo("localhost", 12345);
        db = mongo.getDB(DATABASENAME);


        testModel = new MailMessageTestModel("/attachments.pst");
    }

    @After
    public void tearDown() throws Exception {
        mongod.stop();
        mongodExe.stop();
    }

    @Test
    public void testFullExport() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

        List<MailMessage> pstMessages = testModel.generatePSTMessages();

        for (MailMessage pstMessage : pstMessages) {
            storageService.store(pstMessage);
        }

        PSTExporter exporter = new PSTExporter(backupFolder.getRoot().getAbsolutePath(), 100, 1000, "test", CompressorStreamFactory.GZIP, storageService);

        exporter.start();

        Collection<File> files = FileUtils.listFiles(backupFolder.getRoot(), new String[]{CompressorStreamFactory.GZIP}, true);

        Assert.assertEquals(pstMessages.size(), files.size());

        for (MailMessage pstMessage : pstMessages) {
            File exportedFile = new File(pstMessage.getExportDirName(backupFolder.getRoot().toPath().toString(), "test")
                    + File.separator
                    + pstMessage.getExportFileName(CompressorStreamFactory.GZIP));

            String exportedContents = decompress(exportedFile, CompressorStreamFactory.GZIP);
            String messageContents = MailMessage.serializeMailMessageWithAttachments(pstMessage);

            Assert.assertEquals(messageContents, exportedContents);

            MailMessage exportedMessage = MailMessage.deserializeMailMessageWithAttachments(exportedContents);

            Assert.assertEquals(pstMessage.getAttachments().length, exportedMessage.getAttachments().length);


            for (int i = 0; i<pstMessage.getAttachments().length; i++) {
                junit.framework.Assert.assertEquals(pstMessage.getAttachments()[i].getBody(), exportedMessage.getAttachments()[i].getBody());
                junit.framework.Assert.assertEquals(pstMessage.getAttachments()[i].getFileName(), exportedMessage.getAttachments()[i].getFileName());
                junit.framework.Assert.assertEquals(pstMessage.getAttachments()[i].getSize(), exportedMessage.getAttachments()[i].getSize());
                junit.framework.Assert.assertEquals(pstMessage.getAttachments()[i].getFileExt(), exportedMessage.getAttachments()[i].getFileExt());
            }
        }
    }

    @Test
    public void testWithCompression() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

        MailMessage pstMessage = testModel.generatePSTMessage();

        storageService.store(pstMessage);

        PSTExporter exporter = new PSTExporter(backupFolder.getRoot().getAbsolutePath(), 100, 100, "test", CompressorStreamFactory.GZIP, storageService);

        String messageContents = MailMessage.serializeMailMessage(pstMessage);
        File outputFile = new File(backupFolder.getRoot().getAbsolutePath() + File.separator + "testCompress");
        exporter.compress(outputFile, messageContents);

        String decompressed = decompress(outputFile, CompressorStreamFactory.GZIP);

        Assert.assertEquals(messageContents, decompressed);

    }

    @Test
    public void testWithoutCompression() throws Exception {
        IStorageService storageService = new MongoService(mongo, db);

        MailMessage pstMessage = testModel.generatePSTMessage();

        storageService.store(pstMessage);

        PSTExporter exporter = new PSTExporter(backupFolder.getRoot().getAbsolutePath(), 100, 100, "test", "NONE", storageService);

        String messageContents = MailMessage.serializeMailMessage(pstMessage);
        File outputFile = new File(backupFolder.getRoot().getAbsolutePath() + File.separator + "testCompress");
        exporter.compress(outputFile, messageContents);

        String decompressed = decompress(outputFile, "NONE");

        Assert.assertEquals(messageContents, decompressed);

    }

    private String decompress(File file, String compression) throws Exception {
        if (!compression.equals("NONE")) {
            CompressorInputStream compressedStream = null;
            try {
                compressedStream = new CompressorStreamFactory()
                        .createCompressorInputStream(compression,
                                new FileInputStream(file));
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                final byte[] buffer = new byte[1024];
                int n;
                while (-1 != (n = compressedStream.read(buffer))) {
                    out.write(buffer, 0, n);
                }
                out.close();

                return out.toString();
            } finally {
                if (compressedStream != null)
                    compressedStream.close();
            }
        } else {
            return FileUtils.readFileToString(file);
        }
    }
}
