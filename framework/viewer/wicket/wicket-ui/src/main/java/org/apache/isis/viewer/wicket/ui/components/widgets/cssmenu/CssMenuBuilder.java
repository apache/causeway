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

package org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Collections2;

import org.apache.wicket.Application;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.progmodel.facets.actions.notcontributed.NotContributedFacet;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.util.Actions;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuItem.Builder;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuPanel.Style;

/**
 * Used to build a {@link CssMenuItem} hierarchy from a
 * {@link ObjectAdapterMemento object adapter}'s actions and any contributed
 * actions from services.
 */
public class CssMenuBuilder {

    private final ObjectAdapterMemento adapterMemento;
    private final List<ObjectAdapter> serviceAdapters;
    private final List<ObjectAction> actions;

    private final CssMenuLinkFactory cssMenuLinkFactory;

    public CssMenuBuilder(final ObjectAdapterMemento adapterMemento, final List<ObjectAdapter> serviceAdapters, final List<ObjectAction> actions, final CssMenuLinkFactory cssMenuLinkFactory) {
        this.adapterMemento = adapterMemento;
        this.serviceAdapters = serviceAdapters;
        this.actions = actions;
        this.cssMenuLinkFactory = cssMenuLinkFactory;
    }

    public CssMenuPanel buildPanel(final String wicketId, final String rootName) {
        final CssMenuItem findUsing = CssMenuItem.newMenuItem(rootName).build();
        addMenuItems(findUsing, actions);
        final CssMenuPanel cssMenuPanel = new CssMenuPanel(wicketId, Style.SMALL, Collections.singletonList(findUsing));
        return cssMenuPanel;
    }

    private void addMenuItems(final CssMenuItem parent, final List<ObjectAction> actions) {
        addMenuItemsForActionsOfType(parent, actions, ActionType.SET);
        addMenuItemsForActionsOfType(parent, actions, ActionType.USER);
        if (isExplorationMode()) {
            addMenuItemsForActionsOfType(parent, actions, ActionType.EXPLORATION);
        }
        if (isDebugMode()) {
            addMenuItemsForActionsOfType(parent, actions, ActionType.DEBUG);
        }
    }

    /**
     * Protected so can be overridden in testing if required.
     */
    protected boolean isExplorationMode() {
        //return Application.get().getConfigurationType().equalsIgnoreCase(Application.DEVELOPMENT);
        return Application.get().usesDeploymentConfig();
    }

    /**
     * Protected so can be overridden in testing if required.
     */
    protected boolean isDebugMode() {
        // TODO: need to figure out how to switch into debug mode;
        // probably call a Debug toggle page, and stuff into
        // Session.getMetaData()
        return true;
    }

    private void addMenuItemsForActionsOfType(final CssMenuItem parent, final List<ObjectAction> actions, final ActionType type) {
        final Collection<ObjectAction> filterActionsOfType = Collections2.filter(actions, Actions.ofType(type));
        for (final ObjectAction action : filterActionsOfType) {
            addMenuItem(parent, action);
        }
    }

    private void addMenuItems(final CssMenuItem parent, final ObjectAction[] actions) {
        addMenuItems(parent, Arrays.asList(actions));
    }

    private void addMenuItem(final CssMenuItem parent, final ObjectAction action) {
        if (action.getType() == ActionType.SET) {
            addMenuItemForActionSet(parent, action);
        } else {
            addMenuItemForAction(parent, action);
        }
    }

    private void addMenuItemForActionSet(final CssMenuItem parent, final ObjectAction action) {
        final Builder builder = parent.newSubMenuItem(action.getName());
        final List<ObjectAction> actions = action.getActions();
        addMenuItems(builder.itemBeingBuilt(), actions);
        if (builder.itemBeingBuilt().hasSubMenuItems()) {
            builder.build();
        }
    }

    private void addMenuItemForAction(final CssMenuItem parent, final ObjectAction contributedAction) {

        // skip if annotated to not be contributed
        if (contributedAction.getFacet(NotContributedFacet.class) != null) {
            return;
        }

        final ObjectAdapterMemento serviceAdapterMemento = determineAdapterFor(contributedAction);

        final Builder subMenuItemBuilder = parent.newSubMenuItem(serviceAdapterMemento, contributedAction, cssMenuLinkFactory);
        if (subMenuItemBuilder != null) {
            // could be null if invisible
            subMenuItemBuilder.build();
        }
    }

    /**
     * It's a bit hokey to have to do this, but the
     * {@link ObjectSpecification#getServiceActionsReturning(ActionType...)
     * method we call} on {@link ObjectSpecification}, while nicely traversing
     * the services for us, unfortunately does not pass us back the service
     * adapters also.
     */
    private ObjectAdapterMemento determineAdapterFor(final ObjectAction action) {
        // search through service adapters first
        final ObjectSpecification onType = action.getOnType();
        for (final ObjectAdapter serviceAdapter : getServiceAdapters()) {
            if (serviceAdapter.getSpecification() == onType) {
                return ObjectAdapterMemento.createOrNull(serviceAdapter);
            }
        }
        // otherwise, specified adapter
        return adapterMemento;
    }

    protected List<ObjectAdapter> getServiceAdapters() {
        return serviceAdapters;
    }

}
