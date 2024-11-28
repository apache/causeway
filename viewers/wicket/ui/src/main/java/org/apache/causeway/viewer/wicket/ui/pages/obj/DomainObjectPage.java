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
package org.apache.causeway.viewer.wicket.ui.pages.obj;

import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.wicket.Application;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.publishing.spi.PageRenderSubscriber;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.modelhelpers.WhereAmIHelper;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.components.entity.icontitle.EntityIconAndTitlePanelFactory;
import org.apache.causeway.viewer.wicket.ui.pages.PageAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * Web page representing an entity.
 */
@AuthorizeInstantiation(UserMemento.AUTHORIZED_USER_ROLE)
//@Log4j2
public class DomainObjectPage extends PageAbstract {

    private static final long serialVersionUID = 144368606134796079L;

    private static final String ID_DOMAIN_OBJECT_CONTAINER = "domainObjectContainer";
    private static final String ID_DOMAIN_OBJECT = UiComponentType.DOMAIN_OBJECT.getId();
    private static final String ID_WHEREAMI_CONTAINER = "whereAmI-container";
    private static final String ID_WHEREAMI_ITEMS = "whereAmI-items";

    private static final CssResourceReference DOMAIN_OBJECT_PAGE_CSS =
            new CssResourceReference(DomainObjectPage.class, "DomainObjectPage.css");

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
    public static DomainObjectPage forPageParameters(final PageParameters pageParameters) {
        var bookmark = PageParameterUtils.toBookmark(pageParameters);
        if(!bookmark.isPresent()) {
            throw new RestartResponseException(Application.get().getHomePage());
        }
        return new DomainObjectPage(
                pageParameters,
                UiObjectWkt.ofPageParameters(pageParameters));
    }

    /**
     * Ensures that any exception that might have occurred already (eg from an action invocation) is shown.
     */
    public static DomainObjectPage forAdapter(
            final ManagedObject adapter) {
        return new DomainObjectPage(
                PageParameterUtils.createPageParametersForObject(adapter),
                UiObjectWkt.ofAdapter(adapter));
    }

    // -- CONSTRUCTOR

    private DomainObjectPage(
            final PageParameters pageParameters,
            final UiObjectWkt objectModel) {
        super(pageParameters, null/*titleString*/, UiComponentType.DOMAIN_OBJECT);
        this.model = objectModel;
    }

    @Override
    protected void onInitialize() {
        buildPage();
        super.onInitialize();
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(DOMAIN_OBJECT_PAGE_CSS));
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

        var objectSpec = model.getTypeOfSpecification();

        Facets.gridPreload(objectSpec, objectAdapter);

        var titleStr = objectAdapter.getTitle();
        setTitle(titleStr);

        var domainObjectContainer = new WebMarkupContainer(ID_DOMAIN_OBJECT_CONTAINER);
        Wkt.cssAppend(domainObjectContainer, objectSpec.getFeatureIdentifier());

        Facets.cssClass(objectSpec, objectAdapter)
            .ifPresent(cssClass->
                Wkt.cssAppend(domainObjectContainer, cssClass)
            );

        themeDiv.addOrReplace(domainObjectContainer);

        addWhereAmIIfShown(domainObjectContainer, WhereAmIHelper.of(model));

        // bookmarks and breadcrumbs
        bookmarkPageIfShown(model);
        addBreadcrumbIfShown(model);

        addBookmarkedPages(domainObjectContainer);

        // CAUSEWAY[3626] do this last, could throw ObjectNotFoundException:
        Try.run(()->addChildComponents(domainObjectContainer, model))
            .ifFailure(__->{
                // For the non happy case, ensure we have the model populated for Wicket,
                // such that we don't provoke an ErrorPage simply because the model is missing parts.
                Wkt.add(domainObjectContainer, new EmptyPanel(ID_DOMAIN_OBJECT));
            })
            .ifFailureFail(); // simply re-throw
    }

    protected void addWhereAmIIfShown(
            final WebMarkupContainer domainObjectContainer,
            final WhereAmIHelper whereAmIModel) {

        var whereAmIContainer = new WebMarkupContainer(ID_WHEREAMI_CONTAINER);
        domainObjectContainer.addOrReplace(whereAmIContainer);

        if(!whereAmIModel.isShowWhereAmI()) {
            whereAmIContainer.setVisible(false);
            return;
        }

        final RepeatingView listItems = new RepeatingView(ID_WHEREAMI_ITEMS);

        whereAmIModel.streamParentChainReversed().forEach(objectModel->
            listItems.add(EntityIconAndTitlePanelFactory.entityIconAndTitlePanel(listItems.newChildId(), objectModel)));

        Wkt.labelAdd(listItems, listItems.newChildId(), whereAmIModel.getStartOfChain().getTitle());

        whereAmIContainer.addOrReplace(listItems);
    }

    // -- REFRESH ENTITIES

    @Override
    public void onNewRequestCycle() {
        var objectModel = (UiObjectWkt) getUiHintContainerIfAny();
        ManagedObjects.refreshViewmodel(objectModel.getObject(),
                ()->PageParameterUtils
                        .toBookmark(getPageParameters())
                        .orElseThrow());
    }

    @Override
    public void onRendering(final Can<PageRenderSubscriber> pageRenderSubscribers) {
        onRenderingOrRendered(pageRenderSubscribers, (pageRenderSubscriber, bookmark) -> {
            pageRenderSubscriber.onRenderingDomainObject(bookmark);
            return null;
        });
    }

    @Override
    public void onRendered(final Can<PageRenderSubscriber> pageRenderSubscribers) {
        onRenderingOrRendered(pageRenderSubscribers, (pageRenderSubscriber, bookmark) -> {
            pageRenderSubscriber.onRenderedDomainObject(bookmark);
            return null;
        });
    }

    private void onRenderingOrRendered(
            final Can<PageRenderSubscriber> pageRenderSubscribers,
            final BiFunction<PageRenderSubscriber, Bookmark, Void> handler) {

        if(pageRenderSubscribers.isEmpty()) {
            return;
        }

        // guard against unspecified
        ManagedObjects.asSpecified(model.getObject())
            .map(ManagedObject::getBookmark)
            // guard against no bookmark available
            .filter(Optional::isPresent)
            .map(Optional::get)
            .ifPresent(bookmark->{
                pageRenderSubscribers
                    .forEach(pageRenderSubscriber -> handler.apply(pageRenderSubscriber, bookmark));
            });
    }

    // -- HELPER

    private void addBreadcrumbIfShown(final UiObjectWkt objectModel) {
        getBreadcrumbModel()
        .ifPresent(breadcrumbModel->breadcrumbModel.visited(objectModel));
    }

    private void removeAnyBreadcrumb(final UiObjectWkt objectModel) {
        getBreadcrumbModel()
        .ifPresent(breadcrumbModel->breadcrumbModel.remove(objectModel));
    }

}
