package com.reqo.ironhold.serviceintegrationtests.resources;

import com.reqo.ironhold.serviceintegrationtests.SampleMiscIndexService;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.net.URI;
import java.util.UUID;

/**
 * User: ilya
 * Date: 11/3/13
 * Time: 9:48 AM
 */
public class ImportPSTResourceIT  {
    private static Logger logger = Logger.getLogger(ImportPSTResourceIT.class);

    private URI siteBase;
    private HtmlUnitDriver driver;
    private static final String URL = System.getProperty("integration-test.url");

    @Before
    public void setUp() throws Exception {
        siteBase = new URI(URL + "importpst/session");
        driver = new HtmlUnitDriver();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                driver.close();
            }
        });



    }


    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetSession() {
        driver.get(siteBase.toString());
        String source = driver.getPageSource();
        logger.info(source);
        Assert.assertTrue(source.contains("j_spring_security"));

        WebElement username = driver.findElementByName("j_username");
        WebElement password  = driver.findElementByName("j_password");
        WebElement submit  = driver.findElementByName("submit");

        username.sendKeys(SampleMiscIndexService.sampleClientKey + "/" + SampleMiscIndexService.sampleUsername);
        password.sendKeys(SampleMiscIndexService.samplePassword);
        submit.click();

        source = driver.getPageSource();
        logger.info(source);

        Assert.assertFalse(source.contains("HTTP"));

        String sessionId = source;
        Assert.assertNotNull(UUID.fromString(sessionId));

    }
}