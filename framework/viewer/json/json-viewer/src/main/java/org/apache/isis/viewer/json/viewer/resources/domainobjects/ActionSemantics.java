package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.facets.actions.idempotent.IdempotentFacet;
import org.apache.isis.core.metamodel.facets.actions.queryonly.QueryOnlyFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.json.applib.util.Enums;
import org.apache.isis.viewer.json.viewer.ResourceContext;

public enum ActionSemantics {

    QUERY_ONLY("invokeQueryOnly"),
    IDEMPOTENT("invokeIdempotent"),
    SIDE_EFFECTS("invoke");
    
    private final String invokeKey;
    private final String name;
    private ActionSemantics(String invokeKey) {
        this.invokeKey = invokeKey;
        this.name = Enums.enumToCamelCase(this);
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

    public String getName() {
        return name;
    }
    
    public static ActionSemantics determine(ResourceContext resourceContext, ObjectAction action) {
        if(action.containsFacet(QueryOnlyFacet.class)) {
            return QUERY_ONLY;
        }
        if(action.containsFacet(IdempotentFacet.class)) {
            return IDEMPOTENT;
        }
        return SIDE_EFFECTS;
    }

    
}
