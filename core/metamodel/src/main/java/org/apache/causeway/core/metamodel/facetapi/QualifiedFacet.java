package org.apache.causeway.core.metamodel.facetapi;

import org.jspecify.annotations.Nullable;

/**
 * A {@link Facet} can be qualified (similar to Spring beans) in order to allow for alternative
 * behavior or semantics based on context.
 *
 * <p>Introduced for layout related facets, as layouts are potentially qualified to specify a layout variant.
 *
 * <p>This design adds an additional discriminator, such that facets of same type and precedence
 * can be collected into a single {@link FacetRanking}'s rank and looked up later using the qualifier as a criterion
 * to find the winning {@link Facet}.
 *
 * @implNote We have only one use-case yet, which is honoring layout variants.
 * Should other use-cases emerge, this interface will need a redesign.
 *
 * @since 4.0
 */
@FunctionalInterface
public interface QualifiedFacet {

    @Nullable String qualifier();

}
