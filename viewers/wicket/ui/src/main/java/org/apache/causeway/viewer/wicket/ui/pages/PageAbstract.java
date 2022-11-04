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
package org.apache.causeway.viewer.wicket.ui.pages;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.devutils.debugbar.IDebugBarContributor;
import org.apache.wicket.devutils.debugbar.InspectorDebugPanel;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.services.exceprecog.ExceptionRecognizerService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Timing;
import org.apache.causeway.commons.internal.debug._Debug;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.hints.CausewayEnvelopeEvent;
import org.apache.causeway.viewer.wicket.model.hints.CausewayEventLetterAbstract;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.models.ActionPrompt;
import org.apache.causeway.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.causeway.viewer.wicket.model.models.BookmarkableModel;
import org.apache.causeway.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.app.registry.HasComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.components.actionprompt.ActionPromptModalWindow;
import org.apache.causeway.viewer.wicket.ui.components.actionpromptsb.ActionPromptSidebar;
import org.apache.causeway.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModel;
import org.apache.causeway.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModelProvider;
import org.apache.causeway.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.causeway.viewer.wicket.ui.errors.JGrowlBehaviour;
import org.apache.causeway.viewer.wicket.ui.pages.common.bootstrap.css.BootstrapOverridesCssResourceReference;
import org.apache.causeway.viewer.wicket.ui.pages.common.datatables.DatatablesCssBootstrap5ReferenceWkt;
import org.apache.causeway.viewer.wicket.ui.pages.common.datatables.DatatablesCssReferenceWkt;
import org.apache.causeway.viewer.wicket.ui.pages.common.datatables.DatatablesJavaScriptBootstrap5ReferenceWkt;
import org.apache.causeway.viewer.wicket.ui.pages.common.datatables.DatatablesJavaScriptReferenceWkt;
import org.apache.causeway.viewer.wicket.ui.pages.common.datatables.DatatablesJavaScriptResourceReferenceInit;
import org.apache.causeway.viewer.wicket.ui.pages.common.fontawesome.FontAwesomeCssReferenceWkt;
import org.apache.causeway.viewer.wicket.ui.pages.common.livequery.js.LiveQueryJsResourceReference;
import org.apache.causeway.viewer.wicket.ui.pages.common.sidebar.css.SidebarCssResourceReference;
import org.apache.causeway.viewer.wicket.ui.pages.common.viewer.js.CausewayWicketViewerJsResourceReference;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.Wkt.EventTopic;

import de.agilecoders.wicket.core.markup.html.references.BootstrapJavaScriptReference;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Convenience adapter for {@link WebPage}s built up using {@link UiComponentType}s.
 */
