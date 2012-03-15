package org.apache.isis.extensions.jpa.metamodel.facets.object.embeddable;

import javax.persistence.Embeddable;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.aggregated.AggregatedFacetAbstract;


/**
 * Derived from being {@link Embeddable}.
 */
public class AggregatedFacetDerivedFromJpaEmbeddableAnnotation extends
        AggregatedFacetAbstract {

    public AggregatedFacetDerivedFromJpaEmbeddableAnnotation(
            final FacetHolder holder) {
        super(holder);
    }

}
