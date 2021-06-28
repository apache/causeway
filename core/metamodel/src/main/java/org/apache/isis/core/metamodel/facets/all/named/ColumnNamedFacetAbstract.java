package org.apache.isis.core.metamodel.facets.all.named;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.i8n.staatic.HasStaticTextFacetAbstract;

/**
 * The base for the {@link ColumnNamedFacet}.
 *
 * @see ColumnNamedFacet
 * @since 2.0
 */
public abstract class ColumnNamedFacetAbstract
extends HasStaticTextFacetAbstract
implements ColumnNamedFacet {

    private static final Class<? extends Facet> type() {
        return ColumnNamedFacet.class;
    }

    protected ColumnNamedFacetAbstract(
            final String originalText,
            final FacetHolder holder) {
        this(
                originalText,
                holder,
                Precedence.DEFAULT);
    }

    protected ColumnNamedFacetAbstract(
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
