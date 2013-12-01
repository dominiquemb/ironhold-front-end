package com.reqo.ironhold.web.domain;

import java.util.Comparator;

/**
 * User: ilya
 * Date: 12/1/13
 * Time: 8:47 AM
 */
public class FacetGroupName {


    public static final FacetGroupName FACET_FROM_NAME = new FacetGroupName("From by name:", "from", 1);
    public static final FacetGroupName FACET_FROM_DOMAIN = new FacetGroupName("From by domain:", "from_domain", 2);
    public static final FacetGroupName FACET_TO_NAME = new FacetGroupName("To by name:", "to", 3);
    public static final FacetGroupName FACET_TO_DOMAIN = new FacetGroupName("To by domain:", "to_domain", 4);
    public static final FacetGroupName FACET_YEAR = new FacetGroupName("Year:", "date", 5);
    public static final FacetGroupName FACET_FILEEXT = new FacetGroupName("Attachment file type:", "file_ext", 6);
    public static final FacetGroupName FACET_MSGTYPE = new FacetGroupName("Message type:", "msg_type", 7);

    private final String value;
    private final String label;
    private final int order;

    private FacetGroupName(String label, String value, int order) {
        this.value = value;
        this.label = label;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static FacetGroupName fromValue(String value) {
        if (value.equals(FACET_FROM_NAME.getValue())) {
            return FACET_FROM_NAME;
        } else if (value.equals(FACET_FROM_DOMAIN.getValue())) {
            return FACET_FROM_DOMAIN;
        } else if (value.equals(FACET_TO_NAME.getValue())) {
            return FACET_TO_NAME;
        } else if (value.equals(FACET_YEAR.getValue())) {
            return FACET_YEAR;
        } else if (value.equals(FACET_TO_DOMAIN.getValue())) {
            return FACET_TO_DOMAIN;
        } else if (value.equals(FACET_FILEEXT.getValue())) {
            return FACET_FILEEXT;
        } else if (value.equals(FACET_MSGTYPE.getValue())) {
            return FACET_MSGTYPE;
        }

        throw new IllegalArgumentException("Unknown facet group name " + value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacetGroupName that = (FacetGroupName) o;

        if (!label.equals(that.label)) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + label.hashCode();
        return result;
    }
}
