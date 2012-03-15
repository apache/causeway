package org.apache.isis.extensions.jpa.metamodel.facets.prop.column;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public class JpaColumnFacetAnnotation extends FacetAbstract implements
        JpaColumnFacet {

    public static Class<? extends Facet> type() {
        return JpaColumnFacet.class;
    }

    private final String name;

    public JpaColumnFacetAnnotation(final String name, final FacetHolder holder) {
        super(JpaColumnFacetAnnotation.type(), holder, false);
        this.name = name;
    }

    public String name() {
        return name;
    }

}
