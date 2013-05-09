package org.apache.isis.viewer.wicket.ui.components.additionallinks;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.apache.wicket.Session;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionFilters;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;
import org.apache.isis.viewer.wicket.ui.selector.links.LinksSelectorPanelAbstract;

public final class EntityActionUtil {
    
    private EntityActionUtil(){}

    public static List<LinkAndLabel> entityActions(EntityModel entityModel, ObjectAssociation association) {
        final ObjectSpecification adapterSpec = entityModel.getTypeOfSpecification();
        final ObjectAdapter adapter = entityModel.load(ConcurrencyChecking.NO_CHECK);
        final ObjectAdapterMemento adapterMemento = entityModel.getObjectAdapterMemento();
        
        @SuppressWarnings("unchecked")
        final List<ObjectAction> userActions = adapterSpec.getObjectActions(ActionType.USER, Contributed.INCLUDED,
                Filters.and(EntityActionUtil.memberOrderOf(association), EntityActionUtil.dynamicallyVisibleFor(adapter)));
        
        final CssMenuLinkFactory linkFactory = new EntityActionLinkFactory(entityModel);
    
        return Lists.transform(userActions, new Function<ObjectAction, LinkAndLabel>(){
    
            @Override
            public LinkAndLabel apply(ObjectAction objectAction) {
                return linkFactory.newLink(adapterMemento, objectAction, LinksSelectorPanelAbstract.ID_ADDITIONAL_LINK);
            }});
    }

    private static Filter<ObjectAction> dynamicallyVisibleFor(final ObjectAdapter adapter) {
        final AuthenticationSessionProvider asa = (AuthenticationSessionProvider) Session.get();
        AuthenticationSession authSession = asa.getAuthenticationSession();
        return ObjectActionFilters.dynamicallyVisible(authSession, adapter, Where.ANYWHERE);
    }

    private static Filter<ObjectAction> memberOrderOf(ObjectAssociation association) {
        final String collectionName = association.getName();
        final String collectionId = association.getId();
        return new Filter<ObjectAction>() {
    
            @Override
            public boolean accept(ObjectAction t) {
                final MemberOrderFacet memberOrderFacet = t.getFacet(MemberOrderFacet.class);
                if(memberOrderFacet == null) {
                    return false; 
                }
                final String memberOrderName = memberOrderFacet.name();
                if(Strings.isNullOrEmpty(memberOrderName)) {
                    return false;
                }
                return memberOrderName.equalsIgnoreCase(collectionName) || memberOrderName.equalsIgnoreCase(collectionId);
            }
        };
    }

}
