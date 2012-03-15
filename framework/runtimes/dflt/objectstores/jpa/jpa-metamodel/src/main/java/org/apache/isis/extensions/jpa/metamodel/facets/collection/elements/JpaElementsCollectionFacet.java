package org.apache.isis.extensions.jpa.metamodel.facets.collection.elements;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.extensions.jpa.metamodel.facets.JpaFetchTypeFacet;


/**
 * Corresponds to the {@link CollectionOfElements} annotation.
 * <p>
 * Not all the information from the annotation is stored here. Specifically:
 * <ul>
 * <li>{@link CollectionOfElements#targetElement()} ->
 * {@link TypeOfFacetDerivedFromHibernateCollectionOfElementsAnnotation}</li>
 * <li>{@link CollectionOfElements#fetch()} ->
 * {@link JpaFetchTypeFacet#getFetchType()}</li>
 * </ul>
 */
public interface JpaElementsCollectionFacet extends Facet {

}
