package org.apache.isis.viewer.wicket.model.hints;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class UiHintsBroadcastEvent
{
    private final AjaxRequestTarget target;
    
    public UiHintsBroadcastEvent(AjaxRequestTarget target) {
        this.target = target;
    }

    public AjaxRequestTarget getTarget() {
        return target;
    }
}

