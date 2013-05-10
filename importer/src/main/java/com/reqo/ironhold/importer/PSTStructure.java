package com.reqo.ironhold.importer;

import com.pff.PSTFile;
import com.pff.PSTFolder;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.net.UnknownHostException;
import java.util.List;

public class PSTStructure {

    private static final int MILLIS_IN_A_SEC = 1000;


    public static void main(String args[]) throws Exception {
        new PSTStructure().processMessages(args[0]);
    }

    public PSTStructure() throws UnknownHostException {
    }

    public void processMessages(String file) throws Exception {

        PSTFile pstFile = new PSTFile(file);
        try {

            long started = System.currentTimeMillis();

            processFolder("", pstFile.getRootFolder());

            long finished = System.currentTimeMillis();

            String timeTook = DurationFormatUtils.formatDurationWords(finished
                    - started, true, true);
            float duration = (finished - started) / MILLIS_IN_A_SEC;

            System.out.println("time took: " + timeTook);
        } finally {
            pstFile.getFileHandle().close();
        }

    }

    private void processFolder(String folderPath, PSTFolder folder)
            throws Exception {

        if (folderPath.endsWith("Inbox")) {
            boolean t = true;
        }

        folder.getSubFolderCount();
        if (folder.getContentCount() > 0) {
            System.out.println("Processing " + folderPath + "[" + (folder.getSubFolders().size()) + "] ["
                    + folder.getContentCount() + " items] [" + folder.getAssociateContentCount() + " ass items]");
        } else {
            System.out.println("Processing " + folderPath);

        }
        // go through the folders...
        if (folder.getSubFolderCount() > 0) {
            List<PSTFolder> childFolders = folder.getSubFolders();
            for (PSTFolder childFolder : childFolders) {
                processFolder(folderPath + "/" + childFolder.getDisplayName(),
                        childFolder);
            }
        }

        // and now the emails for this folder
        if (folder.getContentCount() > 0) {
            System.out.println(folderPath + ":" + folder.getContentCount());

        }

    }

}
