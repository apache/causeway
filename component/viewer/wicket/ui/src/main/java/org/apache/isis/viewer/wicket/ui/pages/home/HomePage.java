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

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.actions.homepage.HomePageFacet;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionModel.Mode;
import org.apache.isis.viewer.wicket.model.models.ActionModel.SingleResultsMode;
import org.apache.isis.viewer.wicket.model.util.MementoFunctions;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;

/**
 * Web page representing the home page (showing a welcome message).
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class HomePage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    public HomePage() {
        super(new PageParameters(), ApplicationActions.INCLUDE);

        addChildComponents(null);
        buildGui();

        addBookmarkedPages();
    }

    private void buildGui() {
        final HomePageTuple homePageTuple = lookupHomePageAction();
        if(homePageTuple != null) {
            Components.permanentlyHide(this, ComponentType.WELCOME); 
            final ObjectAdapterMemento serviceMemento = MementoFunctions.fromAdapter().apply(homePageTuple.serviceAdapter);
            ActionMemento homePageActionMemento = MementoFunctions.fromAction().apply(homePageTuple.action);
            Mode mode = homePageTuple.action.getParameterCount() > 0? Mode.PARAMETERS : Mode.RESULTS;
            final IModel<?> actionModel = ActionModel.create(serviceMemento, homePageActionMemento, mode, SingleResultsMode.INLINE);
            getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.ACTION, actionModel);
        } else {
            Components.permanentlyHide(this, ComponentType.ACTION);
            getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.WELCOME, null);
        }
    }

    private static class HomePageTuple {
        HomePageTuple(ObjectAdapter serviceAdapter, ObjectAction objectAction) {
            this.serviceAdapter = serviceAdapter;
            action = objectAction;
        }
        ObjectAdapter serviceAdapter;
        ObjectAction action;
    }
    
    private HomePageTuple lookupHomePageAction() {
        List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();
        for (ObjectAdapter serviceAdapter : serviceAdapters) {
            final ObjectSpecification serviceSpec = serviceAdapter.getSpecification();
            List<ObjectAction> objectActions = serviceSpec.getObjectActions(Contributed.EXCLUDED);
            for (ObjectAction objectAction : objectActions) {
                if(objectAction.containsFacet(HomePageFacet.class)) {
                    return new HomePageTuple(serviceAdapter, objectAction);
                }
            }
        }
        return null;
    }

}
