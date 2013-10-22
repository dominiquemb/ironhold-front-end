//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.08.22 at 07:23:03 PM EDT 
//


package com.reqo.ironhold.reader.bloomberg.model.ib;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


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
 *         &lt;element ref="{}RoomID"/>
 *         &lt;element ref="{}StartTime"/>
 *         &lt;element ref="{}StartTimeUTC"/>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element ref="{}Attachment"/>
 *           &lt;element ref="{}Invite"/>
 *           &lt;element ref="{}ParticipantEntered"/>
 *           &lt;element ref="{}ParticipantLeft"/>
 *           &lt;element ref="{}Message"/>
 *           &lt;element ref="{}History"/>
 *           &lt;element ref="{}SystemMessage"/>
 *         &lt;/choice>
 *         &lt;element ref="{}EndTime"/>
 *         &lt;element ref="{}EndTimeUTC"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Perspective" type="{http://www.w3.org/2001/XMLSchema}string" default=" " />
 *       &lt;attribute name="RoomType" type="{http://www.w3.org/2001/XMLSchema}string" default="C" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "roomID",
        "startTime",
        "startTimeUTC",
        "attachmentOrInviteOrParticipantEntered",
        "endTime",
        "endTimeUTC"
})
@XmlRootElement(name = "Conversation")
public class Conversation {

    @XmlElement(name = "RoomID", required = true)
    protected RoomID roomID;
    @XmlElement(name = "StartTime", required = true)
    protected StartTime startTime;
    @XmlElement(name = "StartTimeUTC", required = true)
    protected StartTimeUTC startTimeUTC;
    @XmlElements({
            @XmlElement(name = "Attachment", type = Attachment.class),
            @XmlElement(name = "Invite", type = Invite.class),
            @XmlElement(name = "ParticipantEntered", type = ParticipantEntered.class),
            @XmlElement(name = "ParticipantLeft", type = ParticipantLeft.class),
            @XmlElement(name = "Message", type = Message.class),
            @XmlElement(name = "History", type = History.class),
            @XmlElement(name = "SystemMessage", type = SystemMessage.class)
    })
    protected List<Object> attachmentOrInviteOrParticipantEntered;
    @XmlElement(name = "EndTime", required = true)
    protected EndTime endTime;
    @XmlElement(name = "EndTimeUTC", required = true)
    protected EndTimeUTC endTimeUTC;
    @XmlAttribute(name = "Perspective")
    protected String perspective;
    @XmlAttribute(name = "RoomType")
    protected String roomType;

    /**
     * Gets the value of the roomID property.
     *
     * @return possible object is
     *         {@link RoomID }
     */
    public RoomID getRoomID() {
        return roomID;
    }

    /**
     * Sets the value of the roomID property.
     *
     * @param value allowed object is
     *              {@link RoomID }
     */
    public void setRoomID(RoomID value) {
        this.roomID = value;
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
     * Gets the value of the attachmentOrInviteOrParticipantEntered property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attachmentOrInviteOrParticipantEntered property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttachmentOrInviteOrParticipantEntered().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Attachment }
     * {@link Invite }
     * {@link ParticipantEntered }
     * {@link ParticipantLeft }
     * {@link Message }
     * {@link History }
     * {@link SystemMessage }
     */
    public List<Object> getAttachmentOrInviteOrParticipantEntered() {
        if (attachmentOrInviteOrParticipantEntered == null) {
            attachmentOrInviteOrParticipantEntered = new ArrayList<Object>();
        }
        return this.attachmentOrInviteOrParticipantEntered;
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
     * Gets the value of the perspective property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getPerspective() {
        if (perspective == null) {
            return " ";
        } else {
            return perspective;
        }
    }

    /**
     * Sets the value of the perspective property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPerspective(String value) {
        this.perspective = value;
    }

    /**
     * Gets the value of the roomType property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getRoomType() {
        if (roomType == null) {
            return "C";
        } else {
            return roomType;
        }
    }

    /**
     * Sets the value of the roomType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRoomType(String value) {
        this.roomType = value;
    }

}