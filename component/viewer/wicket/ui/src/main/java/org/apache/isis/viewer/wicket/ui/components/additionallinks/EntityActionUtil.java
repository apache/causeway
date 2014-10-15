/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.additionallinks;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.google.common.base.Function;
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
import org.apache.isis.core.metamodel.layout.memberorderfacet.MemberOrderFacetComparator;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.ActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.selector.links.LinksSelectorPanelAbstract;

public final class EntityActionUtil {
    
    private EntityActionUtil(){}

    private final static MemberOrderFacetComparator memberOrderFacetComparator = new MemberOrderFacetComparator(false);
    
    public static List<LinkAndLabel> entityActionsForAssociation(
            final EntityModel entityModel,
            final ObjectAssociation association,
            final ActionPromptProvider actionPromptProvider,
            final DeploymentType deploymentType) {
        
        final List<ObjectAction> associatedActions = Lists.newArrayList();

        addActions(ActionType.USER, entityModel, association, associatedActions);
        if(deploymentType.isPrototyping()) {
            addActions(ActionType.EXPLORATION, entityModel, association, associatedActions);
            addActions(ActionType.PROTOTYPE, entityModel, association, associatedActions);
        }
        
        Collections.sort(associatedActions, new Comparator<ObjectAction>() {

            @Override
            public int compare(ObjectAction o1, ObjectAction o2) {
                final MemberOrderFacet m1 = o1.getFacet(MemberOrderFacet.class);
                final MemberOrderFacet m2 = o2.getFacet(MemberOrderFacet.class);
                return memberOrderFacetComparator.compare(m1, m2);
            }});
        
        final ActionLinkFactory linkFactory = new EntityActionLinkFactory(entityModel);
    
        final ObjectAdapterMemento adapterMemento = entityModel.getObjectAdapterMemento();
        return Lists.transform(associatedActions, new Function<ObjectAction, LinkAndLabel>(){
    
            @Override
            public LinkAndLabel apply(ObjectAction objectAction) {
                return linkFactory.newLink(adapterMemento, objectAction, LinksSelectorPanelAbstract.ID_ADDITIONAL_LINK, actionPromptProvider);
            }});
    }

    private static List<ObjectAction> addActions(
            final ActionType type,
            final EntityModel entityModel,
            final ObjectAssociation association,
            final List<ObjectAction> associatedActions) {
        final ObjectAdapter adapter = entityModel.load(ConcurrencyChecking.NO_CHECK);

        final AuthenticationSessionProvider asa = (AuthenticationSessionProvider) Session.get();
        AuthenticationSession authSession = asa.getAuthenticationSession();

        final ObjectSpecification objectSpecification = entityModel.getTypeOfSpecification();
        @SuppressWarnings({ "unchecked", "deprecation" })
        Filter<ObjectAction> filter = Filters.and(
                    ObjectAction.Filters.memberOrderOf(association),
                    ObjectAction.Filters.dynamicallyVisible(authSession, adapter, Where.ANYWHERE),
                    ObjectAction.Filters.notBulkOnly(),
                    ObjectAction.Filters.excludeWizardActions(objectSpecification));

        final List<ObjectAction> userActions = objectSpecification.getObjectActions(type, Contributed.INCLUDED, filter);
        associatedActions.addAll(userActions);
        return userActions;
    }


}
