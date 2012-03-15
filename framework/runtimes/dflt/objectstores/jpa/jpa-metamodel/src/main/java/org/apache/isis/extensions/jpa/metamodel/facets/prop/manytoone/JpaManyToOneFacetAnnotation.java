package org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public class JpaManyToOneFacetAnnotation extends FacetAbstract implements
        JpaManyToOneFacet {

    public static Class<? extends Facet> type() {
        return JpaManyToOneFacet.class;
    }

    public JpaManyToOneFacetAnnotation(final FacetHolder holder) {
        super(JpaManyToOneFacetAnnotation.type(), holder, false);
    }

}
