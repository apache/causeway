package org.apache.isis.core.metamodel.spec.feature;

import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.facets.actions.idempotent.IdempotentFacet;
import org.apache.isis.core.metamodel.facets.actions.queryonly.QueryOnlyFacet;

public enum ActionSemantics {
    QUERY_ONLY, IDEMPOTENT, SIDE_EFFECTS;

    public static ActionSemantics determine(final ObjectAction action) {
        if (action.containsFacet(QueryOnlyFacet.class)) {
            return QUERY_ONLY;
        }
        if (action.containsFacet(IdempotentFacet.class)) {
            return IDEMPOTENT;
        }
        return SIDE_EFFECTS;
    }

    public ConcurrencyChecking getConcurrencyChecking() {
        return this == QUERY_ONLY? ConcurrencyChecking.NO_CHECK: ConcurrencyChecking.CHECK;
    }
}