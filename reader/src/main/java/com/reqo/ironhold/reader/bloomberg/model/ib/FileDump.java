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
 *         &lt;element ref="{}Version"/>
 *         &lt;element ref="{}Conversation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}NextFile" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "version",
        "conversation",
        "nextFile"
})
@XmlRootElement(name = "FileDump")
public class FileDump {

    @XmlElement(name = "Version", required = true)
    protected Version version;
    @XmlElement(name = "Conversation")
    protected List<Conversation> conversation;
    @XmlElement(name = "NextFile")
    protected NextFile nextFile;

    /**
     * Gets the value of the version property.
     *
     * @return possible object is
     *         {@link Version }
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is
     *              {@link Version }
     */
    public void setVersion(Version value) {
        this.version = value;
    }

    /**
     * Gets the value of the conversation property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the conversation property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConversation().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Conversation }
     */
    public List<Conversation> getConversation() {
        if (conversation == null) {
            conversation = new ArrayList<Conversation>();
        }
        return this.conversation;
    }

    /**
     * Gets the value of the nextFile property.
     *
     * @return possible object is
     *         {@link NextFile }
     */
    public NextFile getNextFile() {
        return nextFile;
    }

    /**
     * Sets the value of the nextFile property.
     *
     * @param value allowed object is
     *              {@link NextFile }
     */
    public void setNextFile(NextFile value) {
        this.nextFile = value;
    }

}
