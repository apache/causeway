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

package org.apache.isis.viewer.wicket.ui.components.entity.blocks.action;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionModel.SingleResultsMode;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.util.Actions;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.entity.blocks.summary.EntitySummaryPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageType;
import org.apache.isis.viewer.wicket.ui.util.Links;

public final class EntityActionLinkFactory implements CssMenuLinkFactory {

    private static final long serialVersionUID = 1L;

    private final EntitySummaryPanel summaryPanel;

    public EntityActionLinkFactory(final EntityModel entityModel, final EntitySummaryPanel summaryPanel) {
        this.summaryPanel = summaryPanel;
    }

    @Override
    public LinkAndLabel newLink(final ObjectAdapterMemento adapterMemento, final ObjectAction action, final String linkId) {
        final ObjectAdapter adapter = adapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK);

        final AbstractLink link = createLink(adapterMemento, action, linkId, adapter);
        final ObjectAdapter contextAdapter = summaryPanel.getEntityModel().getObject();
        final String label = Actions.labelFor(action, contextAdapter);

        return new LinkAndLabel(link, label);
    }

    private AbstractLink createLink(final ObjectAdapterMemento adapterMemento, final ObjectAction action, final String linkId, final ObjectAdapter adapter) {
        final Boolean persistent = adapter.representsPersistent();
        if (persistent) {
            return createLinkForPersistent(linkId, adapterMemento, action);
        } else {
            return createLinkForTransient(linkId, adapterMemento, action);
        }
    }

    private AbstractLink createLinkForPersistent(final String linkId, final ObjectAdapterMemento adapterMemento, final ObjectAction action) {
        final ObjectAdapter adapter = adapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK);
        final ObjectAdapter contextAdapter = summaryPanel.getEntityModel().getObject();

        final PageParameters pageParameters = ActionModel.createPageParameters(adapter, action, contextAdapter, ActionModel.SingleResultsMode.REDIRECT);
        final Class<? extends Page> pageClass = getPageClassRegistry().getPageClass(PageType.ACTION);
        return Links.newBookmarkablePageLink(linkId, pageParameters, pageClass);
    }

    private Link<?> createLinkForTransient(final String linkId, final ObjectAdapterMemento adapterMemento, final ObjectAction action) {
        final ActionMemento actionMemento = new ActionMemento(action);
        final ActionModel.Mode actionMode = ActionModel.determineMode(action);
        return new Link<String>(linkId) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                // TODO: seems like can't use REDIRECT, since won't
                // let multiple setResponsePage() calls once
                // committed to redirecting (I'm guessing)
                final ActionModel actionModel = ActionModel.create(adapterMemento, actionMemento, actionMode, SingleResultsMode.INLINE);
                summaryPanel.onClick(actionModel);
            }
        };
    }

    // ///////////////////////////////////////////////////////////////////
    // Dependencies (from IsisContext)
    // ///////////////////////////////////////////////////////////////////

    public IsisContext getIsisContext() {
        return IsisContext.getInstance();
    }

    public PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }


    // ///////////////////////////////////////////////////////////////////
    // Convenience
    // ///////////////////////////////////////////////////////////////////

    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        final ComponentFactoryRegistryAccessor cfra = (ComponentFactoryRegistryAccessor) Application.get();
        return cfra.getComponentFactoryRegistry();
    }

    protected PageClassRegistry getPageClassRegistry() {
        final PageClassRegistryAccessor pcra = (PageClassRegistryAccessor) Application.get();
        return pcra.getPageClassRegistry();
    }

}