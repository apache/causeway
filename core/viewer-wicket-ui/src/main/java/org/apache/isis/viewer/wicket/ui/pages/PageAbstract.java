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

package org.apache.isis.viewer.wicket.ui.pages;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.google.inject.name.Named;

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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.ConfigPropertyEnum;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.model.hints.IsisEnvelopeEvent;
import org.apache.isis.viewer.wicket.model.hints.IsisEventLetterAbstract;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.BookmarkableModel;
import org.apache.isis.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.DialogMode;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.actionprompt.ActionPromptModalWindow;
import org.apache.isis.viewer.wicket.ui.components.actionpromptsb.ActionPromptSidebar;
import org.apache.isis.viewer.wicket.ui.components.bookmarkedpages.BookmarkedPagesPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.favicon.Favicon;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlBehaviour;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.markup.html.references.BootlintHeaderItem;
import de.agilecoders.wicket.core.markup.html.references.BootstrapJavaScriptReference;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;
import de.agilecoders.wicket.core.settings.ITheme;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesomeCssReference;

/**
 * Convenience adapter for {@link WebPage}s built up using {@link ComponentType}s.
 */
public abstract class PageAbstract extends WebPage implements ActionPromptProvider {

    private static Logger LOG = LoggerFactory.getLogger(PageAbstract.class);

    private static final long serialVersionUID = 1L;
    
    public static final ConfigPropertyEnum<DialogMode> CONFIG_DIALOG_MODE =
            new ConfigPropertyEnum<>("isis.viewer.wicket.dialogMode", DialogMode.SIDEBAR);

    /**
     * @see <a href="http://github.com/brandonaaron/livequery">livequery</a>
     */
    private static final JavaScriptResourceReference JQUERY_LIVEQUERY_JS = new JavaScriptResourceReference(PageAbstract.class, "jquery.livequery.js");
    private static final JavaScriptResourceReference JQUERY_ISIS_WICKET_VIEWER_JS = new JavaScriptResourceReference(PageAbstract.class, "jquery.isis.wicket.viewer.js");

    private static final String LIVE_RELOAD_URL_KEY = "isis.viewer.wicket.liveReloadUrl";

    // not to be confused with the bootstrap theme...
    // is simply a CSS class derived from the application's name
    private static final String ID_THEME = "theme";

    private static final String ID_BOOKMARKED_PAGES = "bookmarks";

    private static final String ID_ACTION_PROMPT_MODAL_WINDOW = "actionPromptModalWindow";
    private static final String ID_ACTION_PROMPT_SIDEBAR = "actionPromptSidebar";

    private static final String ID_PAGE_TITLE = "pageTitle";

    private static final String ID_FAVICON = "favicon";

    public static final String ID_MENU_LINK = "menuLink";

    public static final String UIHINT_FOCUS = "focus";

    /**
     * This is a bit hacky, but best way I've found to pass an exception over to the WicketSignInPage
     * if there is a problem rendering this page.
     */
    public static ThreadLocal<ExceptionModel> EXCEPTION = new ThreadLocal<>();

    private final List<ComponentType> childComponentIds;

    @com.google.inject.Inject
    @Named("applicationName")
    private String applicationName;

    @com.google.inject.Inject(optional = true)
    @Named("applicationCss")
    private String applicationCss;
    
    @com.google.inject.Inject(optional = true)
    @Named("applicationJs")
    private String applicationJs;

    @com.google.inject.Inject
    private PageClassRegistry pageClassRegistry;


    /**
     * Top-level &lt;div&gt; to which all content is added.
     *
     * <p>
     *     Has <code>protected</code> visibility so that subclasses can also add directly to this div.
     * </p>
     */
    protected MarkupContainer themeDiv;

