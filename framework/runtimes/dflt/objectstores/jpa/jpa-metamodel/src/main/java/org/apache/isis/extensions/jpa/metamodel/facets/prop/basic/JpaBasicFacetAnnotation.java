package org.apache.isis.extensions.jpa.metamodel.facets.prop.basic;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public class JpaBasicFacetAnnotation extends FacetAbstract implements
        JpaBasicFacet {

    public static Class<? extends Facet> type() {
        return JpaBasicFacet.class;
    }

    public JpaBasicFacetAnnotation(final FacetHolder holder) {
        super(JpaBasicFacetAnnotation.type(), holder, false);
    }

}
