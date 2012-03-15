package org.apache.isis.extensions.jpa.metamodel.facets.object.embeddable;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;


public class JpaEmbeddableFacetAnnotation extends FacetAbstract implements
        JpaEmbeddableFacet {

    public static Class<? extends Facet> type() {
        return JpaEmbeddableFacet.class;
    }

    public JpaEmbeddableFacetAnnotation(final FacetHolder holder) {
        super(JpaEmbeddableFacetAnnotation.type(), holder, false);
    }

}
