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
 *         &lt;element ref="{}DisclaimerReference"/>
 *         &lt;element ref="{}DisclaimerText"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "disclaimerReference",
        "disclaimerText"
})
@XmlRootElement(name = "Disclaimer")
public class Disclaimer {

    @XmlElement(name = "DisclaimerReference", required = true)
    protected DisclaimerReference disclaimerReference;
    @XmlElement(name = "DisclaimerText", required = true)
    protected DisclaimerText disclaimerText;

    /**
     * Gets the value of the disclaimerReference property.
     *
     * @return possible object is
     *         {@link DisclaimerReference }
     */
    public DisclaimerReference getDisclaimerReference() {
        return disclaimerReference;
    }

    /**
     * Sets the value of the disclaimerReference property.
     *
     * @param value allowed object is
     *              {@link DisclaimerReference }
     */
    public void setDisclaimerReference(DisclaimerReference value) {
        this.disclaimerReference = value;
    }

    /**
     * Gets the value of the disclaimerText property.
     *
     * @return possible object is
     *         {@link DisclaimerText }
     */
    public DisclaimerText getDisclaimerText() {
        return disclaimerText;
    }

    /**
     * Sets the value of the disclaimerText property.
     *
     * @param value allowed object is
     *              {@link DisclaimerText }
     */
    public void setDisclaimerText(DisclaimerText value) {
        this.disclaimerText = value;
    }

}
