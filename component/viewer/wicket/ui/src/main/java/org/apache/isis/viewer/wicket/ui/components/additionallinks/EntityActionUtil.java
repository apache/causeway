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
import org.apache.isis.core.metamodel.spec.feature.ObjectActionFilters;
import org.apache.isis.core.metamodel.spec.feature.ObjectActions;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;
import org.apache.isis.viewer.wicket.ui.selector.links.LinksSelectorPanelAbstract;

public final class EntityActionUtil {
    
    private EntityActionUtil(){}

    private final static MemberOrderFacetComparator memberOrderFacetComparator = new MemberOrderFacetComparator(false);
    
    public static List<LinkAndLabel> entityActions(EntityModel entityModel, ObjectAssociation association) {
        final ObjectSpecification adapterSpec = entityModel.getTypeOfSpecification();
        final ObjectAdapter adapter = entityModel.load(ConcurrencyChecking.NO_CHECK);
        final ObjectAdapterMemento adapterMemento = entityModel.getObjectAdapterMemento();
        
        @SuppressWarnings("unchecked")
        final List<ObjectAction> userActions = adapterSpec.getObjectActions(ActionType.USER, Contributed.INCLUDED,
                Filters.and(ObjectActions.memberOrderOf(association), EntityActionUtil.dynamicallyVisibleFor(adapter)));
        Collections.sort(userActions, new Comparator<ObjectAction>() {

            @Override
            public int compare(ObjectAction o1, ObjectAction o2) {
                final MemberOrderFacet m1 = o1.getFacet(MemberOrderFacet.class);
                final MemberOrderFacet m2 = o2.getFacet(MemberOrderFacet.class);
                return memberOrderFacetComparator.compare(m1, m2);
            }});
        
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

}
