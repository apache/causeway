package org.apache.isis.viewer.wicket.model.hints;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class UiHintsSetEvent
{
    private final AjaxRequestTarget target;
    
    public UiHintsSetEvent(AjaxRequestTarget target) {
        this.target = target;
    }

    public AjaxRequestTarget getTarget() {
        return target;
    }
}

