package org.apache.isis.core.metamodel.facets.all.i8n;

import javax.annotation.Nullable;

import org.apache.isis.commons.collections.ImmutableEnumSet;

public interface HasTranslation {

    /**
     * Originating text of preferred NounForm to be translated before use in the UI.
     */
    String preferredText();

    /**
     * Translated text of preferred NounForm to be used in the UI.
     */
    String preferredTranslated();

    /**
     * Originating text to be translated before use in the UI.
     */
    String text(NounForm nounForm);

    /**
     * Translated text to be used in the UI.
     */
    String translated(NounForm nounForm);

    ImmutableEnumSet<NounForm> getSupportedNounForms();

    @Nullable
    default String translatedElseNull(final NounForm nounForm) {
        return getSupportedNounForms().contains(nounForm)
                ? translated(nounForm)
                : null;
    }

    default void memoizeTranslations() {
        getSupportedNounForms().forEach(this::translated);
    }

}
