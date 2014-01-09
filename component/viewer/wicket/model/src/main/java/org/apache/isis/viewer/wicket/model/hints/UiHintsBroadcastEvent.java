package org.apache.isis.viewer.wicket.model.hints;


public class UiHintsBroadcastEvent extends UiHintsEventAbstract {
    
    public UiHintsBroadcastEvent(UiHintsEventAbstract ev) {
        super(ev.getUiHintContainer(), ev.getTarget());
    }
    public UiHintsBroadcastEvent(UiHintContainer uiHintContainer) {
        super(uiHintContainer, null);
    }
    
}

