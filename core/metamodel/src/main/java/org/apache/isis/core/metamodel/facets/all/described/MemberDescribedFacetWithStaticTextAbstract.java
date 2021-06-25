package org.apache.isis.core.metamodel.facets.all.described;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.i8n.imperative.HasImperativeText;
import org.apache.isis.core.metamodel.facets.all.i8n.staatic.HasStaticText;
import org.apache.isis.core.metamodel.facets.all.i8n.staatic.HasStaticTextFacetAbstract;

import lombok.Getter;

/**
 * One of two bases for the {@link MemberDescribedFacet}.
 *
 * @see MemberDescribedFacetWithImperativeTextAbstract
 * @since 2.0
 */
public abstract class MemberDescribedFacetWithStaticTextAbstract
extends HasStaticTextFacetAbstract
implements MemberDescribedFacet {

    private static final Class<? extends Facet> type() {
        return MemberDescribedFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private final _Either<HasStaticText, HasImperativeText> specialization = _Either.left(this);

    protected MemberDescribedFacetWithStaticTextAbstract(
            final String originalText,
            final FacetHolder holder) {
        this(
                originalText,
                holder,
                Precedence.DEFAULT);
    }

    protected MemberDescribedFacetWithStaticTextAbstract(
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
