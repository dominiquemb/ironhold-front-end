package com.reqo.ironhold.storage.model;

import com.reqo.ironhold.storage.model.metadata.PSTFileMeta;

import java.util.Date;

/**
 * User: ilya
 * Date: 3/24/13
 * Time: 5:47 PM
 */
public class PSTFileMetaTestModel extends CommonTestModel {
    public static PSTFileMeta generate() {
        PSTFileMeta metaData = new PSTFileMeta("pstFileName", "mailBoxName",
                "originalFilePath", "commentary", generateText(), "hostname", (int) Math.random() * 500000,
                new Date());
        for (int i = 0; i < Math.random() * 100; i++) {
            metaData.updateSizeStatistics((int)Math.random() * 500000, (int)Math.random() * 250000);
        }

        metaData.persistCalculations();

        return metaData;
    }
}
