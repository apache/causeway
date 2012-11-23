package org.apache.isis.core.metamodel.spec.feature;

import java.util.List;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetFilters;
import org.apache.isis.core.metamodel.interactions.DisablingInteractionAdvisor;
import org.apache.isis.core.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.isis.core.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

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
            }
        };
    }

    public static Filter<ObjectAction> withNoBusinessRules() {
        return new Filter<ObjectAction>(){
            @Override
            public boolean accept(final ObjectAction objectAction) {
                final List<Facet> hidingFacets = objectAction.getFacets(FacetFilters.isA(HidingInteractionAdvisor.class));
                final List<Facet> disablingFacets = objectAction.getFacets(FacetFilters.isA(DisablingInteractionAdvisor.class));
                final List<Facet> validatingFacets = objectAction.getFacets(FacetFilters.isA(ValidatingInteractionAdvisor.class));
                return hidingFacets.isEmpty() && disablingFacets.isEmpty() && validatingFacets.isEmpty();
            }};
    }

    public static Filter<ObjectAction> contributedAnd1ParamAndVoid() {
        return new Filter<ObjectAction>(){
            @Override
            public boolean accept(final ObjectAction objectAction) {
                boolean contributed = objectAction.isContributed();
                boolean has1Param = objectAction.getParameterCount() == 1;
                boolean hasReturn = objectAction.hasReturn();
                return contributed && has1Param && !hasReturn;
            }
        };
    }

    public static Filter<ObjectAction> filterOfType(final ActionType type) {
        return new Filter<ObjectAction>(){
            @Override
            public boolean accept(ObjectAction oa) {
                return oa.getType() == type;
            }
        };
    }
}
