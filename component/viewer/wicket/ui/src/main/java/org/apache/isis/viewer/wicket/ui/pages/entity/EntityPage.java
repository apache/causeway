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

import java.util.List;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ObjectVisibilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.hints.IsisUiHintEvent;
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
        this(pageParameters, new EntityModel(pageParameters));
    }
    
    private EntityPage(final PageParameters pageParameters, final EntityModel entityModel) {
        this(pageParameters, entityModel, null);
    }

    public EntityPage(final ObjectAdapter adapter) {
        this(adapter, null);
    }

    /**
     * Ensure that any {@link ConcurrencyException} that might have occurred already
     * (eg from an action invocation) is show.
     */
    public EntityPage(final ObjectAdapter adapter, final ConcurrencyException exIfAny) {
        this(new PageParameters(), newEntityModel(adapter, exIfAny));
    }

    private static EntityModel newEntityModel(
            final ObjectAdapter adapter,
            final ConcurrencyException exIfAny) {
        final EntityModel model = new EntityModel(adapter);
        model.setException(exIfAny);
        return model;
    }

    private EntityPage(
            final PageParameters pageParameters,
            final EntityModel entityModel,
            final String titleString) {
        super(pageParameters, titleString, ComponentType.ENTITY);

        this.model = entityModel;

        final ObjectAdapter objectAdapter;
        try {
            // check object still exists
            objectAdapter = entityModel.getObject();
        } catch(final RuntimeException ex) {
            removeAnyBookmark(model);
            removeAnyBreadcrumb(model);

            // we throw an authorization exception here to avoid leaking out information as to whether the object exists or not.
            throw new ObjectMember.AuthorizationException(ex);
        }

        // check that the entity overall can be viewed.
        if(!isVisible(objectAdapter)) {
            throw new ObjectMember.AuthorizationException();
        }

        // belt-n-braces: check that at least one property of the entity can be viewed.
        final AuthenticationSession session = getAuthenticationSession();
        final ObjectSpecification specification = objectAdapter.getSpecification();
        final List<ObjectAssociation> visibleAssociation = specification.getAssociations(Contributed.INCLUDED, ObjectAssociation.Filters.dynamicallyVisible(session, objectAdapter, Where.NOWHERE));

        if(visibleAssociation.isEmpty()) {
            throw new ObjectMember.AuthorizationException();
        }


        // the next bit is a work-around for JRebel integration...
        // ... even though the IsisJRebelPlugin calls invalidateCache, it seems that there is 
        // some caching elsewhere in the Wicket viewer meaning that stale metadata is referenced.
        // doing an additional call here seems to be sufficient, though not exactly sure why... :-(
        if(!getDeploymentType().isProduction()) {
            getSpecificationLoader().invalidateCacheFor(objectAdapter.getObject());
        }

        if(titleString == null) {
            final String titleStr = objectAdapter.titleString(null);
            setTitle(titleStr);
        }
        
        addChildComponents(themeDiv, model);
        
        // bookmarks and breadcrumbs
        bookmarkPage(model);
        addBreadcrumb(entityModel);

        addBookmarkedPages();


        // TODO mgrigorov: Zero Clipboard has been moved to EntityIconAndTitlePanel where the entity model is available.
        // Is this still needed for something else ?!
        //
        // ensure the copy link holds this page.
        send(this, Broadcast.BREADTH, new IsisUiHintEvent(entityModel, null));
    }

    private boolean isVisible(final ObjectAdapter input) {
        final InteractionResult visibleResult = InteractionUtils.isVisibleResult(input.getSpecification(), createVisibleInteractionContext(input));
        return visibleResult.isNotVetoing();
    }

    private VisibilityContext<?> createVisibleInteractionContext(final ObjectAdapter objectAdapter) {
        return new ObjectVisibilityContext(
                getDeploymentCategory(),
                getAuthenticationSession(),
                InteractionInvocationMethod.BY_USER,
                objectAdapter,
                objectAdapter.getSpecification().getIdentifier(),
                Where.OBJECT_FORMS);
    }



    private void addBreadcrumb(final EntityModel entityModel) {
        final BreadcrumbModelProvider session = (BreadcrumbModelProvider) getSession();
        final BreadcrumbModel breadcrumbModel = session.getBreadcrumbModel();
        breadcrumbModel.visited(entityModel);
    }

    private void removeAnyBreadcrumb(final EntityModel entityModel) {
        final BreadcrumbModelProvider session = (BreadcrumbModelProvider) getSession();
        final BreadcrumbModel breadcrumbModel = session.getBreadcrumbModel();
        breadcrumbModel.remove(entityModel);
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

    protected DeploymentCategory getDeploymentCategory() {
        return getDeploymentType().getDeploymentCategory();
    }


}
