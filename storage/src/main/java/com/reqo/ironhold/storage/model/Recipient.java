package com.reqo.ironhold.storage.model;

import javax.mail.Address;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Recipient {
    private String name;
    private String address;

    public Recipient() {

    }

    public Recipient(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public Recipient(Address address) {
    	String addressString = address.toString().trim();
		if (addressString.contains("<")) {
			this.name = addressString.substring(0, addressString.indexOf("<")).trim();
			this.address = addressString.substring(addressString.indexOf("<")).trim();
		} else {
			this.name = addressString;
			this.address = addressString;
		}
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
}