@Log4j2
public abstract class PageAbstract
extends WebPageBase
implements ActionPromptProvider {

    private static final long serialVersionUID = 1L;

        // not to be confused with the bootstrap theme...
    // is simply a CSS class derived from the application's name
    private static final String ID_THEME = "theme";
    private static final String ID_BOOKMARKED_PAGES = "bookmarks";
    private static final String ID_ACTION_PROMPT_MODAL_WINDOW = "actionPromptModalWindow";
    private static final String ID_ACTION_PROMPT_SIDEBAR = "actionPromptSidebar";
    private static final String ID_PAGE_TITLE = "pageTitle";
    public static final String ID_MENU_LINK = "menuLink";
    public static final String UIHINT_FOCUS = "focus";

    /**
     * This is a bit hacky, but best way I've found to pass an exception over to the WicketSignInPage
     * if there is a problem rendering this page.
     */
    public static final ThreadLocal<ExceptionModel> EXCEPTION = new ThreadLocal<>();

    private final List<UiComponentType> childComponentIds;

    /**
     * Top-level &lt;div&gt; to which all content is added.
     *
     * <p>
     *     Has <code>protected</code> visibility so that subclasses can also add directly to this div.
     * </p>
     */
    protected MarkupContainer themeDiv;

    protected PageAbstract(
            final PageParameters pageParameters,
            final String title,
            final UiComponentType... childComponentIds) {

        super(pageParameters);

        try {

            // for breadcrumbs support
            getSession().bind();

            setTitle(title);

            // must be a direct child of <body> for the 'sticky-top' CSS class to work
            MarkupContainer header = createPageHeader("header");
            add(header);

            themeDiv = Wkt.containerAdd(this, ID_THEME);

            String applicationName = getApplicationSettings().getName();
            Wkt.cssAppend(themeDiv, Wkt.cssNormalize(applicationName));

            boolean devUtilitiesEnabled = getApplication().getDebugSettings().isDevelopmentUtilitiesEnabled();
            Component debugBar = devUtilitiesEnabled
                    ? newDebugBar("debugBar")
                    : new EmptyPanel("debugBar").setVisible(false);
            add(debugBar);

            MarkupContainer footer = createPageFooter("footer");
            themeDiv.add(footer);

            addActionPromptModalWindow(themeDiv);
            addActionPromptSidebar(themeDiv);

            this.childComponentIds = List.of(childComponentIds);

            // ensure that all collected JavaScript contributions are loaded at the page footer
            add(new HeaderResponseContainer("footerJS", "footerJS"));

        } catch(final RuntimeException ex) {

            log.error("Failed to construct page, going back to sign in page", ex);

            val exceptionRecognizerService = getMetaModelContext().getServiceRegistry()
                    .lookupServiceElseFail(ExceptionRecognizerService.class);

            val recognition = exceptionRecognizerService.recognize(ex);

            val exceptionModel = ExceptionModel.create(getMetaModelContext(), recognition, ex);

            getSession().invalidate();
            getSession().clear();

            // for the WicketSignInPage to render
            EXCEPTION.set(exceptionModel);

            throw new RestartResponseAtInterceptPageException(getSignInPage());
        }
    }

    protected DebugBar newDebugBar(final String id) {
        final DebugBar debugBar = new DebugBar(id);
        final List<IDebugBarContributor> contributors = DebugBar.getContributors(getApplication());
        for (Iterator<IDebugBarContributor> iterator = contributors.iterator(); iterator.hasNext(); ) {
            final IDebugBarContributor contributor = iterator.next();
            // the InspectorDebug invokes load on every model found.
            // for ActionModels this has the rather unfortunate effect of invoking them!
            // https://issues.apache.org/jira/browse/CAUSEWAY-1622 raised to refactor and then reinstate this
            if(contributor == InspectorDebugPanel.DEBUG_BAR_CONTRIB) {
                iterator.remove();
            }
        }
        return debugBar;
    }


    /**
     * Creates the component that should be used as a page header/navigation bar
     *
     * @param id The component id
     * @return The container that should be used as a page header/navigation bar
     */
    protected MarkupContainer createPageHeader(final String id) {
        Component header = getComponentFactoryRegistry().createComponent(id, UiComponentType.HEADER, null);
        return (MarkupContainer) header;
    }

    /**
     * Creates the component that should be used as a page header/navigation bar
     *
     * @param id The component id
     * @return The container that should be used as a page header/navigation bar
     */
    protected MarkupContainer createPageFooter(final String id) {
        Component footer = getComponentFactoryRegistry().createComponent(id, UiComponentType.FOOTER, null);
        return (MarkupContainer) footer;
    }


    protected void setTitle(final String title) {
        Wkt.labelAdd(this, ID_PAGE_TITLE, title != null
                ? title
                : getApplicationSettings().getName());
    }

    private Class<? extends Page> getSignInPage() {
        return getPageClassRegistry().getPageClass(PageType.SIGN_IN);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {

        super.renderHead(response);

        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference())));
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(BootstrapJavaScriptReference.instance())));
        response.render(FontAwesomeCssReferenceWkt.asHeaderItem());

        response.render(DatatablesJavaScriptReferenceWkt.asHeaderItem());
        response.render(DatatablesJavaScriptBootstrap5ReferenceWkt.asHeaderItem());
        response.render(DatatablesCssReferenceWkt.asHeaderItem());
        response.render(DatatablesCssBootstrap5ReferenceWkt.asHeaderItem());
        response.render(DatatablesJavaScriptResourceReferenceInit.instance(getConfiguration()));

        response.render(BootstrapOverridesCssResourceReference.asHeaderItem());
        BootstrapOverridesCssResourceReference
            .contributeThemeSpecificOverrides(getApplication(), response);

        response.render(SidebarCssResourceReference.asHeaderItem());

        response.render(LiveQueryJsResourceReference.asHeaderItem());
        response.render(CausewayWicketViewerJsResourceReference.asHeaderItem());

        new JGrowlBehaviour(getMetaModelContext())
            .renderFeedbackMessages(response);

        getWicketViewerSettings().getCss()
        .ifPresent(applicationCss -> {
            response.render(CssReferenceHeaderItem.forUrl(applicationCss));
        });

        getWicketViewerSettings().getJs()
        .ifPresent(applicationJs -> {
            response.render(JavaScriptReferenceHeaderItem.forUrl(applicationJs));
        });

        getWicketViewerSettings().getLiveReloadUrl()
        .ifPresent(liveReloadUrl -> {
            response.render(JavaScriptReferenceHeaderItem.forUrl(liveReloadUrl));
        });

        if(getSystemEnvironment().isPrototyping()) {
            addBootLint(response);
        }

        String markupId = null;
        UiHintContainer hintContainer = getUiHintContainerIfAny();
        if(hintContainer != null) {
            String path = hintContainer.getHint(getPage(), PageAbstract.UIHINT_FOCUS);
            if(path != null) {
                Component childComponent = get(path);
                if(childComponent != null) {
                    markupId = childComponent.getMarkupId();
                }
            }
        }

        Wkt.javaScriptAdd(response, EventTopic.FOCUS_FIRST_PROPERTY, markupId);
    }

    protected UiHintContainer getUiHintContainerIfAny() {
        return null;
    }

    /**
     * BootLint checks for malformed bootstrap CSS. It is probably only needed in PROTOTYPE mode.
     */
    private void addBootLint(final IHeaderResponse response) {
        // rather than using the default BootlintHeaderItem.INSTANCE;
        // this allows us to assign 'form-control' class to an <a> (for x-editable styling)

    	// Bootlint not available for BS4 (as for now)
    	/*
        response.render(new BootlintHeaderItem(
                "bootlint.showLintReportForCurrentDocument(['E042'], {'problemFree': false});"));
                */
    }

    /**
     * As provided in the {@link #PageAbstract(org.apache.wicket.request.mapper.parameter.PageParameters, String, org.apache.causeway.viewer.commons.model.components.UiComponentType...)} constructor}.
     *
     * <p>
     * This superclass doesn't do anything with this property directly, but
     * requiring it to be provided enforces standardization of the
     * implementation of the subclasses.
     */
    public List<UiComponentType> getChildModelTypes() {
        return childComponentIds;
    }

    /**
     * For subclasses to call.
     *
     * <p>
     * Should be called in the subclass' constructor.
     *
     * @param model
     *            - used to find the best matching {@link ComponentFactory} to
     *            render the model.
     */
    protected void addChildComponents(final MarkupContainer container, final IModel<?> model) {
        for (final UiComponentType uiComponentType : getChildModelTypes()) {
            addComponent(container, uiComponentType, model);
        }
    }

    private void addComponent(final MarkupContainer container, final UiComponentType uiComponentType, final IModel<?> model) {
        getComponentFactoryRegistry().addOrReplaceComponent(container, uiComponentType, model);
    }


    ////////////////////////////////////////////////////////////////
    // bookmarked pages
    ////////////////////////////////////////////////////////////////

    /**
     * Convenience for subclasses
     */
    protected void addBookmarkedPages(final MarkupContainer container) {

        final Component bookmarks = getBookmarkedPagesModel()
            .map(bm->getComponentFactoryRegistry()
                            .createComponent(ID_BOOKMARKED_PAGES, UiComponentType.BOOKMARKED_PAGES, bm))
            .orElseGet(()->new EmptyPanel(ID_BOOKMARKED_PAGES).setVisible(false));

        container.add(bookmarks);

        bookmarks.add(new Behavior() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onConfigure(final Component component) {
                super.onConfigure(component);

                PageParameters parameters = getPageParameters();
                component.setVisible(parameters.get(PageParameterUtils.CAUSEWAY_NO_HEADER_PARAMETER_NAME).isNull());
            }
        });
    }

    private boolean isShowBookmarks() {
        return getWicketViewerSettings().getBookmarkedPages().isShowChooser();
    }

    protected boolean isShowBreadcrumbs() {
        return getWicketViewerSettings().getBookmarkedPages().isShowDropDownOnFooter();
    }

    protected void bookmarkPageIfShown(final BookmarkableModel model) {
        getBookmarkedPagesModel()
        .ifPresent(bm->bm.bookmarkPage(model));
    }

    protected void removeAnyBookmark(final UiObjectWkt model) {
        getBookmarkedPagesModel()
        .ifPresent(bm->bm.remove(model));
    }

    private Optional<BookmarkedPagesModel> getBookmarkedPagesModel() {
        return isShowBookmarks()
                ? _Casts.castTo(BookmarkedPagesModelProvider.class, getSession())
                        .map(BookmarkedPagesModelProvider::getBookmarkedPagesModel)
                : Optional.empty();
    }

    protected Optional<BreadcrumbModel> getBreadcrumbModel() {
        return isShowBreadcrumbs()
                ? _Casts.castTo(BreadcrumbModelProvider.class, getSession())
                        .map(BreadcrumbModelProvider::getBreadcrumbModel)
                : Optional.empty();
    }



    // ///////////////////////////////////////////////////////////////////
    // ActionPromptModalWindowProvider
    // ///////////////////////////////////////////////////////////////////

    private ActionPromptModalWindow actionPromptModalWindow;
    private ActionPromptSidebar actionPromptSidebar;

    @Override
    public ActionPrompt getActionPrompt(
            final PromptStyle promptStyle,
            final BeanSort sort) {

        switch (promptStyle) {
        case DIALOG_SIDEBAR:
            return actionPromptSidebar;
        case DIALOG_MODAL:
            return actionPromptModalWindow;
        default:
            // fall through
        }

        val dialogMode =
                sort.isManagedBeanAny()
                        ? getWicketViewerSettings().getDialogModeForMenu()
                        : getWicketViewerSettings().getDialogMode();
        switch (dialogMode) {
        case SIDEBAR:
            return actionPromptSidebar;
        case MODAL:
        default:
            return actionPromptModalWindow;
        }
    }

    @Override
    public void closePrompt(final AjaxRequestTarget target) {
        actionPromptSidebar.closePrompt(target);
        actionPromptModalWindow.closePrompt(target);
    }

    private void addActionPromptModalWindow(final MarkupContainer parent) {
        actionPromptModalWindow = ActionPromptModalWindow.newModalWindow(ID_ACTION_PROMPT_MODAL_WINDOW);
        parent.addOrReplace(actionPromptModalWindow);
    }

    private void addActionPromptSidebar(final MarkupContainer parent) {
        actionPromptSidebar = ActionPromptSidebar.newSidebar(ID_ACTION_PROMPT_SIDEBAR);
        parent.addOrReplace(actionPromptSidebar);
    }


    // ///////////////////////////////////////////////////////////////////
    // UI Hint
    // ///////////////////////////////////////////////////////////////////

    /**
     * Propagates all {@link org.apache.causeway.viewer.wicket.model.hints.CausewayEventLetterAbstract letter} events down to
     * all child components, wrapped in an {@link org.apache.causeway.viewer.wicket.model.hints.CausewayEnvelopeEvent envelope} event.
     */
    @Override
    public void onEvent(final org.apache.wicket.event.IEvent<?> event) {
        _Casts.castTo(CausewayEventLetterAbstract.class, event.getPayload())
        .ifPresent(letter->
            send(PageAbstract.this, Broadcast.BREADTH, new CausewayEnvelopeEvent(letter)));
    }

    // -- getComponentFactoryRegistry (Convenience)
    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        final HasComponentFactoryRegistry cfra = (HasComponentFactoryRegistry) getApplication();
        return cfra.getComponentFactoryRegistry();
    }


    // -- RE-ATTACH ENTITIES

    @Override
    public void renderPage() {
        if(XrayUi.isXrayEnabled()){
            _Debug.log("about to render %s ..", this.getClass().getSimpleName());
            val stopWatch = _Timing.now();
            onNewRequestCycle();
            super.renderPage();
            stopWatch.stop();
            _Debug.log(".. rendering took %s", stopWatch.toString());
        } else {
            onNewRequestCycle();
            super.renderPage();
        }
    }

    /**
     * Hook to re-fetch entities for view-models, usually required once at begin of request.
     * @apiNote ideally we would not need that hook at all;
     * this is a hack that came after re-designing the entity-model
     */
    public void onNewRequestCycle() {
        // implemented only by EntityPage
    }

}
