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
package org.apache.isis.viewer.wicket.viewer.wicketapp;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.wicket.Application;
import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.authentication.strategy.DefaultAuthenticationStrategy;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.devutils.debugbar.DebugBarInitializer;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.PageRequestHandlerTracker;
import org.apache.wicket.request.cycle.RequestCycleListenerCollection;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.settings.RequestCycleSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import org.apache.isis.commons.internal.concurrent._ConcurrentContext;
import org.apache.isis.commons.internal.concurrent._ConcurrentTaskList;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.wicket.model.isis.WicketApplicationInitializer;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettingsAccessor;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.string.MultiLineStringPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.themepicker.IsisWicketThemeSupport;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountConfirmationMap;
import org.apache.isis.viewer.wicket.ui.pages.login.WicketLogoutPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;
import org.apache.isis.viewer.wicket.viewer.integration.AuthenticatedWebSessionForIsis;
import org.apache.isis.viewer.wicket.viewer.integration.ConverterForObjectAdapter;
import org.apache.isis.viewer.wicket.viewer.integration.ConverterForObjectAdapterMemento;
import org.apache.isis.viewer.wicket.viewer.integration.IsisResourceSettings;
import org.apache.isis.viewer.wicket.viewer.integration.WebRequestCycleForIsis;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;

/**
 * Main application, subclassing the Wicket {@link Application} and
 * bootstrapping Isis.
 *
 * <p>
 * Its main responsibility is to allow the set of {@link ComponentFactory}s used
 * to render the domain objects to be registered. This type of customization is
 * common place. At a more fundamental level, also allows the {@link Page}
 * implementation for each {@link PageType page type} to be overridden. This is
 * probably less common, because CSS can also be used for this purpose.
 *
 * <p>
 * New {@link ComponentFactory}s can be specified in two ways. The preferred
 * approach is to have the IoC container discover the ComponentFactory.
 * See <tt>asciidoc</tt> extension for an example of this.
 *
 */
