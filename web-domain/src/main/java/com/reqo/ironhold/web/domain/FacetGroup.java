package com.reqo.ironhold.web.domain;

import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.list.ImmutableList;

import java.util.Comparator;

/**
 * User: ilya
 * Date: 11/24/13
 * Time: 1:37 PM
 */
public class FacetGroup {

    public static final Comparator<FacetGroup> BY_ORDER = new Comparator<FacetGroup>() {
        @Override
        public int compare(FacetGroup facetGroup, FacetGroup facetGroup2) {
            return Integer.compare(facetGroup.getName().getOrder(), facetGroup2.getName().getOrder());
        }
    };

    private FacetGroupName name;
    private ImmutableList<FacetValue> valueMap;

    public static final Function<FacetGroup, FacetGroupName> GET_NAME = new Function<FacetGroup, FacetGroupName>() {
        @Override
        public FacetGroupName valueOf(FacetGroup facetGroup) {
            return facetGroup.getName();
        }
    };

    public FacetGroup(FacetGroupName name, ImmutableList<FacetValue> valueMap) {
        this.name = name;
        this.valueMap = valueMap;
    }

    public FacetGroupName getName() {
        return name;
    }

    public void setName(FacetGroupName name) {
        this.name = name;
    }

    public ImmutableList<FacetValue> getValueMap() {
        return valueMap;
    }

    public void setValueMap(ImmutableList<FacetValue> valueMap) {
        this.valueMap = valueMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacetGroup that = (FacetGroup) o;

        if (!name.equals(that.name)) return false;
        if (valueMap != null ? !valueMap.equals(that.valueMap) : that.valueMap != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (valueMap != null ? valueMap.hashCode() : 0);
        return result;
    }
}
