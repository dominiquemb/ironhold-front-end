package com.reqo.ironhold.service.resources;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.FindBy;

import java.net.URI;
import java.util.UUID;

/**
 * User: ilya
 * Date: 11/3/13
 * Time: 9:48 AM
 */
public class ImportPSTResourceIT extends TestCase {
    private static Logger logger = Logger.getLogger(ImportPSTResourceIT.class);

    private URI siteBase;
    private HtmlUnitDriver driver;


    @Override
    protected void setUp() throws Exception {
        super.setUp();


        siteBase = new URI("http://localhost:10001/service/importpst/session");
        driver = new HtmlUnitDriver();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                driver.close();
            }
        });
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetSession() {
        driver.get(siteBase.toString());
        String source = driver.getPageSource();
        logger.info(source);
        assertTrue(source.contains("j_spring_security"));

        WebElement username = driver.findElementByName("j_username");
        WebElement password  = driver.findElementByName("j_password");
        WebElement submit  = driver.findElementByName("submit");

        username.sendKeys("demo/demo");
        password.sendKeys("demo");
        submit.click();

        source = driver.getPageSource();
        logger.info(source);

        assertFalse(source.contains("HTTP"));

        String sessionId = source;
        assertNotNull(UUID.fromString(sessionId));

    }
}