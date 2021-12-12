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
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.commons.internal.base._Timing;
import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.modelhelpers.WhereAmIHelper;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.util.PageParameterUtils;
import org.apache.isis.viewer.wicket.ui.components.entity.icontitle.EntityIconAndTitlePanel;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

/**
 * Web page representing an entity.
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
//@Log4j2
public class EntityPage extends PageAbstract {

    private static final long serialVersionUID = 144368606134796079L;
    private static final CssResourceReference ENTITY_PAGE_CSS =
            new CssResourceReference(EntityPage.class, "EntityPage.css");

    private final EntityModel model;

    // -- FACTORIES

    /**
     * Called reflectively, in support of {@link BookmarkablePageLink} links.
     * Specifically handled by <code>IsisWicketApplication#newPageFactory()</code>
     *
     * Creates an EntityModel from the given page parameters.
     * Redirects to the application home page if there is no OID in the parameters.
     *
     * @param pageParameters The page parameters with the OID
     * @return An EntityModel for the requested OID
     */
    public static EntityPage ofPageParameters(
            final IsisAppCommonContext commonContext,
            final PageParameters pageParameters) {

        _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
            _Debug.log("new EntityPage from PageParameters %s", pageParameters);
        });

        val bookmark = PageParameterUtils.toBookmark(pageParameters);
        if(!bookmark.isPresent()) {
            throw new RestartResponseException(Application.get().getHomePage());
        }

        return new EntityPage(
                pageParameters,
                EntityModel.ofPageParameters(commonContext, pageParameters));
    }

    /**
     * Ensures that any exception that might have occurred already (eg from an action invocation) is shown.
     */
    public static EntityPage ofAdapter(
            final IsisAppCommonContext commonContext,
            final ManagedObject adapter) {

        _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
            _Debug.log("new EntityPage from Adapter %s", adapter.getSpecification());
        });

        return new EntityPage(
                PageParameterUtils.createPageParametersForObject(adapter),
                EntityModel.ofAdapter(commonContext, adapter));
    }

    // -- CONSTRUCTOR

    private EntityPage(
            final PageParameters pageParameters,
            final EntityModel entityModel) {

        super(pageParameters, null/*titleString*/, ComponentType.ENTITY);
        this.model = entityModel;
        buildPage();
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(ENTITY_PAGE_CSS));
    }

    @Override
    public void renderPage() {
        if(XrayUi.isXrayEnabled()){
            _Debug.log("about to render EntityPage ..");
            val stopWatch = _Timing.now();
            super.renderPage();
            stopWatch.stop();
            _Debug.log(".. rendering took %s", stopWatch.toString());
        } else {
            super.renderPage();
        }
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
        if(!model.isVisible()) {
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

        final String titleStr = objectAdapter.titleString();
        setTitle(titleStr);

        WebMarkupContainer entityPageContainer = new WebMarkupContainer("entityPageContainer");
        Wkt.cssAppend(entityPageContainer, objectSpec.getFeatureIdentifier());

        CssClassFacet cssClassFacet = objectSpec.getFacet(CssClassFacet.class);
        if(cssClassFacet != null) {
            final String cssClass = cssClassFacet.cssClass(objectAdapter);
            Wkt.cssAppend(entityPageContainer, cssClass);
        }

        themeDiv.addOrReplace(entityPageContainer);

        addWhereAmIIfShown(entityPageContainer, WhereAmIHelper.of(model));

        addChildComponents(entityPageContainer, model);

        // bookmarks and breadcrumbs
        bookmarkPageIfShown(model);
        addBreadcrumbIfShown(model);

        addBookmarkedPages(entityPageContainer);
    }

    protected void addWhereAmIIfShown(
            final WebMarkupContainer entityPageContainer,
            final WhereAmIHelper whereAmIModel) {

        val whereAmIContainer = new WebMarkupContainer("whereAmI-container");
        entityPageContainer.addOrReplace(whereAmIContainer);

        if(!whereAmIModel.isShowWhereAmI()) {
            whereAmIContainer.setVisible(false);
            return;
        }

        final RepeatingView listItems = new RepeatingView("whereAmI-items");

        whereAmIModel.streamParentChainReversed().forEach(entityModel->
            listItems.add(new EntityIconAndTitlePanel(listItems.newChildId(), entityModel)));

        Wkt.labelAdd(listItems, listItems.newChildId(), whereAmIModel.getStartOfChain().getTitle());

        whereAmIContainer.addOrReplace(listItems);

    }

    // -- UTILIY

    /**
     * Re-fetch entities for JAXB viewmodels, usually required once at begin of request.
     */
    public static void jaxbViewmodelRefresh(final IRequestablePage iRequestablePage) {
        if(iRequestablePage instanceof EntityPage) {

            val entityPage = (EntityPage) iRequestablePage;
            val entityModel = (EntityModel)entityPage.getUiHintContainerIfAny();
            val spec = entityModel.getObject().getSpecification();
            if(spec.isViewModel()) {

                val viewModelFacet = spec.getFacet(ViewModelFacet.class);
                if(viewModelFacet.containsEntities()) {

                    val viewmodel = entityModel.getBookmarkedOwner();
                    if(viewmodel.isBookmarkMemoized()) {
                        viewmodel.reloadViewmodelFromMemoizedBookmark();
                    } else {
                        val bookmark = PageParameterUtils
                                .toBookmark(entityPage.getPageParameters()).orElseThrow();
                        viewmodel.reloadViewmodelFromBookmark(bookmark);
                    }

                }
            }
        }
    }

    // -- HELPER

    private void addBreadcrumbIfShown(final EntityModel entityModel) {
        getBreadcrumbModel()
        .ifPresent(breadcrumbModel->breadcrumbModel.visited(entityModel));
    }

    private void removeAnyBreadcrumb(final EntityModel entityModel) {
        getBreadcrumbModel()
        .ifPresent(breadcrumbModel->breadcrumbModel.remove(entityModel));
    }



}
