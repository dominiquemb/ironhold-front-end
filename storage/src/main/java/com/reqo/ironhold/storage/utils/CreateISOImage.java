package com.reqo.ironhold.storage.utils;

import com.reqo.ironhold.storage.security.CheckSumHelper;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ISO9660Directory;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.ISO9660RootDirectory;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.CreateISO;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.ISO9660Config;
import de.tu_darmstadt.informatik.rbg.hatlak.iso9660.impl.ISOImageFileHandler;
import de.tu_darmstadt.informatik.rbg.hatlak.rockridge.impl.RockRidgeConfig;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

/**
 * User: ilya
 * Date: 4/12/13
 * Time: 3:04 PM
 */
public class CreateISOImage {
    static {
        System.setProperty("jobname", CreateISOImage.class.getSimpleName());
    }

    private static Logger logger = Logger.getLogger(CreateISOImage.class);
    private final File dataRoot;
    private final File archiveRoot;
    private final File keyStoreRoot;
    private final File backupRoot;


    public CreateISOImage(File dataRoot, File archiveRoot, File keyStoreRoot, File backupRoot) {
        this.dataRoot = dataRoot;
        this.archiveRoot = archiveRoot;
        this.keyStoreRoot = keyStoreRoot;
        this.backupRoot = backupRoot;

    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Options bean = new Options();
        CmdLineParser parser = new CmdLineParser(bean);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }

        ApplicationContext context = new ClassPathXmlApplicationContext("mimeMailMessageStorageServiceContext.xml");
        CreateISOImage util = context.getBean(CreateISOImage.class);
        util.run(bean.getClient(), bean.getPartition(), bean.getSubPartitionMask());
        System.exit(1);
    }

    private void run(String client, String partitionFocus, String subPartitionMask) throws Exception {
        File backupDir = new File(this.backupRoot.getAbsolutePath() + File.separator + client + File.separator + partitionFocus);
        FileUtils.forceMkdir(backupDir);
        ISO9660RootDirectory isoImage = new ISO9660RootDirectory();
        ISO9660Directory isoDataRoot = isoImage.addDirectory("dataroot");
        ISO9660Directory isoKeystore = isoImage.addDirectory("keystore");

        ISO9660Directory isoPartition = new ISO9660Directory(partitionFocus);
        isoDataRoot.addDirectory(isoPartition);

        File partitionDir = new File(this.dataRoot.getAbsolutePath() + File.separator + client + File.separator + partitionFocus);

        for (File subPartition : partitionDir.listFiles()) {
            if (subPartition.getName().startsWith(subPartitionMask)) {
                ISO9660Directory isoSubPartition = new ISO9660Directory(subPartition.getName());
                isoSubPartition.addContentsRecursively(subPartition);
                isoPartition.addDirectory(isoSubPartition);
            }
        }
        isoKeystore.addFile(this.keyStoreRoot.getAbsoluteFile() + File.separator + client + ".keystore");

        File isoFile = new File(backupDir.getAbsolutePath() + File.separator + client + "_" + partitionFocus + "_" + subPartitionMask + ".iso");
        File isoChecksumFile = new File(backupDir.getAbsolutePath() + File.separator + client + "_" + partitionFocus + "_" + subPartitionMask + ".checksum");

        CreateISO c = new CreateISO(new ISOImageFileHandler(isoFile), isoImage);

        c.process(new ISO9660Config(), new RockRidgeConfig(), null, null);

        FileUtils.writeStringToFile(isoChecksumFile, CheckSumHelper.getCheckSum(FileUtils.readFileToByteArray(isoFile)));

    }


    static class Options {

        @Option(name = "-client", usage = "client name", required = true)
        private String client;

        @Option(name = "-partition", usage = "partition to reconcile", required = false)
        private String partition;

        @Option(name = "-subPartitionMask", usage = "subPartition to reconcile", required = false)
        private String subPartitionMask;

        public String getClient() {
            return client;
        }

        String getPartition() {
            return partition;
        }

        String getSubPartitionMask() {
            return subPartitionMask;
        }

    }

}