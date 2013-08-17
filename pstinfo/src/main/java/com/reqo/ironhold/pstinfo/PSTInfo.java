package com.reqo.ironhold.pstinfo;

import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessageStore;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

/**
 * User: ilya
 * Date: 8/14/13
 * Time: 12:05 AM
 */
public class PSTInfo {
    private final File file;

    public PSTInfo(File file) {
        this.file = file;
    }

    public void dump(PrintStream ps) throws Exception {
        PSTFile pstFile = new PSTFile(file);

        PSTMessageStore store = pstFile.getMessageStore();
        ps.println("PSTMessageStore.displayName: " + store.getDisplayName());
        ps.println("PSTMessageStore.addrType: " + store.getAddrType());
        ps.println("PSTMessageStore.comment: " + store.getComment());
        ps.println("PSTMessageStore.creationTime: " + store.getCreationTime());
        ps.println("PSTMessageStore.emailAddress: " + store.getEmailAddress());
        ps.println("PSTMessageStore.lastModified: " + store.getLastModificationTime());
        ps.println("PSTMessageStore.messageClass: " + store.getMessageClass());


        processFolder(ps, "", pstFile.getRootFolder());

    }


    private void processFolder(PrintStream ps, String folderPath, PSTFolder folder)
            throws Exception {
        folder.getSubFolderCount();
        // go through the folders...
        if (folder.getSubFolderCount() > 0) {
            List<PSTFolder> childFolders = folder.getSubFolders();
            for (PSTFolder childFolder : childFolders) {
                processFolder(ps, folderPath + "/" + childFolder.getDisplayName(),
                        childFolder);
            }
        }

        // and now the emails for this folder
        if (folder.getContentCount() > 0) {
            ps.println("\t" + folderPath + ": " + folder.getContentCount());
            ps.println("\t\tPSTFolder.emailAddress: " + folder.getEmailAddress());
            ps.println("\t\tPSTFolder.comment: " + folder.getComment());
            ps.println("\t\tPSTFolder.addrType: " + folder.getAddrType());
        }

    }
}
