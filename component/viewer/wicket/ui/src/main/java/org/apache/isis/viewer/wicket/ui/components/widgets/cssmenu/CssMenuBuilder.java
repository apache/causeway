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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Collections2;

import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.progmodel.facets.actions.bulk.BulkFacet;
import org.apache.isis.core.progmodel.facets.actions.notcontributed.NotContributedFacet;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionPromptModalWindowProvider;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuItem.Builder;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuPanel.Style;

/**
 * Used to build a {@link CssMenuItem} hierarchy from a
 * {@link ObjectAdapterMemento object adapter}'s actions and any contributed
 * actions from services.
 */
public class CssMenuBuilder {

    /**
     * The target to invoke upon; may be null in case of bulk actions; not used if a contributed action. 
     */
    private final ObjectAdapterMemento adapterMemento;
    /**
     * Used to determine wire up against contributed actions.
     */
    private final List<ObjectAdapter> serviceAdapters;
    
    private final List<ObjectAction> actions;

    private final ActionLinkFactory cssMenuLinkFactory;
    
    public CssMenuBuilder(final ObjectAdapterMemento adapterMemento, final List<ObjectAdapter> serviceAdapters, final List<ObjectAction> actions, final ActionLinkFactory cssMenuLinkFactory) {
        this.adapterMemento = adapterMemento; // may be null
        this.serviceAdapters = serviceAdapters;
        this.actions = actions;
        this.cssMenuLinkFactory = cssMenuLinkFactory;
    }

    public CssMenuBuilder(final List<ObjectAction> actions, final ActionLinkFactory cssMenuLinkFactory) {
        this(null, null, actions, cssMenuLinkFactory);
    }


    public CssMenuPanel buildPanel(
            final String wicketId, final String rootName, 
            final ActionPromptModalWindowProvider actionPromptModalWindowProvider) {
        final CssMenuItem findUsing = CssMenuItem.newMenuItem(rootName).build();
        addMenuItems(findUsing, actions, actionPromptModalWindowProvider);
        final CssMenuPanel cssMenuPanel = new CssMenuPanel(wicketId, Style.SMALL, Collections.singletonList(findUsing));
        return cssMenuPanel;
    }

    private void addMenuItems(
            final CssMenuItem parent, 
            final List<ObjectAction> actions, 
            final ActionPromptModalWindowProvider actionPromptModalWindowProvider) {
        addMenuItemsForActionsOfType(parent, actions, ActionType.USER, actionPromptModalWindowProvider);
        if ( isExploring() || isPrototyping()) {
            addMenuItemsForActionsOfType(parent, actions, ActionType.EXPLORATION, actionPromptModalWindowProvider);
            addMenuItemsForActionsOfType(parent, actions, ActionType.PROTOTYPE, actionPromptModalWindowProvider);
        }
        if (isDebugMode()) {
            addMenuItemsForActionsOfType(parent, actions, ActionType.DEBUG, actionPromptModalWindowProvider);
        }
    }

    public boolean isExploring() {
        return IsisContext.getDeploymentType().isExploring();
    }
    public boolean isPrototyping() {
        return IsisContext.getDeploymentType().isPrototyping();
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

    private void addMenuItemsForActionsOfType(
            final CssMenuItem parent, 
            final List<ObjectAction> actions, 
            final ActionType type, 
            final ActionPromptModalWindowProvider actionPromptModalWindowProvider) {
        final Collection<ObjectAction> filterActionsOfType = Collections2.filter(actions, Filters.asPredicate(ObjectAction.Filters.ofType(type)));
        for (final ObjectAction action : filterActionsOfType) {
            addMenuItem(parent, action, actionPromptModalWindowProvider);
        }
    }

    private void addMenuItem(
            final CssMenuItem parent, 
            final ObjectAction action, 
            final ActionPromptModalWindowProvider actionPromptModalWindowProvider) {
        addMenuItemForAction(parent, action, actionPromptModalWindowProvider);
    }

    private void addMenuItemForAction(
            final CssMenuItem parent, 
            final ObjectAction action, 
            final ActionPromptModalWindowProvider actionPromptModalWindowProvider) {
        
        final NotContributedFacet notContributed = action.getFacet(NotContributedFacet.class);
        if (notContributed != null && notContributed.toActions()) {
            // skip if is an action that has been annotated to not be contributed
            return;
        }

        Builder subMenuItemBuilder = null;
        
        final ObjectAdapterMemento targetAdapterMemento = adapterMemento; // determineAdapterFor(action);
        if(targetAdapterMemento != null) {
            // against an entity or a service (if a contributed action)
            subMenuItemBuilder = parent.newSubMenuItem(targetAdapterMemento, action, cssMenuLinkFactory, actionPromptModalWindowProvider);
        } else {
            if (action.containsDoOpFacet(BulkFacet.class)) {
                // ignore fact have no target action; 
                // we expect that the link factory is able to handle this
                // (ie will iterate through all objects from a list and invoke in bulk)
                subMenuItemBuilder = parent.newSubMenuItem(action, cssMenuLinkFactory, actionPromptModalWindowProvider);
            }
        }
        
        if (subMenuItemBuilder != null) {
            subMenuItemBuilder.build();
        }
    }

    protected List<ObjectAdapter> getServiceAdapters() {
        return serviceAdapters;
    }

}
