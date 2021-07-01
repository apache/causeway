package org.apache.isis.core.metamodel.facets.all.named;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.i8n.staatic.HasStaticTextFacetAbstract;

/**
 * The base for the {@link CanonicalNamedFacet}.
 *
 * @see CanonicalNamedFacet
 * @since 2.0
 */
public abstract class CanonicalNamedFacetAbstract
extends HasStaticTextFacetAbstract
implements CanonicalNamedFacet {

    private static final Class<? extends Facet> type() {
        return CanonicalNamedFacet.class;
    }

    protected CanonicalNamedFacetAbstract(
            final String originalText,
            final FacetHolder holder) {
        this(
                originalText,
                holder,
                Precedence.DEFAULT);
    }

    protected CanonicalNamedFacetAbstract(
            final String originalText,
            final FacetHolder holder,
            final Precedence precedence) {
        super(type(),
                TranslationContext.forTranslationContextHolder(holder.getFeatureIdentifier()),
                originalText,
                holder,
                precedence);
    }

}
