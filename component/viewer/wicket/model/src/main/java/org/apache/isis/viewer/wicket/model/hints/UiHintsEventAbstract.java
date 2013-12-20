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
    public AjaxRequestTarget getTarget() {
        return target;
    }
}

