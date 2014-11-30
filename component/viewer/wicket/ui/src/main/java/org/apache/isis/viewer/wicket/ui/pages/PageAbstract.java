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

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.markup.html.references.BootlintJavaScriptReference;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;
import de.agilecoders.wicket.core.settings.ITheme;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesomeCssReference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
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
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.hints.IsisEnvelopeEvent;
import org.apache.isis.viewer.wicket.model.hints.IsisEventLetterAbstract;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.ApplicationActionsModel;
import org.apache.isis.viewer.wicket.model.models.BookmarkableModel;
import org.apache.isis.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.actionprompt.ActionPromptModalWindow;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.themepicker.ThemeChooser;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlBehaviour;
import org.apache.isis.viewer.wicket.ui.pages.about.AboutPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * Convenience adapter for {@link WebPage}s built up using {@link ComponentType}s.
 */
public abstract class PageAbstract extends WebPage implements ActionPromptProvider {

    private static Logger LOG = LoggerFactory.getLogger(PageAbstract.class);

    private static final long serialVersionUID = 1L;

    /**
     * @see <a href="http://github.com/brandonaaron/livequery">livequery</a>
     */
    private static final JavaScriptResourceReference JQUERY_LIVEQUERY_JS = new JavaScriptResourceReference(PageAbstract.class, "jquery.livequery.js");
    private static final JavaScriptResourceReference JQUERY_ISIS_WICKET_VIEWER_JS = new JavaScriptResourceReference(PageAbstract.class, "jquery.isis.wicket.viewer.js");
    
    private static final String ID_THEME = "theme";
    private static final String ID_BOOKMARKED_PAGES = "bookmarks";

    private static final String ID_ACTION_PROMPT_MODAL_WINDOW = "actionPromptModalWindow";
    
    private static final String ID_PAGE_TITLE = "pageTitle";
    
    public static final String ID_MENU_LINK = "menuLink";

    private static final String ID_THEME_PICKER = "themePicker";
    private static final String ID_BREADCRUMBS = "breadcrumbs";


    /**
     * This is a bit hacky, but best way I've found to pass an exception over to the WicketSignInPage
     * if there is a problem rendering this page.
     */
    public static ThreadLocal<ExceptionModel> EXCEPTION = new ThreadLocal<ExceptionModel>(); 

    private final List<ComponentType> childComponentIds;
    private final PageParameters pageParameters;

    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @Inject
    @Named("applicationName")
    private String applicationName;
    
    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @Inject
    @Named("applicationCss")
    private String applicationCss;
    
    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @Inject
    @Named("applicationJs")
    private String applicationJs;

    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @Inject
    private PageClassRegistry pageClassRegistry;

    public PageAbstract(
            final PageParameters pageParameters,
            final String title,
            final ComponentType... childComponentIds) {
        try {
            // for breadcrumbs support
            getSession().bind();
            
            setTitle(title);
            
            themeDiv = new WebMarkupContainer(ID_THEME);
            add(themeDiv);
            if(applicationName != null) {
                themeDiv.add(new CssClassAppender(CssClassAppender.asCssStyle(applicationName)));
            }

            addApplicationActions(themeDiv);

            this.childComponentIds = Collections.unmodifiableList(Arrays.asList(childComponentIds));
            this.pageParameters = pageParameters;

            addApplicationName(themeDiv);
            addUserName(themeDiv);
            addLogoutLink(themeDiv);
            addAboutLink(themeDiv);
            addDevModeWarning(themeDiv);
            addBreadcrumbs();
            addThemePicker();

            // ensure that all collected JavaScript contributions are loaded at the page footer
            add(new HeaderResponseContainer("footerJS", "footerJS"));

        } catch(RuntimeException ex) {

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

    private void addApplicationName(MarkupContainer themeDiv) {
        BookmarkablePageLink<Void> applicationNameLink = homePageLink("applicationName");
        applicationNameLink.setBody(Model.of(applicationName));
        themeDiv.add(applicationNameLink);
    }

    private void addThemePicker() {
        ThemeChooser themeChooser = new ThemeChooser(ID_THEME_PICKER);
        themeDiv.addOrReplace(themeChooser);
    }


    protected void setTitle(final String title) {
        addOrReplace(new Label(ID_PAGE_TITLE, title != null? title: applicationName));
    }


    private Class<? extends Page> getSignInPage() {
        return pageClassRegistry.getPageClass(PageType.SIGN_IN);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        
        super.renderHead(response);

        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference())));
        response.render(CssHeaderItem.forReference(FontAwesomeCssReference.instance()));
        response.render(CssHeaderItem.forReference(new BootstrapOverridesCssResourceReference()));
        contributeThemeSpecificOverrides(response);
        PanelUtil.renderHead(response, PageAbstract.class);

