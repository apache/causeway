package org.apache.isis.core.metamodel.facets.all.i8n;

public interface HasTranslation {

    /**
     * Originating text to be translated before use in the UI.
     */
    String text();

    /**
     * Translated text to be used in the UI.
     */
    String translated();

}
