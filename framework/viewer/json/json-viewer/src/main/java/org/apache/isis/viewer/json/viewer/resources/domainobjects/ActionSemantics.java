package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.json.viewer.ResourceContext;

public enum ActionSemantics {

    QUERY_ONLY("invokeQueryOnly"),
    IDEMPOTENT("invokeQueryOnly"),
    SIDE_EFFECTS("invoke");
    
    private final String invokeKey;
    private ActionSemantics(String invokeKey) {
        this.invokeKey = invokeKey;
        
    }
    
    public String getInvokeKey() {
        return invokeKey;
    }

    public boolean isQueryOnly() {
        return this == QUERY_ONLY;
    }

    public boolean isIdempotent() {
        return this == IDEMPOTENT;
    }

    public static ActionSemantics determine(ResourceContext resourceContext, ObjectAction action) {
        // TODO
        return ActionSemantics.SIDE_EFFECTS;
    }

    
}
