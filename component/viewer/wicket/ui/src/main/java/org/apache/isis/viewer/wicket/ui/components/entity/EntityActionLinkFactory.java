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

package org.apache.isis.viewer.wicket.ui.components.entity;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActions;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionModel.SingleResultsMode;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuItem;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.action.ActionPage;
import org.apache.isis.viewer.wicket.ui.util.Links;

public final class EntityActionLinkFactory implements CssMenuLinkFactory {

    private static final long serialVersionUID = 1L;

    private final EntityModel entityModel;

    public EntityActionLinkFactory(final EntityModel entityModel) {
        this.entityModel = entityModel;
    }

    @Override
    public LinkAndLabel newLink(final ObjectAdapterMemento adapterMemento, final ObjectAction action, final String linkId) {
        final ObjectAdapter adapter = adapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK);

        final AbstractLink link = createLink(adapterMemento, action, linkId, adapter);
        //action
        final String label = ObjectActions.nameFor(action);

        // check visibility and whether enabled
        final AuthenticationSession session = getAuthenticationSession();

        final Consent visibility = action.isVisible(session, adapter, Where.OBJECT_FORMS);
        if (visibility.isVetoed()) {
            return null;
        }

        final Consent usability = action.isUsable(session, adapter, Where.OBJECT_FORMS);
        final String disabledReasonIfAny = usability.getReason();
        if(disabledReasonIfAny != null) {
            link.setEnabled(false);
        }

        final boolean blobOrClob = CssMenuItem.returnsBlobOrClob(action);
        final boolean prototype = CssMenuItem.isExplorationOrPrototype(action);
        final String actionIdentifier = CssMenuItem.actionIdentifierFor(action);
        final String cssClass = CssMenuItem.cssClassFor(action);
        return new LinkAndLabel(link, label, disabledReasonIfAny, blobOrClob, prototype, actionIdentifier, cssClass);
    }

    private AbstractLink createLink(final ObjectAdapterMemento adapterMemento, final ObjectAction action, final String linkId, final ObjectAdapter adapter) {
        final Boolean persistent = adapter.representsPersistent();
        if (persistent) {
            return createLinkForPersistent(linkId, adapterMemento, action);
        } else {
            return createLinkForTransient(linkId, adapterMemento, action);
        }
    }

    /**
     * Creates a link to the {@link ActionPage} (ie the {@link PageClassRegistry registered page} for 
     * {@link PageType#ACTION action}s) with the appropriate {@link PageParameters} to either render the action's
     * parameter form (if it takes arguments) or to invoke the action directly.
     * 
     * <p>
     * If the action's {@link ObjectAction#getSemantics() semantics} are {@link ActionSemantics.Of#SAFE safe}, then
     * concurrency checking is disabled; otherwise it is enforced.
     */
    private AbstractLink createLinkForPersistent(final String linkId, final ObjectAdapterMemento adapterMemento, final ObjectAction action) {
        final ObjectAdapter adapter = adapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK);
        final ObjectAdapter contextAdapter = entityModel.getObject();

        // use the action semantics to determine whether invoking this action will require a concurrency check or not
        // if it's "safe", then we'll just continue without any checking. 
        final ConcurrencyChecking concurrencyChecking = ConcurrencyChecking.concurrencyCheckingFor(action.getSemantics());
        final PageParameters pageParameters = ActionModel.createPageParameters(adapter, action, contextAdapter, ActionModel.SingleResultsMode.REDIRECT, concurrencyChecking);
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
                setResponsePage(new ActionPage(actionModel));
            }
        };
    }

    // ///////////////////////////////////////////////////////////////////
    // Dependencies (from IsisContext)
    // ///////////////////////////////////////////////////////////////////

    protected IsisContext getIsisContext() {
        return IsisContext.getInstance();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
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