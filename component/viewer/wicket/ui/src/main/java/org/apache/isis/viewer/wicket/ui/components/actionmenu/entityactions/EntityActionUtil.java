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
package org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions;

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
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLinkFactory;

public final class EntityActionUtil {

    private EntityActionUtil(){}

    private final static MemberOrderFacetComparator memberOrderFacetComparator = new MemberOrderFacetComparator(false);

    public static List<LinkAndLabel> getEntityActionLinksForAssociation(
            final ScalarModel scalarModel,
            final DeploymentType deploymentType) {
        final List<LinkAndLabel> entityActions = Lists.newArrayList();

        if (scalarModel.getKind() != ScalarModel.Kind.PROPERTY) {
            return entityActions;
        } else {
            final ObjectAdapterMemento parentMemento = scalarModel.getParentObjectAdapterMemento();
            final EntityModel parentEntityModel = new EntityModel(parentMemento);
            final OneToOneAssociation oneToOneAssociation = scalarModel.getPropertyMemento().getProperty();

            final List<ObjectAction> associatedActions = getObjectActionsForAssociation(parentEntityModel, oneToOneAssociation, deploymentType);

            entityActions.addAll(asLinkAndLabelsForAdditionalLinksPanel(parentEntityModel, associatedActions));
            return entityActions;
        }
    }

    public static List<ObjectAction> getObjectActionsForAssociation(
            final EntityModel entityModel,
            final ObjectAssociation association,
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
            }
        });
        return associatedActions;
    }

    /**
     * Converts an {@link org.apache.isis.viewer.wicket.model.models.EntityModel} and a (subset of its) {@link org.apache.isis.core.metamodel.spec.feature.ObjectAction}s into a
     * list of {@link org.apache.isis.viewer.wicket.model.links.LinkAndLabel}s intended to be apassed
     * to the {@link AdditionalLinksPanel}.
     */
    public static List<LinkAndLabel> asLinkAndLabelsForAdditionalLinksPanel(
            final EntityModel entityModel,
            final List<ObjectAction> actions) {

        final String linkId = AdditionalLinksPanel.ID_ADDITIONAL_LINK;
        final ActionLinkFactory linkFactory = new EntityActionLinkFactory(entityModel);

        final ObjectAdapterMemento adapterMemento = entityModel.getObjectAdapterMemento();
        return Lists.transform(actions, new Function<ObjectAction, LinkAndLabel>() {

            @Override
            public LinkAndLabel apply(ObjectAction objectAction) {
                return linkFactory.newLink(adapterMemento, objectAction, linkId);
            }
        });
    }

    private static List<ObjectAction> addActions(
            final ActionType type,
            final EntityModel entityModel,
            final ObjectAssociation association,
            final List<ObjectAction> associatedActions) {
        final ObjectSpecification adapterSpec = entityModel.getTypeOfSpecification();
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

        final List<ObjectAction> userActions = adapterSpec.getObjectActions(type, Contributed.INCLUDED, filter);
        associatedActions.addAll(userActions);
        return userActions;
    }


    public static void addTopLevelActions(
            final ObjectAdapter adapter,
            final ActionType actionType,
            final List<ObjectAction> topLevelActions,
            final AuthenticationSession authenticationSession) {

        final ObjectSpecification adapterSpec = adapter.getSpecification();

        @SuppressWarnings({ "unchecked", "deprecation" })
        Filter<ObjectAction> filter = Filters.and(
                ObjectAction.Filters.memberOrderNotAssociationOf(adapterSpec),
                ObjectAction.Filters.dynamicallyVisible(authenticationSession, adapter, Where.ANYWHERE),
                ObjectAction.Filters.notBulkOnly(),
                ObjectAction.Filters.excludeWizardActions(adapterSpec));

        final List<ObjectAction> userActions = adapterSpec.getObjectActions(actionType, Contributed.INCLUDED, filter);
        topLevelActions.addAll(userActions);
    }

    public static List<ObjectAction> getTopLevelActions(final ObjectAdapter adapter, final DeploymentType deploymentType, final AuthenticationSession authenticationSession) {
        final List<ObjectAction> topLevelActions = Lists.newArrayList();

        addTopLevelActions(adapter, ActionType.USER, topLevelActions, authenticationSession);
        if(deploymentType.isPrototyping()) {
            addTopLevelActions(adapter, ActionType.EXPLORATION, topLevelActions, authenticationSession);
            addTopLevelActions(adapter, ActionType.PROTOTYPE, topLevelActions, authenticationSession);
        }
        return topLevelActions;
    }
}
