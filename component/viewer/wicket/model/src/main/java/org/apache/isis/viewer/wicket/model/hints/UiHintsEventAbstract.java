package org.apache.isis.viewer.wicket.model.hints;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.isis.viewer.wicket.model.models.EntityModel;

public abstract class UiHintsEventAbstract {
    
    private final UiHintContainer uiHintContainer;
    private final AjaxRequestTarget target;
    
    public UiHintsEventAbstract(UiHintContainer uiHintContainer, AjaxRequestTarget target) {
        this.uiHintContainer = uiHintContainer;
        this.target = target;
    }

    public UiHintContainer getUiHintContainer() {
        return uiHintContainer;
    }
    /**
     * The {@link AjaxRequestTarget target}, if any, that caused this event to be generated.
     * 
     * <p>
     * Typically populated, but not always...
     */
    public AjaxRequestTarget getTarget() {
        return target;
    }
}

