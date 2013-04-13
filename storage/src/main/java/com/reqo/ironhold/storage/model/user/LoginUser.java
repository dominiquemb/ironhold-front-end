package com.reqo.ironhold.storage.model.user;

import com.reqo.ironhold.storage.model.message.Recipient;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * User: ilya
 * Date: 4/10/13
 * Time: 6:51 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUser {
    private static Logger logger = Logger.getLogger(LoginUser.class);
    @JsonIgnore
    private ObjectMapper mapper = new ObjectMapper();
    @JsonIgnore
    protected SimpleDateFormat yearFormat = new SimpleDateFormat("YYYY");

    private String username;
    private String hashedPassword;
    private int rolesBitMask;
    private String name;
    private List<Recipient> recipients;
    private Date lastLogin;
    private Date created;

    public LoginUser() {
        this.created = new Date();
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
                false);
    }


    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<Recipient> recipients) {
        this.recipients = recipients;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getRolesBitMask() {
        return rolesBitMask;
    }

    public void setRolesBitMask(int rolesBitMask) {
        this.rolesBitMask = rolesBitMask;
    }

    public String serialize() throws IOException {
        return mapper.writeValueAsString(this);
    }

    public LoginUser deserialize(String source) throws IOException {
        return mapper.readValue(source, LoginUser.class);
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