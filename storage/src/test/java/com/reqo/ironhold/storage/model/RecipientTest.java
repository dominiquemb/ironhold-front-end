package com.reqo.ironhold.storage.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class RecipientTest {

	@Test
	public void testNormalizeNoWork() {
		Recipient original = new Recipient("name", "address");
		Recipient result = Recipient.normalize(original);
		Assert.assertEquals("Name", result.getName());
		Assert.assertEquals("address", result.getAddress());
	}

	@Test
	public void testNormalizeEmptyNameNoAtSign() {
		Recipient original = new Recipient("", "address");
		Recipient result = Recipient.normalize(original);
		Assert.assertEquals("Address", result.getName());
		Assert.assertEquals("address", result.getAddress());
	}

	@Test
	public void testNormalizeEmptyNameWithAtSign() {
		Recipient original = new Recipient("", "address@domain.com");
		Recipient result = Recipient.normalize(original);
		Assert.assertEquals("Address", result.getName());
		Assert.assertEquals("address@domain.com", result.getAddress());
	}

	@Test
	public void testNormalizeEmptyNameWithNoNameAndAtSign() {
		Recipient original = new Recipient("", "@domain.com");
		Recipient result = Recipient.normalize(original);
		Assert.assertEquals("unknown", result.getName());
		Assert.assertEquals("@domain.com", result.getAddress());
	}

	@Test
	public void testNormalizeArrayEmptyNameWithNoNameAndAtSign() {
		Recipient original = new Recipient("", "@domain.com");
		Recipient[] originals = new Recipient[1];
		originals[0] = original;
		Recipient[] result = Recipient.normalize(originals);
		Assert.assertEquals("unknown", result[0].getName());
		Assert.assertEquals("@domain.com", result[0].getAddress());
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
			cleanLines.add(Recipient.normalize(new Recipient(line, line))
					.getName());
		}
		Assert.assertEquals(lines.size(), 74704);
		Assert.assertEquals(cleanLines.size(), 59489);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testVBMNormalizeFile() throws IOException {
		File file = FileUtils.toFile(RecipientTest.class
				.getResource("/vbmnames.sorted"));
		List<String> lines = FileUtils.readLines(file);
		Set<String> cleanLines = new HashSet<String>();
		for (String line : lines) {
			cleanLines.add(Recipient.normalize(new Recipient(line, line))
					.getName());
		}
		Assert.assertEquals(lines.size(), 125265);
		Assert.assertEquals(cleanLines.size(), 94136);
	}

	@Test
	public void testTWFNormalizeFile() throws IOException {
		File file = FileUtils.toFile(RecipientTest.class
				.getResource("/twfnames.sorted"));
		List<String> lines = FileUtils.readLines(file);
		Set<String> cleanLines = new HashSet<String>();
		for (String line : lines) {
			cleanLines.add(Recipient.normalize(new Recipient(line, line))
					.getName());
		}
		Assert.assertEquals(lines.size(), 21468);
		Assert.assertEquals(cleanLines.size(), 18351);
	}

}
