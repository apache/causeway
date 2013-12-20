package org.apache.isis.viewer.wicket.model.hints;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.isis.viewer.wicket.model.models.EntityModel;

public class UiHintsBroadcastEvent extends UiHintsEventAbstract {
    
    public UiHintsBroadcastEvent(UiHintsEventAbstract ev) {
        super(ev.getUiHintContainer(), ev.getTarget());
    }
    
}