    public PageAbstract(
            final PageParameters pageParameters,
            final String title,
            final ComponentType... childComponentIds) {
        super(pageParameters);

        try {
            // for breadcrumbs support
            getSession().bind();
            
            setTitle(title);

            add(new Favicon(ID_FAVICON));

            themeDiv = new WebMarkupContainer(ID_THEME);
            add(themeDiv);
            if(applicationName != null) {
                themeDiv.add(new CssClassAppender(CssClassAppender.asCssStyle(applicationName)));
            }

            boolean devUtilitiesEnabled = getApplication().getDebugSettings().isDevelopmentUtilitiesEnabled();
            Component debugBar = devUtilitiesEnabled
                                        ? newDebugBar("debugBar")
                                        : new EmptyPanel("debugBar").setVisible(false);
            add(debugBar);

            MarkupContainer header = createPageHeader("header");
            themeDiv.add(header);

            MarkupContainer footer = createPageFooter("footer");
            themeDiv.add(footer);

            addActionPromptModalWindow(themeDiv);
            addActionPromptSidebar(themeDiv);

            this.childComponentIds = Collections.unmodifiableList(Arrays.asList(childComponentIds));

            // ensure that all collected JavaScript contributions are loaded at the page footer
            add(new HeaderResponseContainer("footerJS", "footerJS"));

        } catch(final RuntimeException ex) {

            LOG.error("Failed to construct page, going back to sign in page", ex);
            
            // REVIEW: similar code in WebRequestCycleForIsis
            final  List<ExceptionRecognizer> exceptionRecognizers = getServicesInjector().lookupServices(ExceptionRecognizer.class);
            final String recognizedMessageIfAny = new ExceptionRecognizerComposite(exceptionRecognizers).recognize(ex);
            final ExceptionModel exceptionModel = ExceptionModel.create(recognizedMessageIfAny, ex);

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
            // https://issues.apache.org/jira/browse/ISIS-1622 raised to refactor and then reinstate this
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
        Component header = getComponentFactoryRegistry().createComponent(ComponentType.HEADER, id, null);
        return (MarkupContainer) header;
    }

    /**
     * Creates the component that should be used as a page header/navigation bar
     *
     * @param id The component id
     * @return The container that should be used as a page header/navigation bar
     */
    protected MarkupContainer createPageFooter(final String id) {
        Component footer = getComponentFactoryRegistry().createComponent(ComponentType.FOOTER, id, null);
        return (MarkupContainer) footer;
    }


    protected void setTitle(final String title) {
        addOrReplace(new Label(ID_PAGE_TITLE, title != null? title: applicationName));
    }

    private Class<? extends Page> getSignInPage() {
        return pageClassRegistry.getPageClass(PageType.SIGN_IN);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        
        super.renderHead(response);

        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference())));
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(BootstrapJavaScriptReference.instance())));

        response.render(CssHeaderItem.forReference(FontAwesomeCssReference.instance()));
        response.render(CssHeaderItem.forReference(new BootstrapOverridesCssResourceReference()));
        response.render(CssHeaderItem.forReference(new SidebarCssResourceReference()));
        contributeThemeSpecificOverrides(response);

        response.render(JavaScriptReferenceHeaderItem.forReference(JQUERY_LIVEQUERY_JS));
        response.render(JavaScriptReferenceHeaderItem.forReference(JQUERY_ISIS_WICKET_VIEWER_JS));

        final JGrowlBehaviour jGrowlBehaviour = new JGrowlBehaviour();
        jGrowlBehaviour.renderFeedbackMessages(response);

        if(applicationCss != null) {
            response.render(CssReferenceHeaderItem.forUrl(applicationCss));
        }
        if(applicationJs != null) {
            response.render(JavaScriptReferenceHeaderItem.forUrl(applicationJs));
        }

        String liveReloadUrl = getConfiguration().getString(LIVE_RELOAD_URL_KEY);
        if(liveReloadUrl != null) {
            response.render(JavaScriptReferenceHeaderItem.forUrl(liveReloadUrl));
        }
        if(isModernBrowser()) {
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
        String javaScript = markupId != null
            ? String.format("Wicket.Event.publish(Isis.Topic.FOCUS_FIRST_PROPERTY, '%s')", markupId)
            : "Wicket.Event.publish(Isis.Topic.FOCUS_FIRST_PROPERTY)";

        response.render(OnDomReadyHeaderItem.forScript(javaScript));

    }

    protected UiHintContainer getUiHintContainerIfAny() {
        return null;
    }

    private void addBootLint(final IHeaderResponse response) {
        // rather than using the default BootlintHeaderItem.INSTANCE;
        // this allows us to assign 'form-control' class to an <a> (for x-editable styling)
        response.render(new BootlintHeaderItem("bootlint.showLintReportForCurrentDocument(['E042'], {'problemFree': false});"));
    }

    private boolean isModernBrowser() {
        return !isIePre9();
    }

    private boolean isIePre9() {
        final WebClientInfo clientInfo = WebSession.get().getClientInfo();
        final ClientProperties properties = clientInfo.getProperties();
        if (properties.isBrowserInternetExplorer())
            if (properties.getBrowserVersionMajor() < 9)
                return true;
        return false;
    }

    /**
     * Contributes theme specific Bootstrap CSS overrides if there is such resource
     *
     * @param response The header response to contribute to
     */
    private void contributeThemeSpecificOverrides(final IHeaderResponse response) {
        final IBootstrapSettings bootstrapSettings = Bootstrap.getSettings(getApplication());
        final ITheme activeTheme = bootstrapSettings.getActiveThemeProvider().getActiveTheme();
        final String name = activeTheme.name().toLowerCase(Locale.ENGLISH);
        final String themeSpecificOverride = "bootstrap-overrides-" + name + ".css";
        final ResourceReference.Key themeSpecificOverrideKey = new ResourceReference.Key(PageAbstract.class.getName(), themeSpecificOverride, null, null, null);
        if (PackageResource.exists(themeSpecificOverrideKey)) {
            response.render(CssHeaderItem.forReference(new CssResourceReference(themeSpecificOverrideKey)));
        }
    }

    /**
     * As provided in the {@link #PageAbstract(org.apache.wicket.request.mapper.parameter.PageParameters, String, org.apache.isis.viewer.wicket.ui.ComponentType...)} constructor}.
     * 
     * <p>
     * This superclass doesn't do anything with this property directly, but
     * requiring it to be provided enforces standardization of the
     * implementation of the subclasses.
     */
    public List<ComponentType> getChildModelTypes() {
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
        for (final ComponentType componentType : getChildModelTypes()) {
            addComponent(container, componentType, model);
        }
    }

    private void addComponent(final MarkupContainer container, final ComponentType componentType, final IModel<?> model) {
        getComponentFactoryRegistry().addOrReplaceComponent(container, componentType, model);
    }


    ////////////////////////////////////////////////////////////////
    // bookmarked pages
    ////////////////////////////////////////////////////////////////

    /**
     * Convenience for subclasses
     */
    protected void addBookmarkedPages(final MarkupContainer container) {
        boolean showBookmarks = isShowBookmarks();
        Component bookmarks = showBookmarks
                ? getComponentFactoryRegistry().createComponent(ComponentType.BOOKMARKED_PAGES, ID_BOOKMARKED_PAGES, getBookmarkedPagesModel())
                : new EmptyPanel(ID_BOOKMARKED_PAGES).setVisible(false);
        container.add(bookmarks);

        bookmarks.add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                super.onConfigure(component);

                PageParameters parameters = getPageParameters();
                component.setVisible(parameters.get(PageParametersUtils.ISIS_NO_HEADER_PARAMETER_NAME).isNull());
            }
        });
    }

    private boolean isShowBookmarks() {
        return getConfiguration() .getBoolean(
                        BookmarkedPagesPanel.SHOW_BOOKMARKS_KEY, BookmarkedPagesPanel.SHOW_BOOKMARKS_DEFAULT);
    }

    protected boolean isShowBreadcrumbs() {
        return getConfiguration() .getBoolean(
                        BreadcrumbPanel.SHOW_BREADCRUMBS_KEY, BreadcrumbPanel.SHOW_BREADCRUMBS_DEFAULT);
    }

    protected void bookmarkPageIfShown(final BookmarkableModel<?> model) {
        if(!isShowBookmarks()) {
            // no need...
            return;
        }
        getBookmarkedPagesModel().bookmarkPage(model);
    }

    protected void removeAnyBookmark(final EntityModel model) {
        getBookmarkedPagesModel().remove(model);
    }

    private BookmarkedPagesModel getBookmarkedPagesModel() {
        final BookmarkedPagesModelProvider session = (BookmarkedPagesModelProvider) getSession();
        return session.getBookmarkedPagesModel();
    }



    // ///////////////////////////////////////////////////////////////////
    // ActionPromptModalWindowProvider
    // ///////////////////////////////////////////////////////////////////
    
    private ActionPromptModalWindow actionPromptModalWindow;
    private ActionPromptSidebar actionPromptSidebar;

    public ActionPrompt getActionPrompt(final PromptStyle promptStyle) {
        switch (promptStyle) {
        case AS_CONFIGURED:
        case DIALOG:
        case INLINE:
        case INLINE_AS_IF_EDIT:
        default:
            final DialogMode dialogMode = CONFIG_DIALOG_MODE.from(getConfiguration());
            switch (dialogMode) {
                case SIDEBAR:
                    return actionPromptSidebar;
                case MODAL:
                default:
                    return actionPromptModalWindow;
            }
        case DIALOG_SIDEBAR:
            return actionPromptSidebar;
        case DIALOG_MODAL:
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
     * Propagates all {@link org.apache.isis.viewer.wicket.model.hints.IsisEventLetterAbstract letter} events down to
     * all child components, wrapped in an {@link org.apache.isis.viewer.wicket.model.hints.IsisEnvelopeEvent envelope} event.
     */
    public void onEvent(final org.apache.wicket.event.IEvent<?> event) {
        final Object payload = event.getPayload();
        if(payload instanceof IsisEventLetterAbstract) {
            final IsisEventLetterAbstract letter = (IsisEventLetterAbstract)payload;
            final IsisEnvelopeEvent broadcastEv = new IsisEnvelopeEvent(letter);
            send(this, Broadcast.BREADTH, broadcastEv);
        }
    }
    


    //region > getComponentFactoryRegistry (Convenience)
    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        final ComponentFactoryRegistryAccessor cfra = (ComponentFactoryRegistryAccessor) getApplication();
        return cfra.getComponentFactoryRegistry();
    }
    //endregion


    //region > injected (application-scope) 

    // REVIEW: can't inject because not serializable.
    protected IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

    protected IsisConfiguration getConfiguration() {
        return getIsisSessionFactory().getConfiguration();
    }

    protected ServicesInjector getServicesInjector() {
        return getIsisSessionFactory().getServicesInjector();
    }

    //endregion

    //region > derived from injected components (session-scope)

    protected PersistenceSession getPersistenceSession() {
        return getIsisSessionFactory().getCurrentSession().getPersistenceSession();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return getIsisSessionFactory().getCurrentSession().getAuthenticationSession();
    }

    //endregion


}
