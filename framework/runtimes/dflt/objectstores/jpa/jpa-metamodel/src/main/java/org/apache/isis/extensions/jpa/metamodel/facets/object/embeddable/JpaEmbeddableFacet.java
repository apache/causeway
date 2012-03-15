package org.apache.isis.extensions.jpa.metamodel.facets.object.embeddable;

import javax.persistence.Embeddable;

import org.apache.isis.core.metamodel.facets.MarkerFacet;

/**
 * Corresponds to annotating the class with {@link Embeddable}.
 * <p>
 * The JPA {@link Embeddable} annotation has no attributes. However, in addition
 * to this facet it does also implicitly map to
 * {@link AggregatedFacetDerivedFromJpaEmbeddableAnnotation}.
 */
public interface JpaEmbeddableFacet extends MarkerFacet {

}