        response.render(JavaScriptReferenceHeaderItem.forReference(JQUERY_LIVEQUERY_JS));
        response.render(JavaScriptReferenceHeaderItem.forReference(JQUERY_ISIS_WICKET_VIEWER_JS));

        JGrowlBehaviour jGrowlBehaviour = new JGrowlBehaviour();
        jGrowlBehaviour.renderFeedbackMessages(response);

        if(applicationCss != null) {
            response.render(CssReferenceHeaderItem.forUrl(applicationCss));
        }
        if(applicationJs != null) {
            response.render(JavaScriptReferenceHeaderItem.forUrl(applicationJs));
        }

        // TODO mgrigorov Remove before merge to master
        WebClientInfo clientInfo = WebSession.get().getClientInfo();
        ClientProperties properties = clientInfo.getProperties();
        if (!(properties.isBrowserInternetExplorer() && properties.getBrowserVersionMajor() < 9)) {
            // use BootLint for any browser but IE 6-8
            response.render(JavaScriptHeaderItem.forReference(BootlintJavaScriptReference.INSTANCE));
        }
    }

    /**
     * Contributes theme specific Bootstrap CSS overrides if there is such resource
     *
     * @param response The header response to contribute to
     */
    private void contributeThemeSpecificOverrides(IHeaderResponse response) {
        IBootstrapSettings bootstrapSettings = Bootstrap.getSettings(getApplication());
        ITheme activeTheme = bootstrapSettings.getActiveThemeProvider().getActiveTheme();
        String name = activeTheme.name().toLowerCase(Locale.ENGLISH);
        String themeSpecificOverride = "bootstrap-overrides-" + name + ".css";
        ResourceReference.Key themeSpecificOverrideKey = new ResourceReference.Key(PageAbstract.class.getName(), themeSpecificOverride, null, null, null);
        if (PackageResource.exists(themeSpecificOverrideKey)) {
            response.render(CssHeaderItem.forReference(new CssResourceReference(themeSpecificOverrideKey)));
        }
    }

    private void addUserName(MarkupContainer themeDiv) {
        Label userName = new Label("userName", getAuthenticationSession().getUserName());
        themeDiv.add(userName);
    }

    private void addLogoutLink(MarkupContainer themeDiv) {
        Link logoutLink = new Link("logoutLink") {

            @Override
            public void onClick() {
                getSession().invalidate();
                setResponsePage(getSignInPage());
            }
        };
        themeDiv.add(logoutLink);
    }

    private void addAboutLink(MarkupContainer themeDiv) {
        BookmarkablePageLink<Void> aboutLink = new BookmarkablePageLink<>("aboutLink", AboutPage.class);
        aboutLink.setBody(new ResourceModel("aboutLabel"));
        themeDiv.add(aboutLink);
    }

    /**
     * Adds a component that shows a warning sign next to "Powered by: Apache Isis" in development mode
     * @param themeDiv The parent component
     */
    private void addDevModeWarning(MarkupContainer themeDiv) {
        WebComponent devModeWarning = new WebComponent("devModeWarning");
        devModeWarning.setVisible(getApplication().usesDevelopmentConfig());
        themeDiv.add(devModeWarning);
    }

    private void addBreadcrumbs() {
        BreadcrumbPanel breadcrumbPanel = new BreadcrumbPanel(ID_BREADCRUMBS);
        themeDiv.addOrReplace(breadcrumbPanel);
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

    @Override
    public PageParameters getPageParameters() {
        return pageParameters;
    }

    private void addApplicationActions(MarkupContainer container) {
        addActionPromptModalWindow();
        final ApplicationActionsModel model = new ApplicationActionsModel();
        model.setActionPromptProvider(this);
        addComponent(container, ComponentType.APPLICATION_ACTIONS, model);
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
    protected void addChildComponents(MarkupContainer container, final IModel<?> model) {
        for (final ComponentType componentType : getChildModelTypes()) {
            addComponent(container, componentType, model);
        }
    }

    private void addComponent(MarkupContainer container, final ComponentType componentType, final IModel<?> model) {
        getComponentFactoryRegistry().addOrReplaceComponent(container, componentType, model);
    }


    ////////////////////////////////////////////////////////////////
    // bookmarked pages
    ////////////////////////////////////////////////////////////////

    /**
     * Convenience for subclasses
     */
    protected void addBookmarkedPages() {
        getComponentFactoryRegistry().addOrReplaceComponent(themeDiv, ID_BOOKMARKED_PAGES, ComponentType.BOOKMARKED_PAGES, getBookmarkedPagesModel());
    }

    protected void bookmarkPage(BookmarkableModel<?> model) {
        getBookmarkedPagesModel().bookmarkPage(model);
    }

    protected void removeAnyBookmark(EntityModel model) {
        getBookmarkedPagesModel().remove(model);
    }

    private BookmarkedPagesModel getBookmarkedPagesModel() {
        BookmarkedPagesModelProvider session = (BookmarkedPagesModelProvider) getSession();
        return session.getBookmarkedPagesModel();
    }



    // ///////////////////////////////////////////////////////////////////
    // ActionPromptModalWindowProvider
    // ///////////////////////////////////////////////////////////////////
    
    private ActionPromptModalWindow actionPromptModalWindow;

    protected MarkupContainer themeDiv;
    public ActionPrompt getActionPrompt() {
        return ActionPromptModalWindow.getActionPromptModalWindowIfEnabled(actionPromptModalWindow);
    }

    private void addActionPromptModalWindow() {
        actionPromptModalWindow = ActionPromptModalWindow.newModalWindow(ID_ACTION_PROMPT_MODAL_WINDOW); 
        themeDiv.addOrReplace(actionPromptModalWindow);
    }

    
    // ///////////////////////////////////////////////////////////////////
    // UI Hint
    // ///////////////////////////////////////////////////////////////////

    /**
     * Propagates all {@link org.apache.isis.viewer.wicket.model.hints.IsisEventLetterAbstract letter} events down to
     * all child components, wrapped in an {@link org.apache.isis.viewer.wicket.model.hints.IsisEnvelopeEvent envelope} event.
     */
    public void onEvent(org.apache.wicket.event.IEvent<?> event) {
        Object payload = event.getPayload();
        if(payload instanceof IsisEventLetterAbstract) {
            IsisEventLetterAbstract letter = (IsisEventLetterAbstract)payload;
            IsisEnvelopeEvent broadcastEv = new IsisEnvelopeEvent(letter);
            send(this, Broadcast.BREADTH, broadcastEv);
        }
    }
    

    // ///////////////////////////////////////////////////////////////////
    // Convenience
    // ///////////////////////////////////////////////////////////////////

    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        final ComponentFactoryRegistryAccessor cfra = (ComponentFactoryRegistryAccessor) getApplication();
        return cfra.getComponentFactoryRegistry();
    }

    // ///////////////////////////////////////////////////
    // System components
    // ///////////////////////////////////////////////////

    protected ServicesInjectorSpi getServicesInjector() {
        return getPersistenceSession().getServicesInjector();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }
    
    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }
    
    protected MessageBroker getMessageBroker() {
        return IsisContext.getMessageBroker();
    }
    
    protected IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }



}
