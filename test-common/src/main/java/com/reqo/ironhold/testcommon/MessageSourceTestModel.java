package com.reqo.ironhold.testcommon;


import com.reqo.ironhold.model.message.eml.IMAPMessageSource;
import com.reqo.ironhold.model.message.pst.PSTMessageSource;

public class MessageSourceTestModel extends CommonTestModel {

    public static PSTMessageSource generatePSTMessageSource() {
        return new PSTMessageSource(df.getName() + ".pst", df.getName(), (long) 10000 * (long) Math.random(),
                df.getBirthDate());
    }

    public static IMAPMessageSource generateIMAPMessageSource() {
        IMAPMessageSource source = new IMAPMessageSource();

        source.setHostname(df.getName());
        source.setImapPort(df.getNumberBetween(1, 32000));
        source.setImapSource(df.getRandomText(100));
        source.setLoadTimestamp(df.getBirthDate());
        source.setProtocol(df.getName());
        source.setUsername(df.getName());

        return source;
    }
}
