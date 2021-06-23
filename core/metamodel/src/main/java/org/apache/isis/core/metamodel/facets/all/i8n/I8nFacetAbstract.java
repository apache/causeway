package org.apache.isis.core.metamodel.facets.all.i8n;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.NonNull;
import lombok.val;

public abstract class I8nFacetAbstract
extends FacetAbstract
implements HasTranslation {

    protected final TranslationContext translationContext;

    private final @NonNull NounForms nounForms;
    private final @NonNull _Lazy<NounForms> translatedNounForms;

    protected I8nFacetAbstract(
            final Class<? extends Facet> facetType,
            final NounForms nounForms,
            final FacetHolder holder) {
        this(facetType, nounForms, holder, Precedence.DEFAULT);
    }

    protected I8nFacetAbstract(
            final Class<? extends Facet> facetType,
            final NounForms nounForms,
            final FacetHolder holder,
            final Precedence precedence) {
        super(facetType, holder, precedence);
        this.nounForms = nounForms;
        this.translationContext = TranslationContext
                .forTranslationContextHolder(holder.getFeatureIdentifier());
        this.translatedNounForms = _Lazy.threadSafe(()->
            nounForms.translate(holder.getTranslationService(), translationContext));
    }

    @Override
    public final String preferredText() {
        return text(nounForms.getPreferredNounForm());
    }

    @Override
    public final String preferredTranslated() {
        return translated(nounForms.getPreferredNounForm());
    }

    @Override
    public final String text(final @NonNull NounForm nounForm) {
        return nounForms.get(nounForm);
    }

    @Override
    public final String translated(final NounForm nounForm) {
        return translatedNounForms.get().get(nounForm);
    }

    @Override
    public ImmutableEnumSet<NounForm> getSupportedNounForms() {
        return nounForms.getSupportedNounForms();
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("context", translationContext);
        visitor.accept("nounForms",
                getSupportedNounForms()
                .stream()
                .map(NounForm::name)
                .collect(Collectors.joining(", ")));

        getSupportedNounForms()
        .forEach(nounForm->{
            visitor.accept("originalText." + nounForm, text(nounForm));
            visitor.accept("translated." + nounForm, translated(nounForm)); // memoizes as a side-effect
        });
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {

        // equality by facet-type, (original) text and translation-context

        if(!this.facetType().equals(other.facetType())) {
            return false;
        }

        val otherFacet =  (I8nFacetAbstract)other;

        return Objects.equals(this.nounForms, otherFacet.nounForms)
                && Objects.equals(this.translationContext, otherFacet.translationContext);

    }

}
