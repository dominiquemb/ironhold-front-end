package com.reqo.ironhold.storage.model;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

public class RecipientTest {

	@Test
	public void testNormalizeNoWork() {
		Recipient original = new Recipient("name", "address");
		Recipient result = Recipient.normalize(original);
		Assert.assertEquals("name", result.getName());
		Assert.assertEquals("address", result.getAddress());
	}
	
	@Test
	public void testNormalizeEmptyNameNoAtSign() {
		Recipient original = new Recipient("", "address");
		Recipient result = Recipient.normalize(original);
		Assert.assertEquals("address", result.getName());
		Assert.assertEquals("address", result.getAddress());
	}
	
	@Test
	public void testNormalizeEmptyNameWithAtSign() {
		Recipient original = new Recipient("", "address@domain.com");
		Recipient result = Recipient.normalize(original);
		Assert.assertEquals("address", result.getName());
		Assert.assertEquals("address@domain.com", result.getAddress());
	}

	@Test
	public void testNormalizeEmptyNameWithNoNameAndAtSign() {
		Recipient original = new Recipient("", "@domain.com");
		Recipient result = Recipient.normalize(original);
		Assert.assertEquals("@domain.com", result.getName());
		Assert.assertEquals("@domain.com", result.getAddress());
	}

	@Test
	public void testNormalizeArrayEmptyNameWithNoNameAndAtSign() {
		Recipient original = new Recipient("", "@domain.com");
		Recipient[] originals = new Recipient[1];
		originals[0] = original;
		Recipient[] result = Recipient.normalize(originals);
		Assert.assertEquals("@domain.com", result[0].getName());
		Assert.assertEquals("@domain.com", result[0].getAddress());
	}

}
