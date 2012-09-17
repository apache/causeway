package org.apache.isis.core.metamodel.spec.feature;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;

public class ObjectActionFilters {

    public static Filter<ObjectAction> dynamicallyVisible(final AuthenticationSession session, final ObjectAdapter target, final Where where) {
        return new Filter<ObjectAction>() {
            @Override
            public boolean accept(final ObjectAction objectAction) {
                final Consent visible = objectAction.isVisible(session, target, where);
                return visible.isAllowed();
            }
        };
    }

    public static Filter<ObjectAction> withId(final String actionId) {
        return new Filter<ObjectAction>(){
    
            @Override
            public boolean accept(ObjectAction objectAction) {
                return objectAction.getId().equals(actionId);
            }};
    }

}
