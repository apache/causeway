package org.apache.isis.extensions.jpa.metamodel.facets.prop.basic;

import javax.persistence.Basic;

import org.apache.isis.core.metamodel.facets.MarkerFacet;


/**
 * Corresponds to the property with the {@link Basic} annotation.
 * <p>
 * Maps onto the information in {@link Basic} as follows:
 * <ul>
 * <li>{@link Basic#fetch()} ->
 * {@link JpaFetchTypeFacetDerivedFromJpaBasicAnnotation}</li>
 * <li>{@link Basic#optional()} ->
 * {@link OptionalFacetDerivedFromJpaBasicAnnotation} or
 * {@link MandatoryFacetDerivedFromJpaBasicAnnotation}</li>
 * </ul>
 */
public interface JpaBasicFacet extends MarkerFacet {

}
