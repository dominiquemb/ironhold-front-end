//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.08.22 at 07:23:03 PM EDT 
//


package com.reqo.ironhold.reader.bloomberg.model.ib;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}LoginName"/>
 *         &lt;element ref="{}FirstName"/>
 *         &lt;element ref="{}LastName"/>
 *         &lt;element ref="{}ClientID1" minOccurs="0"/>
 *         &lt;element ref="{}ClientID2" minOccurs="0"/>
 *         &lt;element ref="{}UUID" minOccurs="0"/>
 *         &lt;element ref="{}FirmNumber" minOccurs="0"/>
 *         &lt;element ref="{}AccountNumber" minOccurs="0"/>
 *         &lt;element ref="{}CompanyName"/>
 *         &lt;element ref="{}EmailAddress"/>
 *         &lt;element ref="{}CorporateEmailAddress"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "loginName",
        "firstName",
        "lastName",
        "clientID1",
        "clientID2",
        "uuid",
        "firmNumber",
        "accountNumber",
        "companyName",
        "emailAddress",
        "corporateEmailAddress"
})
@XmlRootElement(name = "User")
public class User {

    @XmlElement(name = "LoginName", required = true)
    protected LoginName loginName;
    @XmlElement(name = "FirstName", required = true)
    protected FirstName firstName;
    @XmlElement(name = "LastName", required = true)
    protected LastName lastName;
    @XmlElement(name = "ClientID1")
    protected ClientID1 clientID1;
    @XmlElement(name = "ClientID2")
    protected ClientID2 clientID2;
    @XmlElement(name = "UUID")
    protected UUID uuid;
    @XmlElement(name = "FirmNumber")
    protected FirmNumber firmNumber;
    @XmlElement(name = "AccountNumber")
    protected AccountNumber accountNumber;
    @XmlElement(name = "CompanyName", required = true)
    protected CompanyName companyName;
    @XmlElement(name = "EmailAddress", required = true)
    protected EmailAddress emailAddress;
    @XmlElement(name = "CorporateEmailAddress", required = true)
    protected CorporateEmailAddress corporateEmailAddress;

    /**
     * Gets the value of the loginName property.
     *
     * @return possible object is
     *         {@link LoginName }
     */
    public LoginName getLoginName() {
        return loginName;
    }

    /**
     * Sets the value of the loginName property.
     *
     * @param value allowed object is
     *              {@link LoginName }
     */
    public void setLoginName(LoginName value) {
        this.loginName = value;
    }

    /**
     * Gets the value of the firstName property.
     *
     * @return possible object is
     *         {@link FirstName }
     */
    public FirstName getFirstName() {
        return firstName;
    }

    /**
     * Sets the value of the firstName property.
     *
     * @param value allowed object is
     *              {@link FirstName }
     */
    public void setFirstName(FirstName value) {
        this.firstName = value;
    }

    /**
     * Gets the value of the lastName property.
     *
     * @return possible object is
     *         {@link LastName }
     */
    public LastName getLastName() {
        return lastName;
    }

    /**
     * Sets the value of the lastName property.
     *
     * @param value allowed object is
     *              {@link LastName }
     */
    public void setLastName(LastName value) {
        this.lastName = value;
    }

    /**
     * Gets the value of the clientID1 property.
     *
     * @return possible object is
     *         {@link ClientID1 }
     */
    public ClientID1 getClientID1() {
        return clientID1;
    }

    /**
     * Sets the value of the clientID1 property.
     *
     * @param value allowed object is
     *              {@link ClientID1 }
     */
    public void setClientID1(ClientID1 value) {
        this.clientID1 = value;
    }

    /**
     * Gets the value of the clientID2 property.
     *
     * @return possible object is
     *         {@link ClientID2 }
     */
    public ClientID2 getClientID2() {
        return clientID2;
    }

    /**
     * Sets the value of the clientID2 property.
     *
     * @param value allowed object is
     *              {@link ClientID2 }
     */
    public void setClientID2(ClientID2 value) {
        this.clientID2 = value;
    }

    /**
     * Gets the value of the uuid property.
     *
     * @return possible object is
     *         {@link UUID }
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     *
     * @param value allowed object is
     *              {@link UUID }
     */
    public void setUUID(UUID value) {
        this.uuid = value;
    }

    /**
     * Gets the value of the firmNumber property.
     *
     * @return possible object is
     *         {@link FirmNumber }
     */
    public FirmNumber getFirmNumber() {
        return firmNumber;
    }

    /**
     * Sets the value of the firmNumber property.
     *
     * @param value allowed object is
     *              {@link FirmNumber }
     */
    public void setFirmNumber(FirmNumber value) {
        this.firmNumber = value;
    }

    /**
     * Gets the value of the accountNumber property.
     *
     * @return possible object is
     *         {@link AccountNumber }
     */
    public AccountNumber getAccountNumber() {
        return accountNumber;
    }

    /**
     * Sets the value of the accountNumber property.
     *
     * @param value allowed object is
     *              {@link AccountNumber }
     */
    public void setAccountNumber(AccountNumber value) {
        this.accountNumber = value;
    }

    /**
     * Gets the value of the companyName property.
     *
     * @return possible object is
     *         {@link CompanyName }
     */
    public CompanyName getCompanyName() {
        return companyName;
    }

    /**
     * Sets the value of the companyName property.
     *
     * @param value allowed object is
     *              {@link CompanyName }
     */
    public void setCompanyName(CompanyName value) {
        this.companyName = value;
    }

    /**
     * Gets the value of the emailAddress property.
     *
     * @return possible object is
     *         {@link EmailAddress }
     */
    public EmailAddress getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the value of the emailAddress property.
     *
     * @param value allowed object is
     *              {@link EmailAddress }
     */
    public void setEmailAddress(EmailAddress value) {
        this.emailAddress = value;
    }

    /**
     * Gets the value of the corporateEmailAddress property.
     *
     * @return possible object is
     *         {@link CorporateEmailAddress }
     */
    public CorporateEmailAddress getCorporateEmailAddress() {
        return corporateEmailAddress;
    }

    /**
     * Sets the value of the corporateEmailAddress property.
     *
     * @param value allowed object is
     *              {@link CorporateEmailAddress }
     */
    public void setCorporateEmailAddress(CorporateEmailAddress value) {
        this.corporateEmailAddress = value;
    }

}
