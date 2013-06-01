package com.reqo.ironhold.storage.model;

import com.reqo.ironhold.storage.model.message.source.IMAPMessageSource;
import com.reqo.ironhold.storage.model.message.source.PSTMessageSource;

import java.util.UUID;

public class MessageSourceTestModel extends CommonTestModel {

    public static PSTMessageSource generatePSTMessageSource() {
        return new PSTMessageSource(UUID.randomUUID().toString(), df.getName() + ".pst", df.getName(), (long) 10000 * (long) Math.random(),
                df.getBirthDate(), UUID.randomUUID().toString());
    }

    public static IMAPMessageSource generateIMAPMessageSource() {
        return new IMAPMessageSource(UUID.randomUUID().toString(), df.getRandomText(100), df.getName(), df.getNumberBetween(1, 32000), df.getName());
    }
}
