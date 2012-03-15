package org.apache.isis.extensions.jpa.metamodel.facets.prop.onetoone;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.apache.isis.core.metamodel.facets.MarkerFacet;


/**
 * Corresponds to the property with the {@link ManyToOne} annotation.
 * <p>
 * Maps onto the information in {@link ManyToOne} as follows:
 * <ul>
 * <li>{@link OneToOne#targetEntity()} -> (no corresponding attribute or facet)</li>
 * <li>{@link OneToOne#cascade()} -> (no corresponding attribute or facet)</li>
 * <li>{@link OneToOne#fetch()} ->
 * {@link JpaFetchTypeFacetDerivedFromJpaOneToOneAnnotation}</li>
 * <li>{@link OneToOne#optional()} ->
 * {@link MandatoryFacetDerivedFromJpaOneToOneAnnotation} or
 * {@link OptionalFacetDerivedFromJpaOneToOneAnnotation}</li>
 * <li>{@link OneToOne#mappedBy()} -> (no corresponding attribute or facet)</li>
 * </ul>
 */
public interface JpaOneToOneFacet extends MarkerFacet {


}
