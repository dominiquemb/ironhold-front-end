package com.reqo.ironhold.storage.model;

import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PSTMessageTestModel extends CommonTestModel {
	private static final int MAX_MESSAGES_TO_LOAD = 5;
    private final int maxMessagesToLoad;
	private List<PSTMessage> pstMessages = new ArrayList<PSTMessage>();
    private PSTMessageSource source;

    public PSTMessageTestModel(String pstFilePath, int maxMessagesToLoad)  throws IOException, PSTException {
        this.maxMessagesToLoad = maxMessagesToLoad;
        File file = FileUtils.toFile(PSTMessageTestModel.class
                .getResource(pstFilePath));
        System.out.println("Loading messages from " + file);
        PSTFile pstFile;
        pstFile = new PSTFile(file);

        loadAllMessages("", pstFile.getRootFolder());

    }
	public PSTMessageTestModel(String pstFilePath) throws IOException, PSTException {
        this(pstFilePath, MAX_MESSAGES_TO_LOAD);
	}

    public PSTMessage generateOriginalPSTMessage() {
        return pstMessages.get(0);
    }


	private void loadAllMessages(String folderPath, PSTFolder folder)
			throws PSTException, IOException {
		if (pstMessages.size() == maxMessagesToLoad) {
			return;
		}

		if (folder.hasSubfolders()) {
			Vector<PSTFolder> childFolders = folder.getSubFolders();
			for (PSTFolder childFolder : childFolders) {
				loadAllMessages(
						folderPath + "/" + childFolder.getDisplayName(),
						childFolder);
				if (pstMessages.size() == maxMessagesToLoad) {
					return;
				}
			}
		}

		if (folder.getContentCount() > 0) {
			PSTMessage message = (PSTMessage) folder.getNextChild();
			while (message != null) {
				System.out.println(message.getInternetMessageId() + " : "
						+ message.getNumberOfAttachments());

				pstMessages.add(message);
				if (pstMessages.size() == maxMessagesToLoad) {
					return;
				}
				message = (PSTMessage) folder.getNextChild();
			}
		}
	}

    public List<PSTMessage> generateOriginalPSTMessages() {
        return pstMessages;
    }

}
