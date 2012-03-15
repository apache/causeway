package org.apache.isis.core.progmodel.facets.object.objecttype;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.isis.core.metamodel.facets.SingleStringValueFacet;


/**
 * Corresponds to annotating the class with the {@link DiscriminatorValue}
 * annotation.
 * <p>
 * Maps onto the information in {@link Entity} as follows:
 * <ul>
 * <li>{@link DiscriminatorValue#value()} ->
 * {@link ObjectTypeFacet#value()}</li>
 * </ul>
 */
public interface ObjectTypeFacet extends SingleStringValueFacet {


}
