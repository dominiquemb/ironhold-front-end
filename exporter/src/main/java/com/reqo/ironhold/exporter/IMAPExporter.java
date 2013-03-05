package com.reqo.ironhold.exporter;

import com.reqo.ironhold.storage.IStorageService;
import com.reqo.ironhold.storage.MongoService;
import com.reqo.ironhold.storage.model.ExportableMessage;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.util.Date;
import java.util.List;

public class IMAPExporter extends MessageExporter {
	static {
		System.setProperty("jobname", IMAPExporter.class.getSimpleName());
	}

	private static Logger logger = Logger.getLogger(IMAPExporter.class);

	public IMAPExporter(String exportDir, int batchSize, int max, String client,
			String compression, String recoveryFile, IStorageService storageService) {
		super(exportDir, batchSize, max,  client,  compression, recoveryFile, storageService);

	}

    @Override
    protected List<ExportableMessage> findNewMessagesSince(Date date, int batchSize) throws Exception {
        return storageService.findNewMimeMailMessagesSince(date, batchSize);
    }

    @Override
    protected Date getUploadDate(String messageId) throws Exception {
        return storageService.getMimeMailMessageUploadDate(messageId);
    }

    public static void main(String[] args) {
		Options bean = new Options();
		CmdLineParser parser = new CmdLineParser(bean);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
			return;
		}
		try {
			IStorageService storageService = new MongoService(bean.getClient(),
					"PSTExporter");
			IMAPExporter exporter = new IMAPExporter(bean.getData(),
					bean.getBatchSize(), bean.getMax(), bean.getClient(),
					bean.getCompression(), bean.getRecoveryFile(), storageService);
			exporter.start();
		} catch (Exception e) {
			logger.error("Critical error detected. Exiting.", e);
			System.exit(0);
		}
	}
}
