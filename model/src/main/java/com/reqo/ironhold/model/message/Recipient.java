package com.reqo.ironhold.model.message;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;

public class Recipient {
	private static Logger logger = Logger.getLogger(Recipient.class);

	private String name;
	private String address;
	private String domain;

	public Recipient() {

	}

	public Recipient(String name, String address) {
		this.name = name == null ? StringUtils.EMPTY : name;
		this.address = address;
		this.domain = address != null && address.contains("@")
				&& address.lastIndexOf('@') < address.length() ? address
				.substring(address.lastIndexOf('@') + 1) : null;
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

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
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
		Recipient result = Recipient.updateNameAndAddress(recipient);
		return Recipient.formatName(result);
	}

	private static Recipient formatName(Recipient result) {
		String name = result.getName().trim();

		// non breaking space

		name = StringUtils.removeStart(name, Character.toString((char) 160));

		while (name.contains("  ")) {
			name = StringUtils.replace(name, "  ", " ");
		}

		if (name.contains("@")) {
			name = name.substring(0, name.indexOf("@"));
		}
		if (name.contains("(")) {
			name = name.substring(0, name.indexOf("("));
		}
		if (name.contains("<")) {
			name = name.substring(0, name.indexOf("<"));
		}
		if (name.contains("/")) {
			name = name.substring(0, name.indexOf("/"));
		}
		if (name.contains("[")) {
			name = name.substring(0, name.indexOf("["));
		}

		name = name.toLowerCase();

		name = WordUtils.capitalize(name, ' ', '.', '_', '*', ',', '-', '\'');
		name = name.trim();
		name = name.replaceAll("^[.,\"'(</]+", "");
		name = name.replaceAll("[.,\"'(</]+$", "");
		name = name.trim();

		if (name.length() == 0 || name.matches("^[\\?*\\s*]*$")
				|| !name.contains(" ")) {
			name = "unknown";
		}

		if (!result.getName().equals(name)) {
			logger.debug(String.format("%s\t%s", result.getName(), name,
					(int) name.charAt(0)));
		}

		return new Recipient(name, result.getAddress());
	}

	private static Recipient updateNameAndAddress(Recipient recipient) {
		if (recipient.getName() != null
				&& recipient.getName().trim().length() > 0) {
			return recipient;
		} else {
			if (recipient.getAddress() == null) {
				return new Recipient("unknown", "unknown");
			}
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

		for (int i = 0; i < recipients.length; i++) {
			results[i] = Recipient.normalize(recipients[i]);
		}

		return results;
	}
}
