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
 *         &lt;element ref="{}StartTime"/>
 *         &lt;element ref="{}StartTimeUTC"/>
 *         &lt;element ref="{}EndTime"/>
 *         &lt;element ref="{}EndTimeUTC"/>
 *         &lt;element ref="{}ConversationID"/>
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
        "startTime",
        "startTimeUTC",
        "endTime",
        "endTimeUTC",
        "conversationID"
})
@XmlRootElement(name = "History")
public class History {

    @XmlElement(name = "User", required = true)
    protected User user;
    @XmlElement(name = "DateTime", required = true)
    protected DateTime dateTime;
    @XmlElement(name = "DateTimeUTC", required = true)
    protected DateTimeUTC dateTimeUTC;
    @XmlElement(name = "StartTime", required = true)
    protected StartTime startTime;
    @XmlElement(name = "StartTimeUTC", required = true)
    protected StartTimeUTC startTimeUTC;
    @XmlElement(name = "EndTime", required = true)
    protected EndTime endTime;
    @XmlElement(name = "EndTimeUTC", required = true)
    protected EndTimeUTC endTimeUTC;
    @XmlElement(name = "ConversationID", required = true)
    protected ConversationID conversationID;
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
     * Gets the value of the startTime property.
     *
     * @return possible object is
     *         {@link StartTime }
     */
    public StartTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     *
     * @param value allowed object is
     *              {@link StartTime }
     */
    public void setStartTime(StartTime value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the startTimeUTC property.
     *
     * @return possible object is
     *         {@link StartTimeUTC }
     */
    public StartTimeUTC getStartTimeUTC() {
        return startTimeUTC;
    }

    /**
     * Sets the value of the startTimeUTC property.
     *
     * @param value allowed object is
     *              {@link StartTimeUTC }
     */
    public void setStartTimeUTC(StartTimeUTC value) {
        this.startTimeUTC = value;
    }

    /**
     * Gets the value of the endTime property.
     *
     * @return possible object is
     *         {@link EndTime }
     */
    public EndTime getEndTime() {
        return endTime;
    }

    /**
     * Sets the value of the endTime property.
     *
     * @param value allowed object is
     *              {@link EndTime }
     */
    public void setEndTime(EndTime value) {
        this.endTime = value;
    }

    /**
     * Gets the value of the endTimeUTC property.
     *
     * @return possible object is
     *         {@link EndTimeUTC }
     */
    public EndTimeUTC getEndTimeUTC() {
        return endTimeUTC;
    }

    /**
     * Sets the value of the endTimeUTC property.
     *
     * @param value allowed object is
     *              {@link EndTimeUTC }
     */
    public void setEndTimeUTC(EndTimeUTC value) {
        this.endTimeUTC = value;
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