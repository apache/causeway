package org.apache.isis.extensions.jpa.metamodel.facets.prop.onetoone;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public class JpaOneToOneFacetAnnotation extends FacetAbstract implements
        JpaOneToOneFacet {

    public static Class<? extends Facet> type() {
        return JpaOneToOneFacet.class;
    }

    public JpaOneToOneFacetAnnotation(final FacetHolder holder) {
        super(JpaOneToOneFacetAnnotation.type(), holder, false);
    }

}
