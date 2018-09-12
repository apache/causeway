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

package org.apache.isis.viewer.wicket.ui.pages.home;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.actions.homepage.HomePageFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionFormExecutorStrategy;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModelProvider;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.isis.viewer.wicket.ui.panels.FormExecutorDefault;
import org.apache.isis.viewer.wicket.ui.util.Components;

/**
 * Web page representing the home page (showing a welcome message).
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class HomePage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    public HomePage(final PageParameters parameters) {
        super(parameters, null);

        addChildComponents(themeDiv, null);
        buildGui();

        addBookmarkedPages(themeDiv);
    }

    private void buildGui() {
        final ObjectAndAction objectAndAction = lookupHomePageAction();
        if(objectAndAction != null) {
            Components.permanentlyHide(themeDiv, ComponentType.WELCOME);
            final ActionModel actionModel = ActionModel.create(new EntityModel(objectAndAction.objectAdapter), objectAndAction.action);
            final FormExecutorDefault<ActionModel> formExecutor =
                    new FormExecutorDefault<>( new ActionFormExecutorStrategy(actionModel));
            formExecutor.executeAndProcessResults(getPage(), null, null, actionModel.isWithinPrompt());
        } else {
            Components.permanentlyHide(themeDiv, ComponentType.ACTION_PROMPT);
            getComponentFactoryRegistry().addOrReplaceComponent(themeDiv, ComponentType.WELCOME, null);
        }

        final BreadcrumbModelProvider session = (BreadcrumbModelProvider) getSession();
        final BreadcrumbModel breadcrumbModel = session.getBreadcrumbModel();
        breadcrumbModel.visitedHomePage();
    }

    private static class ObjectAndAction {
        ObjectAndAction(final ObjectAdapter serviceAdapter, final ObjectAction objectAction) {
            this.objectAdapter = serviceAdapter;
            action = objectAction;
        }
        ObjectAdapter objectAdapter;
        ObjectAction action;
    }

    private ObjectAndAction lookupHomePageAction() {
        final List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();
        for (final ObjectAdapter serviceAdapter : serviceAdapters) {
            final ObjectSpecification serviceSpec = serviceAdapter.getSpecification();
            final Stream<ObjectAction> objectActions = serviceSpec.streamObjectActions(Contributed.EXCLUDED);
            
            final Optional<ObjectAndAction> homePageAction = objectActions
            .map(objectAction->objectAndActionIfHomePageAndUsable(serviceAdapter, objectAction))
            .filter(_NullSafe::isPresent)
            .findAny();
            
            if(homePageAction.isPresent()) {
                return homePageAction.get();
            }

        }
        return null;
    }

    private ObjectAndAction objectAndActionIfHomePageAndUsable(ObjectAdapter serviceAdapter, ObjectAction objectAction) {
        if (!objectAction.containsDoOpFacet(HomePageFacet.class)) {
            return null;
        }

        final Consent visibility =
                objectAction.isVisible(
                        serviceAdapter,
                        InteractionInitiatedBy.USER,
                        Where.ANYWHERE);
        if (visibility.isVetoed()) {
            return null;
        }

        final Consent usability =
                objectAction.isUsable(
                        serviceAdapter,
                        InteractionInitiatedBy.USER,
                        Where.ANYWHERE
                        );
        if (usability.isVetoed()) {
            return  null;
        }

        return new ObjectAndAction(serviceAdapter, objectAction);
    }

}
