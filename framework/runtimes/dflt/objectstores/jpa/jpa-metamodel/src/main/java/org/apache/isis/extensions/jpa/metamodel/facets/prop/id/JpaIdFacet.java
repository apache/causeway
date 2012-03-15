package org.apache.isis.extensions.jpa.metamodel.facets.prop.id;

import javax.persistence.Id;

import org.apache.isis.core.metamodel.facets.MarkerFacet;


/**
 * Corresponds to the property with the {@link Id} annotation.
 * <p>
 * The JPA {@link Id} annotation has no attributes, and no other NOF metadata is
 * derived from it. Internally the JPA object store does use the <tt>Id</tt> to
 * create the {@link Oid}.
 */
public interface JpaIdFacet extends MarkerFacet {

}
