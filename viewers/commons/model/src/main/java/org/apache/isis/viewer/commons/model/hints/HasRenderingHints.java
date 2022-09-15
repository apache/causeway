package org.apache.isis.viewer.commons.model.hints;

import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.viewer.commons.model.hints.RenderingHint;

public interface HasRenderingHints {

    /**
     * @apiNote Similar to {@code #mustBeEditable()}, though not called from the same locations.
     * My suspicion is that it amounts to more or less the same set of conditions.
     */
    boolean isInlinePrompt();

    RenderingHint getRenderingHint();
    @Deprecated// make immutable? - need to recreate any bound UI components anyway
    void setRenderingHint(RenderingHint renderingHint);

    ScalarRepresentation getMode();
    @Deprecated// make immutable? - need to recreate any bound UI components anyway
    void setMode(ScalarRepresentation mode);

    // -- SHORTCUTS

    default boolean isViewMode() {
        return getMode() == ScalarRepresentation.VIEWING;
    }

    default boolean isEditMode() {
        return getMode() == ScalarRepresentation.EDITING;
    }

    default HasRenderingHints toEditMode() {
        setMode(ScalarRepresentation.EDITING);
        return this;
    }

    default HasRenderingHints toViewMode() {
        setMode(ScalarRepresentation.VIEWING);
        return this;
    }

}