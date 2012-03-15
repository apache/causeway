package org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone;

import javax.persistence.ManyToOne;

import org.apache.isis.core.metamodel.facets.MarkerFacet;


/**
 * Corresponds to the property with the {@link ManyToOne} annotation.
 * <p>
 * Maps onto the information in {@link ManyToOne} as follows:
 * <ul>
 * <li>{@link ManyToOne#targetEntity()} -> (no corresponding attribute or facet)
 * </li>
 * <li>{@link ManyToOne#cascade()} -> (no corresponding attribute or facet)</li>
 * <li>{@link ManyToOne#fetch()} ->
 * {@link JpaFetchTypeFacetDerivedFromJpaManyToOneAnnotation}</li>
 * <li>{@link ManyToOne#optional()} ->
 * {@link MandatoryFacetDerivedFromJpaManyToOneAnnotation} or
 * {@link MandatoryFacetDerivedFromJpaManyToOneAnnotation}</li>
 * </ul>
 */
public interface JpaManyToOneFacet extends MarkerFacet {

}
