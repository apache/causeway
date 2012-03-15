package org.apache.isis.extensions.jpa.metamodel.facets.collection.elements;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public class JpaElementsCollectionFacetAnnotation extends FacetAbstract
        implements JpaElementsCollectionFacet {

    public static Class<? extends Facet> type() {
        return JpaElementsCollectionFacet.class;
    }

    public JpaElementsCollectionFacetAnnotation(final FacetHolder holder) {
        super(JpaElementsCollectionFacetAnnotation.type(), holder,
                false);
    }

}
