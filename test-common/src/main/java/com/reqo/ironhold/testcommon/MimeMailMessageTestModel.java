package com.reqo.ironhold.testcommon;

import com.reqo.ironhold.model.message.eml.IMAPMessageSource;
import com.reqo.ironhold.model.message.eml.MimeMailMessage;
import com.reqo.ironhold.model.message.pst.MailMessage;
import com.reqo.ironhold.model.message.pst.PSTMessageSource;
import junit.framework.Assert;

import java.io.IOException;

public class MimeMailMessageTestModel extends CommonTestModel {


    public static MimeMailMessage verifyStorage(MimeMailMessage storedMessage,
                                                MimeMailMessage inputMessage) throws Exception {

        Assert.assertEquals(MimeMailMessage.serialize(inputMessage),
                MimeMailMessage.serialize(storedMessage));
        Assert.assertEquals(inputMessage.getAttachments().length,
                storedMessage.getAttachments().length);
        Assert.assertEquals(MailMessage.serializeAttachments(inputMessage
                .getAttachments()), MailMessage
                .serializeAttachments(storedMessage.getAttachments()));
        Assert.assertNotNull(storedMessage.getStoredDate());

        for (int i = 0; i < inputMessage.getSources().length; i++) {
            if (inputMessage.getSources()[i] instanceof PSTMessageSource) {
                Assert.assertTrue(PSTMessageSource.sameAs((PSTMessageSource) inputMessage.getSources()[i], (PSTMessageSource) storedMessage.getSources()[i]));
            } else if (inputMessage.getSources()[i] instanceof IMAPMessageSource) {
                Assert.assertTrue(IMAPMessageSource.sameAs((IMAPMessageSource) inputMessage.getSources()[i], (IMAPMessageSource) storedMessage.getSources()[i]));
            }
        }

        return storedMessage;
    }

    public static void verifyMimeMailMessage(MimeMailMessage expected, MimeMailMessage actual) throws IOException {
        Assert.assertEquals(MimeMailMessage.serialize(expected),
                MimeMailMessage.serialize(actual));
        Assert.assertEquals(expected.getAttachments().length,
                actual.getAttachments().length);
        Assert.assertEquals(MailMessage.serializeAttachments(expected
                .getAttachments()), MailMessage
                .serializeAttachments(actual.getAttachments()));

        for (int i = 0; i < expected.getSources().length; i++) {
            if (expected.getSources()[i] instanceof PSTMessageSource) {
                Assert.assertTrue(PSTMessageSource.sameAs((PSTMessageSource) expected.getSources()[i], (PSTMessageSource) actual.getSources()[i]));
            } else if (expected.getSources()[i] instanceof IMAPMessageSource) {
                Assert.assertTrue(IMAPMessageSource.sameAs((IMAPMessageSource) expected.getSources()[i], (IMAPMessageSource) actual.getSources()[i]));
            }
        }


    }

}
