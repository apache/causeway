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

import org.apache.isis.applib.layout.component.Grid;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModelProvider;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.wicket.Application;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;

/**
 * Web page representing an entity.
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class EntityPage extends PageAbstract {

    private static final long serialVersionUID = 1L;
    
    private final EntityModel model;
    private final String titleString;

    /**
     * Called reflectively, in support of 
     * {@link BookmarkablePageLink bookmarkable} links.
     */
    public EntityPage(final PageParameters pageParameters) {
        this(pageParameters, createEntityModel(pageParameters));
    }

    /**
     * Creates an EntityModel from the given page parameters.
     * Redirects to the application home page if there is no OID in the parameters.
     *
     * @param parameters The page parameters with the OID
     * @return An EntityModel for the requested OID
     */
    private static EntityModel createEntityModel(final PageParameters parameters) {
        String oid = EntityModel.oidStr(parameters);
        if (Strings.isEmpty(oid)) {
            throw new RestartResponseException(Application.get().getHomePage());
        }
        return new EntityModel(parameters);
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
        this(PageParametersUtils.newPageParameters(), newEntityModel(adapter, exIfAny));
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
        this.titleString = titleString;

        buildPage();
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


        ConcurrencyChecking.executeWithConcurrencyCheckingDisabled(
                new Runnable() {
                    @Override
                    public void run() {
                        EntityPage.this.model.load(ConcurrencyChecking.NO_CHECK);
                        EntityPage.super.onBeforeRender();
                    }
                }
        );
    }

    private void buildPage() {
        final ObjectAdapter objectAdapter;
        try {
            // check object still exists
            objectAdapter = model.getObject();
        } catch(final RuntimeException ex) {
            removeAnyBookmark(model);
            removeAnyBreadcrumb(model);

            // we throw an authorization exception here to avoid leaking out information as to whether the object exists or not.
            throw new ObjectMember.AuthorizationException(ex);
        }

        // check that the entity overall can be viewed.
        if(!ObjectAdapter.Util.isVisible(objectAdapter, InteractionInitiatedBy.USER)) {
            throw new ObjectMember.AuthorizationException();
        }

        final ObjectSpecification objectSpec = model.getTypeOfSpecification();
        final GridFacet gridFacet = objectSpec.getFacet(GridFacet.class);
        if(gridFacet != null) {
            // the facet should always exist, in fact
            // just enough to ask for the metadata.
            // This will cause the current ObjectSpec to be updated as a side effect.
            final Grid unused = gridFacet.getGrid();
        }

        if(titleString == null) {
            final String titleStr = objectAdapter.titleString(null);
            setTitle(titleStr);
        }

        WebMarkupContainer entityPageContainer = new WebMarkupContainer("entityPageContainer");
        CssClassAppender.appendCssClassTo(entityPageContainer, objectSpec.getFullIdentifier().replace('.','-'));
        CssClassAppender.appendCssClassTo(entityPageContainer, objectSpec.getCorrespondingClass().getSimpleName());

        CssClassFacet cssClassFacet = objectSpec.getFacet(CssClassFacet.class);
        if(cssClassFacet != null) {
            final String cssClass = cssClassFacet.cssClass(objectAdapter);
            CssClassAppender.appendCssClassTo(entityPageContainer, cssClass);
        }

        themeDiv.addOrReplace(entityPageContainer);

        addChildComponents(entityPageContainer, model);

        // bookmarks and breadcrumbs
        bookmarkPage(model);
        addBreadcrumb(model);

        addBookmarkedPages(entityPageContainer);
    }

    protected DeploymentCategory getDeploymentCategory() {
        return getIsisSessionFactory().getDeploymentCategory();
    }

}
