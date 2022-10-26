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
package org.apache.causeway.viewer.wicket.ui.pages.entity;

import org.apache.wicket.Application;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.internal.debug._Debug;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.modelhelpers.WhereAmIHelper;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.components.entity.icontitle.EntityIconAndTitlePanel;
import org.apache.causeway.viewer.wicket.ui.pages.PageAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.val;

/**
 * Web page representing an entity.
 */
@AuthorizeInstantiation(UserMemento.AUTHORIZED_USER_ROLE)
//@Log4j2
public class EntityPage extends PageAbstract {

    private static final long serialVersionUID = 144368606134796079L;
    private static final CssResourceReference ENTITY_PAGE_CSS =
            new CssResourceReference(EntityPage.class, "EntityPage.css");

    private final UiObjectWkt model;

    // -- FACTORIES

    /**
     * Called reflectively, in support of {@link BookmarkablePageLink} links.
     * Specifically handled by <code>CausewayWicketApplication#newPageFactory()</code>
     *
     * Creates an EntityModel from the given page parameters.
     * Redirects to the application home page if there is no OID in the parameters.
     *
     * @param pageParameters The page parameters with the OID
     * @return An EntityModel for the requested OID
     */
    public static EntityPage forPageParameters(
            final MetaModelContext commonContext,
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
                UiObjectWkt.ofPageParameters(commonContext, pageParameters));
    }

    /**
     * Ensures that any exception that might have occurred already (eg from an action invocation) is shown.
     */
    public static EntityPage forAdapter(
            final MetaModelContext commonContext,
            final ManagedObject adapter) {

        _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
            _Debug.log("new EntityPage from Adapter %s", adapter.getSpecification());
        });

        return new EntityPage(
                PageParameterUtils.createPageParametersForObject(adapter),
                UiObjectWkt.ofAdapter(commonContext, adapter));
    }

    // -- CONSTRUCTOR

    private EntityPage(
            final PageParameters pageParameters,
            final UiObjectWkt entityModel) {
        super(pageParameters, null/*titleString*/, UiComponentType.ENTITY);
        this.model = entityModel;
    }

    @Override
    protected void onInitialize() {
        buildPage();
        super.onInitialize();
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(ENTITY_PAGE_CSS));
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

        Facets.gridPreload(objectSpec, objectAdapter);

        final String titleStr = objectAdapter.getTitle();
        setTitle(titleStr);

        WebMarkupContainer entityPageContainer = new WebMarkupContainer("entityPageContainer");
        Wkt.cssAppend(entityPageContainer, objectSpec.getFeatureIdentifier());

        Facets.cssClass(objectSpec, objectAdapter)
        .ifPresent(cssClass->
            Wkt.cssAppend(entityPageContainer, cssClass)
        );

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

    // -- REFRESH ENTITIES

    @Override
    public void onNewRequestCycle() {
        val entityModel = (UiObjectWkt) getUiHintContainerIfAny();
        ManagedObjects.refreshViewmodel(entityModel.getObject(),
                ()->PageParameterUtils
                        .toBookmark(getPageParameters())
                        .orElseThrow());
    }

    // -- HELPER

    private void addBreadcrumbIfShown(final UiObjectWkt entityModel) {
        getBreadcrumbModel()
        .ifPresent(breadcrumbModel->breadcrumbModel.visited(entityModel));
    }

    private void removeAnyBreadcrumb(final UiObjectWkt entityModel) {
        getBreadcrumbModel()
        .ifPresent(breadcrumbModel->breadcrumbModel.remove(entityModel));
    }



}
