package org.apache.isis.extensions.jpa.metamodel.facets.prop.joincolumn;

import javax.persistence.JoinColumn;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;

/**
 * Corresponds to a property with the {@link JoinColumn} annotation.
 * <p>
 * Maps onto the information in {@link JoinColumn} as follows:
 * <ul>
 * <li>{@link JoinColumn#name()} -> {@link #name()}. Note: not mapped onto a
 * {@link NamedFacet} subclass because this is a physical name, not a logical
 * name.</li>
 * <li>{@link JoinColumn#referencedColumnName()} -> (no corresponding facet)</li>
 * <li>{@link JoinColumn#unique()} -> (no corresponding facet)</li>
 * <li>{@link JoinColumn#nullable()} ->
 * {@link MandatoryFacetDerivedFromJpaJoinColumnAnnotation} or
 * {@link OptionalFacetDerivedFromJpaJoinColumnAnnotation}</li>
 * <li>{@link JoinColumn#insertable()} -> (no corresponding facet)</li>
 * <li>{@link JoinColumn#updatable()} -> (no corresponding facet)</li>
 * <li>{@link JoinColumn#columnDefinition()} -> (no corresponding facet)</li>
 * <li>{@link JoinColumn#table()} -> (no corresponding facet)</li>
 * </ul>
 */
public interface JpaJoinColumnFacet extends Facet {

    String name();

}
