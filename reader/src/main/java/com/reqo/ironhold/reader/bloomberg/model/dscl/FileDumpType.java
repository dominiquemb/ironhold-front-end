//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.09.22 at 10:36:41 PM EDT 
//


package com.reqo.ironhold.reader.bloomberg.model.dscl;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for FileDumpType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="FileDumpType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Version" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Disclaimer" type="{}DisclaimerType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FileDumpType", propOrder = {
        "version",
        "disclaimer"
})
@XmlRootElement(name = "FileDump")
public class FileDumpType {

    @XmlElement(name = "Version", required = true)
    protected String version;
    @XmlElement(name = "Disclaimer")
    protected List<DisclaimerType> disclaimer;

    /**
     * Gets the value of the version property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the disclaimer property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the disclaimer property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDisclaimer().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link DisclaimerType }
     */
    public List<DisclaimerType> getDisclaimer() {
        if (disclaimer == null) {
            disclaimer = new ArrayList<DisclaimerType>();
        }
        return this.disclaimer;
    }

}