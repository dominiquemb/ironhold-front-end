package com.reqo.ironhold.web.domain;

import com.gs.collections.api.block.function.Function;

import java.util.Comparator;

/**
 * User: ilya
 * Date: 11/24/13
 * Time: 1:37 PM
 */
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

    public FacetValue(String label, long value) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacetValue that = (FacetValue) o;

        if (value != that.value) return false;
        if (!label.equals(that.label)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = label.hashCode();
        result = 31 * result + (int) (value ^ (value >>> 32));
        return result;
    }
}
