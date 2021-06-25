package org.apache.isis.core.metamodel.facets.all.named;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.i8n.imperative.HasImperativeText;
import org.apache.isis.core.metamodel.facets.all.i8n.staatic.HasStaticText;
import org.apache.isis.core.metamodel.facets.all.i8n.staatic.HasStaticTextFacetAbstract;

import lombok.Getter;

/**
 * One of two bases for the {@link MemberNamedFacet}.
 *
 * @see MemberNamedFacetWithImperativeTextAbstract
 * @since 2.0
 */
public abstract class MemberNamedFacetWithStaticTextAbstract
extends HasStaticTextFacetAbstract
implements MemberNamedFacet {

    private static final Class<? extends Facet> type() {
        return MemberNamedFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private final _Either<HasStaticText, HasImperativeText> specialization = _Either.left(this);

    protected MemberNamedFacetWithStaticTextAbstract(
            final String originalText,
            final FacetHolder holder) {
        this(
                originalText,
                holder,
                Precedence.DEFAULT);
    }

    protected MemberNamedFacetWithStaticTextAbstract(
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
