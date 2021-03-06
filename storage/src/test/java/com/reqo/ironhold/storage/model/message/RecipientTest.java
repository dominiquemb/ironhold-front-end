package com.reqo.ironhold.storage.model.message;

import com.reqo.ironhold.web.domain.Recipient;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class RecipientTest {

    @Test
    public void testNormalizeNoWork() {
        Recipient original = Recipient.build("name", "address");
        Recipient result = Recipient.normalize(original);
        Assert.assertEquals("unknown", result.getName());
        Assert.assertEquals("address", result.getAddress());
        Assert.assertEquals(null, result.getDomain());
    }

    @Test
    public void testNormalizeEmptyNameNoAtSign() {
        Recipient original = Recipient.build("", "address");
        Recipient result = Recipient.normalize(original);
        Assert.assertEquals("unknown", result.getName());
        Assert.assertEquals("address", result.getAddress());
        Assert.assertEquals(null, result.getDomain());
    }

    @Test
    public void testNormalizeEmptyNameWithAtSign() {
        Recipient original = Recipient.build("", "address@domain.com");
        Recipient result = Recipient.normalize(original);
        Assert.assertEquals("unknown", result.getName());
        Assert.assertEquals("address@domain.com", result.getAddress());
        Assert.assertEquals("domain.com", result.getDomain());
    }

    @Test
    public void testNormalizeEmptyNameWithNoNameAndAtSign() {
        Recipient original = Recipient.build("", "@domain.com");
        Recipient result = Recipient.normalize(original);
        Assert.assertEquals("unknown", result.getName());
        Assert.assertEquals("@domain.com", result.getAddress());
        Assert.assertEquals("domain.com", result.getDomain());
    }

    @Test
    public void testNormalizeArrayEmptyNameWithNoNameAndAtSign() {
        Recipient original = Recipient.build("", "@domain.com");
        Recipient[] originals = new Recipient[1];
        originals[0] = original;
        Recipient[] result = Recipient.normalize(originals);
        Assert.assertEquals("unknown", result[0].getName());
        Assert.assertEquals("@domain.com", result[0].getAddress());
        Assert.assertEquals("domain.com", result[0].getDomain());
    }

    @Test
    public void mytest() {
        String name = "?? ??";

        Assert.assertTrue(name.matches("^[\\?*\\s*]*$"));
    }

    @Test
    public void testGLTNormalizeFile() throws IOException {
        File file = FileUtils.toFile(RecipientTest.class
                .getResource("/gltnames.sorted"));
        List<String> lines = FileUtils.readLines(file);
        Set<String> cleanLines = new HashSet<String>();
        for (String line : lines) {
            cleanLines.add(Recipient.normalize(Recipient.build(line, line))
                    .getName());
        }
        Assert.assertEquals(74704, lines.size());
        Assert.assertEquals(24280, cleanLines.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testVBMNormalizeFile() throws IOException {
        File file = FileUtils.toFile(RecipientTest.class
                .getResource("/vbmnames.sorted"));
        List<String> lines = FileUtils.readLines(file);
        Set<String> cleanLines = new HashSet<String>();
        for (String line : lines) {
            cleanLines.add(Recipient.normalize(Recipient.build(line, line))
                    .getName());
        }
        Assert.assertEquals(125265, lines.size());
        Assert.assertEquals(42973, cleanLines.size());
    }

    @Test
    public void testTWFNormalizeFile() throws IOException {
        File file = FileUtils.toFile(RecipientTest.class
                .getResource("/twfnames.sorted"));
        List<String> lines = FileUtils.readLines(file);
        Set<String> cleanLines = new HashSet<String>();
        for (String line : lines) {
            cleanLines.add(Recipient.normalize(Recipient.build(line, line))
                    .getName());
        }
        Assert.assertEquals(21468, lines.size());
        Assert.assertEquals(11784, cleanLines.size());
    }

    @Test
    public void testStringConstructor() {
        Recipient recipient = new Recipient("ig@reqo.com");
        Assert.assertEquals("ig", recipient.getName());
        Assert.assertEquals("ig@reqo.com", recipient.getAddress());
        Assert.assertEquals("reqo.com", recipient.getDomain());
    }
}
