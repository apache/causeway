package org.apache.isis.viewer.wicket.model.hints;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.isis.viewer.wicket.model.models.EntityModel;

public class UiHintsSetEvent extends UiHintsEventAbstract {
    
    public UiHintsSetEvent(UiHintContainer uiHintContainer, AjaxRequestTarget target) {
        super(uiHintContainer, target);
    }
    
}

