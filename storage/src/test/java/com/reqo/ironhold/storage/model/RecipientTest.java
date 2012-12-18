package com.reqo.ironhold.storage.model;

import static org.junit.Assert.*;

import javax.mail.Address;

import junit.framework.Assert;

import org.junit.Test;

public class RecipientTest {

	@Test
	public void testConstructorWithEmail() {
		Recipient r = new Recipient(new Address() {

			@Override
			public String getType() {
				return null;
			}

			@Override
			public String toString() {
				return "john <bob@domain.com>";
			}

			@Override
			public boolean equals(Object address) {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
		
		Assert.assertEquals("john", r.getName());
		Assert.assertEquals("<bob@domain.com>", r.getAddress());
	}
	

	@Test
	public void testConstructorWithoutEmail() {
		Recipient r = new Recipient(new Address() {

			@Override
			public String getType() {
				return null;
			}

			@Override
			public String toString() {
				return "john";
			}

			@Override
			public boolean equals(Object address) {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
		
		Assert.assertEquals("john", r.getName());
		Assert.assertEquals("john", r.getAddress());
	}

}
