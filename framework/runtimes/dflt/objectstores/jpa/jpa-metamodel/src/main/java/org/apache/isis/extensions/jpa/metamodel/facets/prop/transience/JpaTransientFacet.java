package org.apache.isis.extensions.jpa.metamodel.facets.prop.transience;

import javax.persistence.Transient;

import org.apache.isis.core.metamodel.facets.MarkerFacet;

/**
 * Corresponds to annotating the class with {@link Transient}.
 * <p>
 * The JPA {@link Transient} annotation has no attributes. However, in addition
 * to this facet it does also implicitly map to
 * {@link DerivedFacetDerivedFromJpaTransientAnnotation}.
 */
public interface JpaTransientFacet extends MarkerFacet {

}
