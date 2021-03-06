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
 *         &lt;element ref="{}User"/>
 *         &lt;element ref="{}DateTime"/>
 *         &lt;element ref="{}DateTimeUTC"/>
 *         &lt;element ref="{}ConversationID"/>
 *         &lt;choice>
 *           &lt;element ref="{}FileName"/>
 *           &lt;element ref="{}Reference"/>
 *         &lt;/choice>
 *         &lt;element ref="{}FileID" minOccurs="0"/>
 *         &lt;element ref="{}FileSize" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="InteractionType" type="{http://www.w3.org/2001/XMLSchema}string" default=" " />
 *       &lt;attribute name="DeviceType" type="{http://www.w3.org/2001/XMLSchema}string" default=" " />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "user",
        "dateTime",
        "dateTimeUTC",
        "conversationID",
        "fileName",
        "reference",
        "fileID",
        "fileSize"
})
@XmlRootElement(name = "Attachment")
public class Attachment {

    @XmlElement(name = "User", required = true)
    protected User user;
    @XmlElement(name = "DateTime", required = true)
    protected DateTime dateTime;
    @XmlElement(name = "DateTimeUTC", required = true)
    protected DateTimeUTC dateTimeUTC;
    @XmlElement(name = "ConversationID", required = true)
    protected ConversationID conversationID;
    @XmlElement(name = "FileName")
    protected FileName fileName;
    @XmlElement(name = "Reference")
    protected Reference reference;
    @XmlElement(name = "FileID")
    protected FileID fileID;
    @XmlElement(name = "FileSize")
    protected FileSize fileSize;
    @XmlAttribute(name = "InteractionType")
    protected String interactionType;
    @XmlAttribute(name = "DeviceType")
    protected String deviceType;

    /**
     * Gets the value of the user property.
     *
     * @return possible object is
     *         {@link User }
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     *
     * @param value allowed object is
     *              {@link User }
     */
    public void setUser(User value) {
        this.user = value;
    }

    /**
     * Gets the value of the dateTime property.
     *
     * @return possible object is
     *         {@link DateTime }
     */
    public DateTime getDateTime() {
        return dateTime;
    }

    /**
     * Sets the value of the dateTime property.
     *
     * @param value allowed object is
     *              {@link DateTime }
     */
    public void setDateTime(DateTime value) {
        this.dateTime = value;
    }

    /**
     * Gets the value of the dateTimeUTC property.
     *
     * @return possible object is
     *         {@link DateTimeUTC }
     */
    public DateTimeUTC getDateTimeUTC() {
        return dateTimeUTC;
    }

    /**
     * Sets the value of the dateTimeUTC property.
     *
     * @param value allowed object is
     *              {@link DateTimeUTC }
     */
    public void setDateTimeUTC(DateTimeUTC value) {
        this.dateTimeUTC = value;
    }

    /**
     * Gets the value of the conversationID property.
     *
     * @return possible object is
     *         {@link ConversationID }
     */
    public ConversationID getConversationID() {
        return conversationID;
    }

    /**
     * Sets the value of the conversationID property.
     *
     * @param value allowed object is
     *              {@link ConversationID }
     */
    public void setConversationID(ConversationID value) {
        this.conversationID = value;
    }

    /**
     * Gets the value of the fileName property.
     *
     * @return possible object is
     *         {@link FileName }
     */
    public FileName getFileName() {
        return fileName;
    }

    /**
     * Sets the value of the fileName property.
     *
     * @param value allowed object is
     *              {@link FileName }
     */
    public void setFileName(FileName value) {
        this.fileName = value;
    }

    /**
     * Gets the value of the reference property.
     *
     * @return possible object is
     *         {@link Reference }
     */
    public Reference getReference() {
        return reference;
    }

    /**
     * Sets the value of the reference property.
     *
     * @param value allowed object is
     *              {@link Reference }
     */
    public void setReference(Reference value) {
        this.reference = value;
    }

    /**
     * Gets the value of the fileID property.
     *
     * @return possible object is
     *         {@link FileID }
     */
    public FileID getFileID() {
        return fileID;
    }

    /**
     * Sets the value of the fileID property.
     *
     * @param value allowed object is
     *              {@link FileID }
     */
    public void setFileID(FileID value) {
        this.fileID = value;
    }

    /**
     * Gets the value of the fileSize property.
     *
     * @return possible object is
     *         {@link FileSize }
     */
    public FileSize getFileSize() {
        return fileSize;
    }

    /**
     * Sets the value of the fileSize property.
     *
     * @param value allowed object is
     *              {@link FileSize }
     */
    public void setFileSize(FileSize value) {
        this.fileSize = value;
    }

    /**
     * Gets the value of the interactionType property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getInteractionType() {
        if (interactionType == null) {
            return " ";
        } else {
            return interactionType;
        }
    }

    /**
     * Sets the value of the interactionType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setInteractionType(String value) {
        this.interactionType = value;
    }

    /**
     * Gets the value of the deviceType property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDeviceType() {
        if (deviceType == null) {
            return " ";
        } else {
            return deviceType;
        }
    }

    /**
     * Sets the value of the deviceType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDeviceType(String value) {
        this.deviceType = value;
    }

}
