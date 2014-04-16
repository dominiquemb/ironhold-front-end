package com.reqo.ironhold.web.domain;

import com.gs.collections.api.block.function.Function;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Comparator;

/**
 * User: ilya
 * Date: 11/24/13
 * Time: 1:37 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacetValue {
    public static final Comparator<FacetValue> BY_VALUE = new Comparator<FacetValue>() {
        @Override
        public int compare(FacetValue facetValue, FacetValue facetValue2) {
            return Long.compare(facetValue2.getValue(), facetValue.getValue());
        }
    };

    public static final Comparator<FacetValue> BY_NAME = new Comparator<FacetValue>() {
        @Override
        public int compare(FacetValue facetValue, FacetValue facetValue2) {
            return facetValue2.getLabel().compareTo(facetValue.getLabel());
        }
    };

    public static final Function<FacetValue, Long> TO_VALUE = new Function<FacetValue, Long>() {
        @Override
        public Long valueOf(FacetValue facetValue) {
            return facetValue.getValue();
        }
    };

    public static final Function<FacetValue, String> TO_NAME = new Function<FacetValue, String>() {
        @Override
        public String valueOf(FacetValue facetValue) {
            return facetValue.getLabel();
        }
    };

    private String label;
    private long value;
    private String facetName;

    public FacetValue() {

    }

    public FacetValue(String facetName, String label, long value) {
        this.facetName = facetName;
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String getFacetName() {
        return facetName;
    }

    public void setFacetName(String facetName) {
        this.facetName = facetName;
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