@Log4j2
public class IsisWicketApplication
extends AuthenticatedWebApplication
implements
    ComponentFactoryRegistryAccessor,
    PageClassRegistryAccessor,
    WicketViewerSettingsAccessor,
    HasCommonContext {

    private static final long serialVersionUID = 1L;

    /**
     * Convenience locator, down-casts inherited functionality.
     */
    public static IsisWicketApplication get() {
        return (IsisWicketApplication) AuthenticatedWebApplication.get();
    }

    @Inject private MetaModelContext metaModelContext;
    @Inject private List<WicketApplicationInitializer> applicationInitializers;

    @Getter(onMethod = @__(@Override)) private IsisAppCommonContext commonContext; // shared

    // injected manually
    @Getter(onMethod = @__(@Override)) private ComponentFactoryRegistry componentFactoryRegistry;
    @Getter(onMethod = @__(@Override)) private PageClassRegistry pageClassRegistry;
    @Getter(onMethod = @__(@Override)) private WicketViewerSettings settings;
    private IsisSystemEnvironment systemEnvironment;
    private IsisConfiguration configuration;

    private final _Experimental experimental;

    // -- CONSTRUCTION

    public IsisWicketApplication() {
        experimental = new _Experimental(this);
    }

    /**
     * Although there are warnings about not overriding this method, it doesn't seem possible
     * to call {@link #setResourceSettings(org.apache.wicket.settings.ResourceSettings)} in the
     * {@link #init()} method.
     */
    @Override
    protected void internalInit() {
        // replace with custom implementation of ResourceSettings that changes the order
        // in which search for i18n properties, to search for the application-specific
        // settings before any other.
        setResourceSettings(new IsisResourceSettings(this));

        super.internalInit();

        // intercept AJAX requests and reload view-models so any detached entities are re-fetched
        IsisWicketAjaxRequestListenerUtil.setRootRequestMapper(this, commonContext);
    }

    private AjaxRequestTarget decorate(final AjaxRequestTarget ajaxRequestTarget) {
        ajaxRequestTarget.registerRespondListener(
                commonContext.injectServicesInto(
                        new TargetRespondListenerToResetQueryResultCache() ));
        return ajaxRequestTarget;
    }

    @Override
    public Application setAjaxRequestTargetProvider(final Function<Page, AjaxRequestTarget> ajaxRequestTargetProvider) {
        final Application application = super.setAjaxRequestTargetProvider(
                (final Page context) -> decorate(ajaxRequestTargetProvider.apply(context)) );
        return application;
    }

    /**
     * Initializes the application; in particular, bootstrapping the Isis
     * backend, and initializing the {@link ComponentFactoryRegistry} to be used
     * for rendering.
     */
    @Override
    protected void init() {
        super.init();

        getCspSettings().blocking().disabled(); // since Wicket 9, CSP is enabled by default [https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP]

        // Initialize Spring Dependency Injection (into Wicket components)
        val springInjector = new SpringComponentInjector(this);
        Injector.get().inject(this);
        getComponentInstantiationListeners().add(springInjector);

        // bootstrap dependencies from the metaModelContext
        {
            Objects.requireNonNull(metaModelContext, "metaModelContext");

            commonContext = IsisAppCommonContext.of(metaModelContext);
            configuration = commonContext.lookupServiceElseFail(IsisConfiguration.class);
            componentFactoryRegistry = commonContext.lookupServiceElseFail(ComponentFactoryRegistry.class);
            pageClassRegistry = commonContext.lookupServiceElseFail(PageClassRegistry.class);
            settings = commonContext.lookupServiceElseFail(WicketViewerSettings.class);
            systemEnvironment = commonContext.lookupServiceElseFail(IsisSystemEnvironment.class);
        }

        // gather configuration plugins into a list of named tasks
        val initializationTasks =
                _ConcurrentTaskList.named("Isis Application Initialization Tasks");
        applicationInitializers
            .forEach(initializer->initializationTasks
                    .addRunnable(String
                            .format("Configure %s",
                                    initializer.getClass().getSimpleName()),
                            ()->initializer.init(this)));

        try {

            initializationTasks.submit(_ConcurrentContext.sequential());

            getRequestCycleSettings().setRenderStrategy(RequestCycleSettings.RenderStrategy.REDIRECT_TO_RENDER);
            getResourceSettings().setParentFolderPlaceholder("$up$");

            RequestCycleListenerCollection requestCycleListeners = getRequestCycleListeners();
            IRequestCycleListener requestCycleListenerForIsis = newWebRequestCycleForIsis();
            requestCycleListeners.add(requestCycleListenerForIsis);
            requestCycleListeners.add(new PageRequestHandlerTracker());

            if (requestCycleListenerForIsis instanceof WebRequestCycleForIsis) {
                WebRequestCycleForIsis webRequestCycleForIsis = (WebRequestCycleForIsis) requestCycleListenerForIsis;
                webRequestCycleForIsis.setPageClassRegistry(pageClassRegistry);
            }

            this.getMarkupSettings().setStripWicketTags(configuration.getViewer().getWicket().isStripWicketTags());

            configureSecurity(configuration);

            getDebugSettings().setAjaxDebugModeEnabled(configuration.getViewer().getWicket().isAjaxDebugMode());

            // must be done after injected componentFactoryRegistry into the app itself
            buildCssBundle();

            filterJavascriptContributions();

            // TODO ISIS-987 Either make the API better (no direct access to the map) or use DB records
            int maxEntries = 1000;
            setMetaData(AccountConfirmationMap.KEY, new AccountConfirmationMap(maxEntries, Duration.ofDays(1)));

            mountPages();

            //  side-effects?
            //  SharedResources sharedResources = getSharedResources();

            if(systemEnvironment.isPrototyping()
                    && configuration.getViewer().getWicket().getDevelopmentUtilities().isEnable()) {

                new DebugBarInitializer().init(this);
            }

            log.debug("storeSettings.asynchronousQueueCapacity: {}", getStoreSettings().getAsynchronousQueueCapacity());
            log.debug("storeSettings.maxSizePerSession        : {}", getStoreSettings().getMaxSizePerSession());
            log.debug("storeSettings.fileStoreFolder          : {}", getStoreSettings().getFileStoreFolder());

            initializationTasks.await();

        } catch(RuntimeException ex) {
            // because Wicket's handling in its WicketFilter (that calls this method) does not log the exception.
            log.error("Failed to initialize", ex);
            throw ex;
        }

        commonContext.getServiceRegistry().select(IsisWicketThemeSupport.class)
        .getFirst()
        .ifPresent(themeSupport->{
            IBootstrapSettings settings = Bootstrap.getSettings();
            settings.setThemeProvider(themeSupport.getThemeProvider());
        });

        //XXX ISIS-2530, don't recreate expired pages
        getPageSettings().setRecreateBookmarkablePagesAfterExpiry(false);

    }

    /*
     * @since 2.0 ... overrides the default, to handle special cases when recreating bookmarked pages
     */
    @Override
    protected IPageFactory newPageFactory() {
        return new _PageFactory(this, super.newPageFactory());
    }

    /*
     * @since 2.0 ... overrides the default, to 'inject' the commonContext into new sessions
     */
    @Override
    public Session newSession(final Request request, final Response response) {
        val newSession = (AuthenticatedWebSessionForIsis) super.newSession(request, response);
        newSession.init(getCommonContext());
        return newSession;
    }

    /**
     * protected visibility to allow ad-hoc overriding of some other authentication strategy.
     */
    void configureSecurity(final IsisConfiguration configuration) {
        getSecuritySettings().setAuthenticationStrategy(newAuthenticationStrategy(configuration));
    }

    /**
     * protected visibility to allow ad-hoc overriding of some other authentication strategy.
     */
    IAuthenticationStrategy newAuthenticationStrategy(final IsisConfiguration configuration) {
        val rememberMe = configuration.getViewer().getWicket().getRememberMe();
        val cookieKey = rememberMe.getCookieKey();
        val encryptionKey = rememberMe.getEncryptionKey().orElse(defaultEncryptionKey());
        return new DefaultAuthenticationStrategy(cookieKey, _CryptFactory.sunJceCrypt(encryptionKey));
    }

    /**
     * As called by {@link #newAuthenticationStrategy(IsisConfiguration)}.
     * If an encryption key for the 'rememberMe' cookie hasn't been configured,
     * then use a different encryption key for the 'rememberMe'
     * cookie each time the app is restarted.
     */
    String defaultEncryptionKey() {
        return systemEnvironment.isPrototyping()
                ? _CryptFactory.FIXED_SALT_FOR_PROTOTYPING
                : UUID.randomUUID().toString();
    }

    // //////////////////////////////////////

    /**
     * Factored out for easy (informal) pluggability.
     */
    protected IRequestCycleListener newWebRequestCycleForIsis() {
        return new WebRequestCycleForIsis();
    }

    /**
     * Made protected visibility for easy (informal) pluggability.
     */
    protected void buildCssBundle() {
        experimental.buildCssBundle();
    }

    /**
     * Additional special cases to be included in the main CSS bundle.
     *
     * <p>
     * These are typically either superclasses or components that don't have their own ComponentFactory, or
     * for {@link ComponentFactory}s (such as <tt>StringPanelFactory</tt>) that don't quite follow the usual pattern
     * (because they create different types of panels).
     *
     * <p>
     * Note that it doesn't really matter if we miss one or two; their CSS will simply be served up individually.
     */
    protected void addSpecialCasesToCssBundle(final Set<CssResourceReference> references) {

        // abstract classes

        // ... though it turns out we cannot add this particular one to the bundle, because
        // it has CSS image links intended to be resolved relative to LinksSelectorPanelAbstract.class.
        // Adding them into the bundle would mean these CSS links are resolved relative to IsisWicketApplication.class
        // instead.
        // references.add(PanelUtil.cssResourceReferenceFor(LinksSelectorPanelAbstract.class));

        // components without factories
        references.add(PanelUtil.cssResourceReferenceFor(AdditionalLinksPanel.class));

        // non-conforming component factories
        references.add(PanelUtil.cssResourceReferenceFor(MultiLineStringPanel.class));
    }

    protected static final Function<ComponentFactory, Iterable<CssResourceReference>> getCssResourceReferences =
            (final ComponentFactory input) -> {
                final CssResourceReference cssResourceReference = input.getCssResourceReference();
                return cssResourceReference != null?
                        Collections.singletonList(cssResourceReference):
                            Collections.<CssResourceReference>emptyList();
            };

    // //////////////////////////////////////

    /**
     * filters JavaScript header contributions so rendered to bottom of page.
     *
     * <p>
     * Factored out for easy (informal) pluggability.
     * </p>
     */
    protected void filterJavascriptContributions() {
        setHeaderResponseDecorator(response -> {
            return new ResourceAggregator(new JavaScriptFilteredIntoFooterHeaderResponse(response, "footerJS"));
        });
    }

    // //////////////////////////////////////

    /**
     * Map entity and action to provide prettier URLs.
     *
     * <p>
     * Factored out for easy (informal) pluggability.
     * </p>
     */
    protected void mountPages() {
        mountPage("/signin", PageType.SIGN_IN);
        mountPage("/signup", PageType.SIGN_UP);
        mountPage("/signup/verify", PageType.SIGN_UP_VERIFY);
        mountPage("/password/reset", PageType.PASSWORD_RESET);

        mountPage("/entity/#{objectOid}", PageType.ENTITY);

        mountPage("/logout", WicketLogoutPage.class);
    }

    protected void mountPage(final String mountPath, final PageType pageType) {
        final Class<? extends Page> pageClass = this.pageClassRegistry.getPageClass(pageType);
        mount(new MountedMapper(mountPath, pageClass));
    }

    // //////////////////////////////////////

    @Override
    protected void onDestroy() {
        try {
            //            if (isisInteractionFactory != null) {
            //                isisInteractionFactory.destroyServicesAndShutdown();
            //            }
            super.onDestroy();
        } catch(final RuntimeException ex) {
            // symmetry with #init()
            log.error("Failed to destroy", ex);
            throw ex;
        }
    }

    // //////////////////////////////////////

    @Override //[ahuber] final on purpose! to switch DeploymentType, do this consistent with systemEnvironment
    public final RuntimeConfigurationType getConfigurationType() {

        if(systemEnvironment==null) {
            return RuntimeConfigurationType.DEPLOYMENT;
        }

        return systemEnvironment.isPrototyping()
                ? RuntimeConfigurationType.DEVELOPMENT
                : RuntimeConfigurationType.DEPLOYMENT;
    }

    // /////////////////////////////////////////////////
    // Wicket Hooks
    // /////////////////////////////////////////////////

    /**
     * Installs a {@link AuthenticatedWebSessionForIsis custom implementation}
     * of Wicket's own {@link AuthenticatedWebSession}, effectively associating
     * the Wicket session with the Isis's equivalent session object.
     *
     * <p>
     * In general, it shouldn't be necessary to override this method.
     */
    @Override
    protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
        return AuthenticatedWebSessionForIsis.class;
    }

    /**
     * Installs a {@link ConverterLocator} preconfigured with a number of
     * implementations to support Isis specific objects.
     */
    @Override
    protected IConverterLocator newConverterLocator() {
        final ConverterLocator converterLocator = new ConverterLocator();
        converterLocator.set(ManagedObject.class, new ConverterForObjectAdapter());
        converterLocator.set(ObjectMemento.class, new ConverterForObjectAdapterMemento(commonContext));
        return converterLocator;
    }

    /**
     * Delegates to the {@link #getPageClassRegistry() PageClassRegistry}.
     */
    @Override
    public Class<? extends Page> getHomePage() {
        return getPageClassRegistry().getPageClass(PageType.HOME);
    }

    /**
     * Delegates to the {@link #getPageClassRegistry() PageClassRegistry}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends WebPage> getSignInPageClass() {
        return (Class<? extends WebPage>) getPageClassRegistry().getPageClass(PageType.SIGN_IN);
    }

    /**
     * Delegates to the {@link #getPageClassRegistry() PageClassRegistry}.
     */
    @SuppressWarnings("unchecked")
    public Class<? extends WebPage> getSignUpPageClass() {
        return (Class<? extends WebPage>) getPageClassRegistry().getPageClass(PageType.SIGN_UP);
    }

    /**
     * Delegates to the {@link #getPageClassRegistry() PageClassRegistry}.
     */
    @SuppressWarnings("unchecked")
    public Class<? extends WebPage> getSignUpVerifyPageClass() {
        return (Class<? extends WebPage>) getPageClassRegistry().getPageClass(PageType.SIGN_UP_VERIFY);
    }

    /**
     * Delegates to the {@link #getPageClassRegistry() PageClassRegistry}.
     */
    @SuppressWarnings("unchecked")
    public Class<? extends WebPage> getForgotPasswordPageClass() {
        return (Class<? extends WebPage>) getPageClassRegistry().getPageClass(PageType.PASSWORD_RESET);
    }

}
