package com.reqo.ironhold.web.domain;

import com.gs.collections.api.block.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Recipient {
    public static final Function<Recipient, String> TO_NAME = new Function<Recipient, String>() {
        @Override
        public String valueOf(Recipient recipient) {
            return recipient.getName();
        }
    };

    private static Logger logger = Logger.getLogger(Recipient.class);

    private String name;
    private String address;
    private String domain;

    public Recipient() {

    }

    public Recipient(String recipient) {
        this.address = recipient.trim().toLowerCase();
        this.name = getNameFromAddress(this.address);
        setDomain(address != null && address.contains("@")
                && address.lastIndexOf('@') < address.length() ? address
                .substring(address.lastIndexOf('@') + 1) : null);
    }

    public static Recipient build(String name, String address) {
        Recipient recipient = new Recipient();

        if (name == null) {
            recipient.setName(getNameFromAddress(address));
        } else {
            recipient.setName(name);
        }

        recipient.setAddress(address);
        recipient.setDomain(address != null && address.contains("@")
                && address.lastIndexOf('@') < address.length() ? address
                .substring(address.lastIndexOf('@') + 1) : null);

        return recipient;
    }

    public static String getNameFromAddress(String address) {
        if (address != null && address.contains("@")) {
            return address.substring(0, address.indexOf("@"));
        } else {
            return StringUtils.EMPTY;
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
        if (recipient.getAddress() != null) {
            recipient.setAddress(recipient.getAddress().trim().toLowerCase());
        }
        if (recipient.getDomain() != null) {
            recipient.setDomain(recipient.getDomain().trim().toLowerCase());
        }

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

        return Recipient.build(name, result.getAddress());
    }

    private static Recipient updateNameAndAddress(Recipient recipient) {
        if (recipient.getName() != null
                && recipient.getName().trim().length() > 0) {
            return recipient;
        } else {
            if (recipient.getAddress() == null) {
                return Recipient.build("unknown", "unknown");
            }
            String address = recipient.getAddress().trim();
            if (address.contains("@")) {
                String name = address.substring(0, address.indexOf("@"));
                if (name.trim().length() > 0) {
                    return Recipient.build(name, address);
                } else {
                    return Recipient.build(address, address);
                }

            } else {
                return Recipient.build(address, address);
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
