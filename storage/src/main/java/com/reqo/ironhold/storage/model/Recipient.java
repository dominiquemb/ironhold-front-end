package com.reqo.ironhold.storage.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Recipient {
    private String name;
    private String address;

    public Recipient() {

    }

    public Recipient(String name, String address) {
        this.name = name == null ? StringUtils.EMPTY : name;
        this.address = address;
    }

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);

    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

	public static Recipient normalize(Recipient recipient) {
		if (recipient.getName().trim().length() > 0) {
			return recipient;
		} else {
			String address = recipient.getAddress().trim();
			if (address.contains("@")) {
				String name = address.substring(0, address.indexOf("@"));
				if (name.trim().length() > 0) {
					return new Recipient(name, address);
				} else {
					return new Recipient(address, address);	
				}
				
			} else {
				return new Recipient(address, address);
			}
		}
	}
	
	public static Recipient[] normalize(Recipient[] recipients) {
		Recipient[] results = new Recipient[recipients.length];
		
		for (int i =0 ; i< recipients.length; i++) {
			results[i] = Recipient.normalize(recipients[i]);
		}
		
		return results;
	}
}
