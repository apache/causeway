package org.apache.isis.extensions.jpa.metamodel.facets.object.entity;

import javax.persistence.Entity;

import org.apache.isis.core.metamodel.facetapi.Facet;


/**
 * Corresponds to annotating the class with the {@link Entity} annotation.
 * <p>
 * Maps onto the information in {@link Entity} as follows:
 * <ul>
 * <li>{@link Entity#name()} -> {@link JpaEntityFacet#getName()}</li>
 * </ul>
 */
public interface JpaEntityFacet extends Facet {

    String getName();

}
