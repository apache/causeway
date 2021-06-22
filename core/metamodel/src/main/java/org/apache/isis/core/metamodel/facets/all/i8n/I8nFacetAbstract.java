package org.apache.isis.core.metamodel.facets.all.i8n;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public abstract class I8nFacetAbstract
extends FacetAbstract
implements HasTranslation {

    protected final TranslationContext translationContext;
    @Getter(onMethod_ = {@Override}) private final ImmutableEnumSet<NounForm> supportedNounForms;

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
        this.supportedNounForms = ImmutableEnumSet.from(nounForms.getSupportedNounForms());
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
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("context", translationContext);
        visitor.accept("supportedNounForms", supportedNounForms);
        supportedNounForms.forEach(nounForm->{
            visitor.accept("originalText." + nounForm, text(nounForm));
            visitor.accept("translated." + nounForm, translated(nounForm)); // memoizes as a side-effect
        });
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {

        // equality by facet-type, text and context

        if(!this.facetType().equals(other.facetType())) {
            return false;
        }

        val otherFacet =  (I8nFacetAbstract)other;

        if(!Objects.equals(this.supportedNounForms, otherFacet.supportedNounForms)
                || !Objects.equals(this.translationContext, otherFacet.translationContext)) {
            return false;
        }

        for(val nounForm : supportedNounForms) {
            if(!Objects.equals(this.text(nounForm), otherFacet.text(nounForm))){
                return false;
            }
        }

        return true;

    }

}
