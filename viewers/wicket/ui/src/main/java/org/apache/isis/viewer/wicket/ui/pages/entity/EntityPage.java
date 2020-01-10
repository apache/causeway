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

import org.apache.wicket.Application;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.string.Strings;

import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.whereami.WhereAmIModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.icontitle.EntityIconAndTitlePanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModelProvider;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.webapp.context.IsisWebAppCommonContext;

import lombok.val;

/**
 * Web page representing an entity.
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class EntityPage extends PageAbstract {

    private static final long serialVersionUID = 144368606134796079L;
    private static final CssResourceReference WHERE_AM_I_CSS =
            new CssResourceReference(EntityPage.class, "EntityPage.css");

    private final EntityModel model;
    private final String titleString;

    /**
     * Called reflectively, in support of {@link BookmarkablePageLink} links. 
     * Specifically handled by {@link org.apache.isis.viewer.wicket.viewer.IsisWicketApplication_newPageFactory}
     */
    public static EntityPage bookmarked(
            IsisWebAppCommonContext commonContext, 
            PageParameters pageParameters) {
        
        val entityModel = createEntityModel(commonContext, pageParameters);
        return new EntityPage(pageParameters, entityModel);
    }

    /**
     * Creates an EntityModel from the given page parameters.
     * Redirects to the application home page if there is no OID in the parameters.
     *
     * @param parameters The page parameters with the OID
     * @return An EntityModel for the requested OID
     */
    private static EntityModel createEntityModel(
            IsisWebAppCommonContext commonContext, 
            PageParameters parameters) {
        
        String oid = EntityModel.oidStr(parameters);
        if (Strings.isEmpty(oid)) {
            throw new RestartResponseException(Application.get().getHomePage());
        }
        return EntityModel.ofParameters(commonContext, parameters);
    }

    private EntityPage(final PageParameters pageParameters, final EntityModel entityModel) {
        this(pageParameters, entityModel, null);
    }

    /**
     * Ensure that any {@link ConcurrencyException} that might have occurred already
     * (eg from an action invocation) is show.
     */
    public EntityPage(
            IsisWebAppCommonContext commonContext, 
            ManagedObject adapter) {
        
        this(PageParametersUtils.newPageParameters(), newEntityModel(commonContext, adapter));
    }

    private static EntityModel newEntityModel(
            IsisWebAppCommonContext commonContext,
            ManagedObject adapter) {
        
        val entityModel = EntityModel.ofAdapter(commonContext, adapter);
        return entityModel;
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

    private void addBreadcrumbIfShown(final EntityModel entityModel) {
        if(!isShowBreadcrumbs()) {
            return;
        }
        final BreadcrumbModelProvider session = (BreadcrumbModelProvider) getSession();
        final BreadcrumbModel breadcrumbModel = session.getBreadcrumbModel();
        breadcrumbModel.visited(entityModel);
    }

    private void removeAnyBreadcrumb(final EntityModel entityModel) {
        final BreadcrumbModelProvider session = (BreadcrumbModelProvider) getSession();
        final BreadcrumbModel breadcrumbModel = session.getBreadcrumbModel();
        breadcrumbModel.remove(entityModel);
    }
    
    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(WHERE_AM_I_CSS));
    }


    @Override
    public UiHintContainer getUiHintContainerIfAny() {
        return model;
    }

    private void buildPage() {
        final ManagedObject objectAdapter;
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
        if(!ManagedObject.VisibilityUtil.isVisible(objectAdapter, InteractionInitiatedBy.USER)) {
            throw new ObjectMember.AuthorizationException();
        }

        final ObjectSpecification objectSpec = model.getTypeOfSpecification();
        final GridFacet gridFacet = objectSpec.getFacet(GridFacet.class);
        if(gridFacet != null) {
            // the facet should always exist, in fact
            // just enough to ask for the metadata.
            // This will cause the current ObjectSpec to be updated as a side effect.
            gridFacet.getGrid(objectAdapter);
        }

        if(titleString == null) {
            final String titleStr = objectAdapter.titleString(null);
            setTitle(titleStr);
        }

        WebMarkupContainer entityPageContainer = new WebMarkupContainer("entityPageContainer");
        CssClassAppender.appendCssClassTo(entityPageContainer,
                CssClassAppender.asCssStyle("isis-" + objectSpec.getSpecId().asString().replace(".","-")));

        CssClassFacet cssClassFacet = objectSpec.getFacet(CssClassFacet.class);
        if(cssClassFacet != null) {
            final String cssClass = cssClassFacet.cssClass(objectAdapter);
            CssClassAppender.appendCssClassTo(entityPageContainer, cssClass);
        }

        themeDiv.addOrReplace(entityPageContainer);

        addWhereAmIIfShown(entityPageContainer, WhereAmIModel.of(model));

        addChildComponents(entityPageContainer, model);

        // bookmarks and breadcrumbs
        bookmarkPageIfShown(model);
        addBreadcrumbIfShown(model);

        addBookmarkedPages(entityPageContainer);


    }

    protected void addWhereAmIIfShown(
            WebMarkupContainer entityPageContainer,
            WhereAmIModel whereAmIModel) {

        val whereAmIContainer = new WebMarkupContainer("whereAmI-container");
        entityPageContainer.addOrReplace(whereAmIContainer);

        if(!whereAmIModel.isShowWhereAmI()) {
            whereAmIContainer.setVisible(false);
            return;
        }

        final RepeatingView listItems = new RepeatingView("whereAmI-items");

        whereAmIModel.streamParentChainReversed().forEach(entityModel->
            listItems.add(new EntityIconAndTitlePanel(listItems.newChildId(), entityModel)));

        listItems.add(new Label(listItems.newChildId(), whereAmIModel.getStartOfChain().getTitle()));

        whereAmIContainer.addOrReplace(listItems);

    }
}
