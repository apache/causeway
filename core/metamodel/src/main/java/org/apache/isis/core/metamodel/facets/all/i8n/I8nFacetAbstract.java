package org.apache.isis.core.metamodel.facets.all.i8n;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.NonNull;
import lombok.val;

public abstract class I8nFacetAbstract
extends FacetAbstract
implements HasTranslation {

    private final _Lazy<String> translated;
    private final String originalText;
    private final TranslationContext translationContext;

    protected I8nFacetAbstract(
            final Class<? extends Facet> facetType,
            final String originalText,
            final FacetHolder holder) {
        this(facetType, originalText, holder, Precedence.DEFAULT);
    }

    protected I8nFacetAbstract(
            final Class<? extends Facet> facetType,
            final String originalText,
            final FacetHolder holder,
            final Precedence precedence) {
        super(facetType, holder, precedence);
        this.originalText = originalText;
        this.translationContext = TranslationContext
                .forTranslationContextHolder(holder.getFeatureIdentifier());
        this.translated = _Lazy.threadSafe(()->originalText);
                //holder.getTranslationService().translate(translationContext, originalText));
    }

    @Override
    public String text() {
        return originalText;
    }

    @Override
    public String translated() {
        return translated.get();
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("context", translationContext);
        visitor.accept("originalText", text());
        visitor.accept("translated", translated()); // memoizes as a side-effect
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        if(! (other instanceof I8nFacetAbstract)) {
            return false;
        }

        val otherFacet =  (I8nFacetAbstract)other;

        return Objects.equals(this.text(), otherFacet.text())
                && Objects.equals(this.translationContext, otherFacet.translationContext);
    }

}
