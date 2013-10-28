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
import java.util.*;

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
    protected SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

    private String username;
    private String hashedPassword;
    private int rolesBitMask;
    private String name;
    private Recipient mainRecipient;
    private List<Recipient> recipients;
    private Date lastLogin;
    private Date created;
    private String id;
    private String[] sources;
    private String lastLoginChannel;
    private String lastLoginContext;

    public LoginUser() {
        this.created = new Date();
        this.id = UUID.randomUUID().toString();
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
                false);
    }

    public void addSource(String sourceId) {
        if (sources == null) {
            sources = new String[]{sourceId};
        } else {
            String[] copy = Arrays.copyOf(sources, sources.length + 1);
            copy[sources.length] = sourceId;
            sources = copy;
        }

    }

    public String[] getSources() {
        return sources;
    }

    public void setSources(String[] sources) {
        this.sources = sources;
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
        this.recipients = new ArrayList<>();
        if (recipients != null) {
            for (Recipient recipient : recipients) {
                this.recipients.add(Recipient.normalize(recipient));
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase();
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

    public Recipient getMainRecipient() {
        return mainRecipient;
    }

    public void setMainRecipient(Recipient mainRecipient) {
        this.mainRecipient = Recipient.normalize(mainRecipient);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastLoginChannel() {
        return lastLoginChannel;
    }

    public void setLastLoginChannel(String lastLoginChannel) {
        this.lastLoginChannel = lastLoginChannel;
    }

    public String getLastLoginContext() {
        return lastLoginContext;
    }

    public void setLastLoginContext(String lastLoginContext) {
        this.lastLoginContext = lastLoginContext;
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

    public boolean hasRole(RoleEnum roleEnum) {
        int andResult = rolesBitMask & roleEnum.getValue();
        return andResult == roleEnum.getValue();
    }

    public boolean hasSource(String id) {
        if (sources == null) return false;
        for (String source : sources) {
            if (id.equals(source)) {
                return true;
            }
        }
        return false;
    }
}
