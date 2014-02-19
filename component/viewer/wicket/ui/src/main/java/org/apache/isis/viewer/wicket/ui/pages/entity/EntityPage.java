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

package org.apache.isis.viewer.wicket.ui.pages.entity;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.hints.UiHintsBroadcastEvent;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModelProvider;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

/**
 * Web page representing an entity.
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class EntityPage extends PageAbstract {

    private static final long serialVersionUID = 1L;
    
    private final EntityModel model;

    /**
     * Called reflectively, in support of 
     * {@link BookmarkablePageLink bookmarkable} links.
     */
    public EntityPage(final PageParameters pageParameters) {
        this(new EntityModel(pageParameters), pageParameters);
    }
    
    private EntityPage(final EntityModel entityModel, final PageParameters pageParameters) {
        this(entityModel, pageParameters, entityModel.getObject().titleString(null));
    }

    public EntityPage(final ObjectAdapter adapter) {
        this(adapter, null);
    }

    /**
     * Ensure that any {@link ConcurrencyException} that might have occurred already
     * (eg from an action invocation) is show.
     */
    public EntityPage(ObjectAdapter adapter, ConcurrencyException exIfAny) {
        this(newEntityModel(adapter, exIfAny));
    }

    public EntityPage(ObjectAdapter adapter, ConcurrencyException exIfAny, PageParameters pageParameters) {
        this(newEntityModel(adapter, exIfAny), pageParameters);
    }

    private EntityPage(EntityModel entityModel) {
        // using the pageParameters implied by the entityModel means that the URL is preserved on redirect-after-post
        // however... the redirect seems to be swallowed in some cases, meaning that the page is not re-rendered at all
        // therefore, reverting this change.
        //this(entityModel, entityModel.getPageParameters());
        
        // using new PageParameters means that the page's URL will not be mounted, however it will at least re-render
        this(entityModel, new PageParameters());
    }

    private static EntityModel newEntityModel(ObjectAdapter adapter, ConcurrencyException exIfAny) {
        EntityModel model = new EntityModel(adapter);
        model.setException(exIfAny);
        return model;
    }

    private EntityPage(EntityModel entityModel, PageParameters pageParameters, String titleString) {
        super(pageParameters != null? pageParameters: entityModel.getPageParameters(), ApplicationActions.INCLUDE, titleString, ComponentType.ENTITY);

        // this is a work-around for JRebel integration...
        // ... even though the IsisJRebelPlugin calls invalidateCache, it seems that there is 
        // some caching elsewhere in the Wicket viewer meaning that stale metadata is referenced.
        // doing an additional call here seems to be sufficient, though not exactly sure why... :-(
        if(!getDeploymentType().isProduction()) {
            getSpecificationLoader().invalidateCacheFor(entityModel.getObject().getObject());
        }
        
        this.model = entityModel;
        addChildComponents(themeDiv, model);
        
        bookmarkPage(model);
        addBookmarkedPages();

        // breadcrumbs
        final BreadcrumbModelProvider session = (BreadcrumbModelProvider) getSession();
        final BreadcrumbModel breadcrumbModel = session.getBreadcrumbModel();
        
        breadcrumbModel.visited(entityModel);

        // ensure the copy link holds this page.
        send(this, Broadcast.BREADTH, new UiHintsBroadcastEvent(entityModel));
    }

    /**
     * A rather crude way of intercepting the redirect-and-post strategy.
     * 
     * <p>
     * Performs eager loading of corresponding {@link EntityModel}, with
     * {@link ConcurrencyChecking#NO_CHECK no} concurrency checking.
     */
    @Override
    protected void onBeforeRender() {
        this.model.load(ConcurrencyChecking.NO_CHECK);
        super.onBeforeRender();
    }

    private DeploymentType getDeploymentType() {
        return IsisContext.getDeploymentType();
    }

}
