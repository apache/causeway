package org.apache.isis.extensions.jpa.metamodel.facets.prop.id;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.MarkerFacetAbstract;


public abstract class JpaIdFacetAbstract extends MarkerFacetAbstract implements
        JpaIdFacet {

    public static Class<? extends Facet> type() {
        return JpaIdFacet.class;
    }

    public JpaIdFacetAbstract(final FacetHolder holder) {
        super(JpaIdFacetAbstract.type(), holder);
    }
}
