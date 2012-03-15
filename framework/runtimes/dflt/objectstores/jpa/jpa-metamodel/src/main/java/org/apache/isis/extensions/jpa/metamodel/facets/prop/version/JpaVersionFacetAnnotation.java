package org.apache.isis.extensions.jpa.metamodel.facets.prop.version;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public class JpaVersionFacetAnnotation extends FacetAbstract implements
        JpaVersionFacet {

    public static Class<? extends Facet> type() {
        return JpaVersionFacet.class;
    }

    public JpaVersionFacetAnnotation(final FacetHolder holder) {
        super(JpaVersionFacetAnnotation.type(), holder, false);
    }

}
