package org.apache.isis.extensions.jpa.metamodel.facets.prop.transience;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public class JpaTransientFacetAnnotation extends FacetAbstract implements
        JpaTransientFacet {

    public static Class<? extends Facet> type() {
        return JpaTransientFacet.class;
    }

    public JpaTransientFacetAnnotation(final FacetHolder holder) {
        super(JpaTransientFacetAnnotation.type(), holder, false);
    }

}
