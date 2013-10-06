//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.08.22 at 07:22:02 PM EDT 
//


package com.reqo.ironhold.reader.bloomberg.model.msg;

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
 *         &lt;element ref="{}FirstName"/>
 *         &lt;element ref="{}LastName"/>
 *         &lt;element ref="{}FirmNumber" minOccurs="0"/>
 *         &lt;element ref="{}AccountName"/>
 *         &lt;element ref="{}AccountNumber" minOccurs="0"/>
 *         &lt;element ref="{}BloombergUUID" minOccurs="0"/>
 *         &lt;element ref="{}BloombergEmailAddress"/>
 *         &lt;element ref="{}CorporateEmailAddress"/>
 *         &lt;element ref="{}ClientID1" minOccurs="0"/>
 *         &lt;element ref="{}ClientID2" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "firstName",
        "lastName",
        "firmNumber",
        "accountName",
        "accountNumber",
        "bloombergUUID",
        "bloombergEmailAddress",
        "corporateEmailAddress",
        "clientID1",
        "clientID2"
})
@XmlRootElement(name = "UserInfo")
public class UserInfo {

    @XmlElement(name = "FirstName", required = true)
    protected FirstName firstName;
    @XmlElement(name = "LastName", required = true)
    protected LastName lastName;
    @XmlElement(name = "FirmNumber")
    protected FirmNumber firmNumber;
    @XmlElement(name = "AccountName", required = true)
    protected AccountName accountName;
    @XmlElement(name = "AccountNumber")
    protected AccountNumber accountNumber;
    @XmlElement(name = "BloombergUUID")
    protected BloombergUUID bloombergUUID;
    @XmlElement(name = "BloombergEmailAddress", required = true)
    protected BloombergEmailAddress bloombergEmailAddress;
    @XmlElement(name = "CorporateEmailAddress", required = true)
    protected CorporateEmailAddress corporateEmailAddress;
    @XmlElement(name = "ClientID1")
    protected ClientID1 clientID1;
    @XmlElement(name = "ClientID2")
    protected ClientID2 clientID2;

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
     * Gets the value of the accountName property.
     *
     * @return possible object is
     *         {@link AccountName }
     */
    public AccountName getAccountName() {
        return accountName;
    }

    /**
     * Sets the value of the accountName property.
     *
     * @param value allowed object is
     *              {@link AccountName }
     */
    public void setAccountName(AccountName value) {
        this.accountName = value;
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
     * Gets the value of the bloombergUUID property.
     *
     * @return possible object is
     *         {@link BloombergUUID }
     */
    public BloombergUUID getBloombergUUID() {
        return bloombergUUID;
    }

    /**
     * Sets the value of the bloombergUUID property.
     *
     * @param value allowed object is
     *              {@link BloombergUUID }
     */
    public void setBloombergUUID(BloombergUUID value) {
        this.bloombergUUID = value;
    }

    /**
     * Gets the value of the bloombergEmailAddress property.
     *
     * @return possible object is
     *         {@link BloombergEmailAddress }
     */
    public BloombergEmailAddress getBloombergEmailAddress() {
        return bloombergEmailAddress;
    }

    /**
     * Sets the value of the bloombergEmailAddress property.
     *
     * @param value allowed object is
     *              {@link BloombergEmailAddress }
     */
    public void setBloombergEmailAddress(BloombergEmailAddress value) {
        this.bloombergEmailAddress = value;
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

}
