package org.apache.isis.extensions.jpa.metamodel.facets.prop.version;

import javax.persistence.Version;

import org.apache.isis.core.metamodel.facets.MarkerFacet;

/**
 * Corresponds to annotating the class with {@link Version}.
 * <p>
 * The JPA {@link Version} annotation has no attributes, and there are no other
 * facets that are derived from its existence.
 */
public interface JpaVersionFacet extends MarkerFacet {

}
