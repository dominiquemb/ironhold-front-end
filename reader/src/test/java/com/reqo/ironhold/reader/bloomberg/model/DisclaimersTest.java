package com.reqo.ironhold.reader.bloomberg.model;

import com.reqo.ironhold.reader.bloomberg.model.msg.Disclaimer;
import com.reqo.ironhold.reader.bloomberg.model.msg.FileDump;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * User: ilya
 * Date: 6/6/13
 * Time: 8:38 AM
 */
public class DisclaimersTest {

    @Test
    public void testProcessXMLFile() throws Exception {
        File file = FileUtils.toFile(DisclaimersTest.class
                .getResource("/a30066168.dscl.130604.xml"));
        JAXBContext jaxbContext = JAXBContext.newInstance(Disclaimer.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        FileDump disclaimers = (FileDump) jaxbUnmarshaller.unmarshal(file);

        Assert.assertEquals(19, disclaimers.getDisclaimer().size());
        for (Disclaimer disclaimer : disclaimers.getDisclaimer()) {
            Assert.assertEquals(1, disclaimer.getDisclaimerText().getContent().size());
            Assert.assertTrue(disclaimer.getDisclaimerText().getContent().get(0).length() > 0);

            Assert.assertEquals(1, disclaimer.getDisclaimerReference().getContent().size());
            Assert.assertTrue(disclaimer.getDisclaimerReference().getContent().get(0).length() > 0);
            Assert.assertTrue(Integer.parseInt(disclaimer.getDisclaimerReference().getContent().get(0)) > 0);
        }
    }
}
