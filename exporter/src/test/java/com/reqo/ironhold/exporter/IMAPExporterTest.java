package com.reqo.ironhold.exporter;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.reqo.ironhold.exporter.model.MailMessageTestModel;
import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.MimeMailMessage;
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

import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * User: ilya
 * Date: 3/2/13
 * Time: 4:46 PM
 */
@SuppressWarnings("unchecked")
public class IMAPExporterTest {
    @Rule
    public TemporaryFolder backupFolder = new TemporaryFolder();

    private MongodExecutable mongodExe;
    private MongodProcess mongod;

    private Mongo mongo;
    private DB db;
    private MailMessageTestModel testModel;
    private static final String DATABASENAME = "MongoServiceTest";

    private String[] messages = new String[]{"/message1.eml", "/message2.eml", "/message3.eml", "/message4.eml"};

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
        Date startDate = new Date();
        IStorageService storageService = new MongoService(mongo, db);

        List<MimeMailMessage> mimeMailMessageList = new ArrayList<MimeMailMessage>();

        for (String message : messages) {
            File file = FileUtils.toFile(IMAPExporterTest.class
                    .getResource(message));
            InputStream is = new FileInputStream(file);
            MimeMessage mimeMessage = new MimeMessage(null, is);

            MimeMailMessage mailMessage = new MimeMailMessage();
            mailMessage.loadMimeMessage(mimeMessage);

            mimeMailMessageList.add(mailMessage);
            storageService.store(mailMessage);
        }

        String recoveryFile = backupFolder.getRoot().getPath()+File.separator + "recovery.txt";
        IMAPExporter exporter = new IMAPExporter(backupFolder.getRoot().getAbsolutePath(), 100, messages.length, "test", CompressorStreamFactory.GZIP, recoveryFile, storageService);

        exporter.start();

        Collection<File> files = FileUtils.listFiles(backupFolder.getRoot(), new String[]{CompressorStreamFactory.GZIP}, true);

        Assert.assertEquals(messages.length, files.size());

        for (MimeMailMessage message : mimeMailMessageList) {
            File exportedFile = new File(message.getExportDirName(backupFolder.getRoot().toPath().toString(), "test")
                    + File.separator
                    + message.getExportFileName(CompressorStreamFactory.GZIP));

            String exportedContents = decompress(exportedFile, CompressorStreamFactory.GZIP);
            String messageContents = message.getRawContents();

            Assert.assertEquals(messageContents, exportedContents);


            MimeMailMessage exportedMessage = new MimeMailMessage();
            exportedMessage.deserializeMessageWithAttachments(exportedContents);

            Assert.assertEquals(message.getAttachments().length, exportedMessage.getAttachments().length);


            for (int i = 0; i < message.getAttachments().length; i++) {
                junit.framework.Assert.assertEquals(message.getAttachments()[i].getBody(), exportedMessage.getAttachments()[i].getBody());
                junit.framework.Assert.assertEquals(message.getAttachments()[i].getFileName(), exportedMessage.getAttachments()[i].getFileName());
                junit.framework.Assert.assertEquals(message.getAttachments()[i].getSize(), exportedMessage.getAttachments()[i].getSize());
                junit.framework.Assert.assertEquals(message.getAttachments()[i].getFileExt(), exportedMessage.getAttachments()[i].getFileExt());
            }
        }

        String recoveryString = FileUtils.readFileToString(new File(recoveryFile));

        Date recoveryDate = new Date();
        recoveryDate.setTime(Long.parseLong(recoveryString));
        Assert.assertTrue(recoveryDate.before(new Date()));
        Assert.assertTrue(recoveryDate.after(startDate));
    }

    @Test
    public void testWithCompression() throws Exception {
        File file = FileUtils.toFile(IMAPExporterTest.class
                .getResource(messages[0]));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(null, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);


        IStorageService storageService = new MongoService(mongo, db);

        storageService.store(mailMessage);

        String recoveryFile = backupFolder.getRoot().getPath()+File.separator + "recovery.txt";

        IMAPExporter exporter = new IMAPExporter(backupFolder.getRoot().getAbsolutePath(), 100, 1, "test", CompressorStreamFactory.GZIP, recoveryFile, storageService);

        String messageContents = mailMessage.serializeMessageWithAttachments();
        File outputFile = new File(backupFolder.getRoot().getAbsolutePath() + File.separator + "testCompress");
        exporter.compress(outputFile, messageContents);

        String decompressed = decompress(outputFile, CompressorStreamFactory.GZIP);

        Assert.assertEquals(messageContents, decompressed);

    }

    @Test
    public void testWithoutCompression() throws Exception {
        File file = FileUtils.toFile(IMAPExporterTest.class
                .getResource(messages[0]));
        InputStream is = new FileInputStream(file);
        MimeMessage mimeMessage = new MimeMessage(null, is);

        MimeMailMessage mailMessage = new MimeMailMessage();
        mailMessage.loadMimeMessage(mimeMessage);


        IStorageService storageService = new MongoService(mongo, db);

        storageService.store(mailMessage);

        String recoveryFile = backupFolder.getRoot().getPath()+File.separator + "recovery.txt";

        IMAPExporter exporter = new IMAPExporter(backupFolder.getRoot().getAbsolutePath(), 100, 1, "test", "NONE", recoveryFile, storageService);

        String messageContents = mailMessage.serializeMessageWithAttachments();
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
