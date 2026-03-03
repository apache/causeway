package org.apache.causeway.core.metamodel.facetapi;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.base._Strings;

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

    record Key(
            Class<? extends Facet> facetType,
            /**
             * The empty String "" is used for qualified facets, that have an empty qualifier
             */
            @Nullable String qualifier) {

        static Key unqualified(final Class<? extends Facet> facetType) {
            return new Key(facetType, null);
        }
        static Key forFacet(final Facet facet) {
            return facet instanceof QualifiedFacet qFacet
                    ? new Key(facet.facetType(), _Strings.nullToEmpty(qFacet.qualifier()))
                    : unqualified(facet.facetType());
        }
        public boolean isQualified() {
            return qualifier!=null;
        }
        public boolean isUnqualified() {
            return qualifier==null;
        }
        public Key toUnqualified() {
            return isUnqualified()
                ? this
                : unqualified(facetType);
        }
        public Key toQualified() {
            return isQualified()
                ? this
                : new Key(facetType, "");
        }
    }

    @Nullable String qualifier();

}
