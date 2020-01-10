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

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

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
import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.devutils.debugbar.InspectorDebugPanel;
import org.apache.wicket.devutils.debugbar.PageSizeDebugPanel;
import org.apache.wicket.devutils.debugbar.SessionSizeDebugPanel;
import org.apache.wicket.devutils.debugbar.VersionDebugContributor;
import org.apache.wicket.devutils.diskstore.DebugDiskDataStore;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.PageRequestHandlerTracker;
import org.apache.wicket.request.cycle.RequestCycleListenerCollection;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.settings.RequestCycleSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.time.Duration;
import org.wicketstuff.select2.ApplicationSettings;

import org.apache.isis.core.commons.internal.concurrent._ConcurrentContext;
import org.apache.isis.core.commons.internal.concurrent._ConcurrentTaskList;
import org.apache.isis.core.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettingsAccessor;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.string.MultiLineStringPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.Select2BootstrapCssReference;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.Select2JsReference;
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
import org.apache.isis.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.webapp.context.memento.ObjectMemento;

import static org.apache.isis.core.commons.internal.base._With.requires;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.BootstrapBaseBehavior;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;
import de.agilecoders.wicket.webjars.WicketWebjars;
import de.agilecoders.wicket.webjars.settings.IWebjarsSettings;
import de.agilecoders.wicket.webjars.settings.WebjarsSettings;
import net.ftlines.wicketsource.WicketSource;

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
 * approach is to use the have the IoC container discover the ComponentFactory.
 * See <tt>asciidoc</tt> extension for an example of this.
 *
 * <p>
 * Alternatively, {@link ComponentFactory}s can be specified by overriding {@link #newIsisWicketModule()}.
 * This mechanism allows a number of other aspects to be customized.
 */
@Log4j2
public class IsisWicketApplication
extends AuthenticatedWebApplication
implements ComponentFactoryRegistryAccessor, PageClassRegistryAccessor, WicketViewerSettingsAccessor,
IsisWebAppCommonContext.Provider {

    private static final long serialVersionUID = 1L;

    /**
     * Convenience locator, down-casts inherited functionality.
     */
    public static IsisWicketApplication get() {
        return (IsisWicketApplication) AuthenticatedWebApplication.get();
    }

    @Inject private MetaModelContext metaModelContext;
    
    @Getter(onMethod = @__(@Override)) private IsisWebAppCommonContext commonContext; // shared

    // injected manually
    @Getter(onMethod = @__(@Override)) private ComponentFactoryRegistry componentFactoryRegistry;
    @Getter(onMethod = @__(@Override)) private PageClassRegistry pageClassRegistry;
    @Getter(onMethod = @__(@Override)) private WicketViewerSettings settings;
    private IsisSystemEnvironment systemEnvironment;
    private IsisConfiguration configuration;

    private final IsisWicketApplication_Experimental experimental;
    private final IsisWicketApplication_newSession newSessionMixin;
    private final IsisWicketApplication_newPageFactory newPageFactoryMixin;

    // /////////////////////////////////////////////////
    // constructor, init
    // /////////////////////////////////////////////////

    public IsisWicketApplication() {
        experimental = new IsisWicketApplication_Experimental(this);
        newSessionMixin = new IsisWicketApplication_newSession(this);
        newPageFactoryMixin = new IsisWicketApplication_newPageFactory(this);
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

        // this doesn't seem to accomplish anything
        // experimental.addListenerToStripRemovedComponentsFromAjaxTargetResponse();

        super.internalInit();

    }

    private AjaxRequestTarget decorate(final AjaxRequestTarget ajaxRequestTarget) {
        ajaxRequestTarget.registerRespondListener(
                commonContext.injectServicesInto(
                        new TargetRespondListenerToResetQueryResultCache() ));
        return ajaxRequestTarget;
    }

    @Override
    public Application setAjaxRequestTargetProvider(Function<Page, AjaxRequestTarget> ajaxRequestTargetProvider) {
        final Application application = super.setAjaxRequestTargetProvider(
                (Page context) -> decorate(ajaxRequestTargetProvider.apply(context)) );
        return application;
    }

    /**
     * Initializes the application; in particular, bootstrapping the Isis
     * backend, and initializing the {@link ComponentFactoryRegistry} to be used
     * for rendering.
     */
    protected void init() {
        super.init();
        
        // Initialize Spring Dependency Injection (into Wicket components)
        val springInjector = new SpringComponentInjector(this);
        Injector.get().inject(this);
        getComponentInstantiationListeners().add(springInjector);

        // bootstrap dependencies from the metaModelContext
        {
            
            requires(metaModelContext, "metaModelContext");
            
            commonContext = IsisWebAppCommonContext.of(metaModelContext);
            configuration = commonContext.lookupServiceElseFail(IsisConfiguration.class);
            componentFactoryRegistry = commonContext.lookupServiceElseFail(ComponentFactoryRegistry.class);
            pageClassRegistry = commonContext.lookupServiceElseFail(PageClassRegistry.class);
            settings = commonContext.lookupServiceElseFail(WicketViewerSettings.class);
            systemEnvironment = commonContext.lookupServiceElseFail(IsisSystemEnvironment.class);
        }

        val backgroundInitializationTasks = 
                _ConcurrentTaskList.named("Isis Application Background Initialization Tasks")
                .addRunnable("Configure WebJars",            this::configureWebJars)
                .addRunnable("Configure WicketBootstrap",    this::configureWicketBootstrap)
                .addRunnable("Configure WicketSelect2",      this::configureWicketSelect2);
        
        try {

            backgroundInitializationTasks.submit(_ConcurrentContext.sequential());

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

            configureWicketSourcePluginIfNecessary();

            // TODO ISIS-987 Either make the API better (no direct access to the map) or use DB records
            int maxEntries = 1000;
            setMetaData(AccountConfirmationMap.KEY, new AccountConfirmationMap(maxEntries, Duration.days(1)));

            mountPages();

            //  side-effects?
            //  SharedResources sharedResources = getSharedResources();

            if(systemEnvironment.isPrototyping()) {
                DebugDiskDataStore.register(this);
                log.debug("DebugDiskDataStore registered; access via ~/wicket/internal/debug/diskDataStore");
                log.debug("DebugDiskDataStore: eg, http://localhost:8080/wicket/wicket/internal/debug/diskDataStore");

                if(!getDebugSettings().isDevelopmentUtilitiesEnabled()) {
                    boolean enableDevUtils = configuration.getViewer().getWicket().getDevelopmentUtilities().isEnable();
                    if(enableDevUtils) {
                        getDebugSettings().setDevelopmentUtilitiesEnabled(true);

                        // copied from DebugBarInitializer
                        // this is hacky, but need to do this because IInitializer#init() called before
                        // the Application's #init() is called.
                        // an alternative, better, design might be to move Isis' own initialization into an
                        // implementation of IInitializer?
                        DebugBar.registerContributor(VersionDebugContributor.DEBUG_BAR_CONTRIB, this);
                        DebugBar.registerContributor(InspectorDebugPanel.DEBUG_BAR_CONTRIB, this);
                        DebugBar.registerContributor(SessionSizeDebugPanel.DEBUG_BAR_CONTRIB, this);
                        DebugBar.registerContributor(PageSizeDebugPanel.DEBUG_BAR_CONTRIB, this);
                    }
                }
            }

            log.debug("storeSettings.inmemoryCacheSize        : {}", getStoreSettings().getInmemoryCacheSize());
            log.debug("storeSettings.asynchronousQueueCapacity: {}", getStoreSettings().getAsynchronousQueueCapacity());
            log.debug("storeSettings.maxSizePerSession        : {}", getStoreSettings().getMaxSizePerSession());
            log.debug("storeSettings.fileStoreFolder          : {}", getStoreSettings().getFileStoreFolder());

            backgroundInitializationTasks.await();
            
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

    }
    
    /*
     * @since 2.0 ... overrides the default, to handle special cases when recreating bookmarked pages
     */
    @Override
    protected IPageFactory newPageFactory() {
        return newPageFactoryMixin.interceptPageFactory(super.newPageFactory());
    }

    /*
     * @since 2.0 ... overrides the default, to 'inject' the commonContext into new sessions
     */
    @Override
    public Session newSession(Request request, Response response) {
        return newSessionMixin.interceptNewSession(super.newSession(request, response));
    }

    /**
     * protected visibility to allow ad-hoc overriding of some other authentication strategy.
     */
    void configureSecurity(IsisConfiguration configuration) {
        getSecuritySettings().setAuthenticationStrategy(newAuthenticationStrategy(configuration));
    }

    /**
     * protected visibility to allow ad-hoc overriding of some other authentication strategy.
     */
    IAuthenticationStrategy newAuthenticationStrategy(IsisConfiguration configuration) {
        final String cookieKey = configuration.getViewer().getWicket().getRememberMe().getCookieKey();
        String encryptionKey = configuration.getViewer().getWicket().getRememberMe().getEncryptionKey();
        if (encryptionKey == null) {
            encryptionKey = defaultEncryptionKey();
        }
        return new DefaultAuthenticationStrategy(cookieKey, encryptionKey);
    }

    /**
     * As called by {@link #newAuthenticationStrategy(IsisConfiguration)}; if an encryption key for the 'rememberMe'
     * cookie hasn't been configured, then use a different encryption key for the 'rememberMe' cookie each time the
     * app is restarted.
     */
    String defaultEncryptionKey() {
        return systemEnvironment.isPrototyping()
                ? "PrototypingEncryptionKey"
                        : UUID.randomUUID().toString();
    }

    private void configureWicketSelect2() {
        ApplicationSettings select2Settings = ApplicationSettings.get();
        select2Settings.setCssReference(new Select2BootstrapCssReference());
        select2Settings.setJavaScriptReference(new Select2JsReference());
    }

    protected void configureWicketSourcePluginIfNecessary() {
        
        requireNonNull(configuration, "Configuration must be prepared prior to init().");

        if(configuration.getViewer().getWicket().isWicketSourcePlugin()) {
            configureWicketSourcePlugin();
        }
    }

    protected void configureWicketSourcePlugin() {
        if(systemEnvironment.isPrototyping()) {
            WicketSource.configure(this);
        }
    }

    // //////////////////////////////////////

    /**
     * Install 2 default collector instances: (FileAssetPathCollector(WEBJARS_PATH_PREFIX), JarAssetPathCollector),
     * and a webjars resource finder.
     *
     * <p>
     * Factored out for easy (informal) pluggability.
     * </p>
     */
    protected void configureWebJars() {
        IWebjarsSettings settings = new WebjarsSettings();
        WicketWebjars.install(this, settings);
    }

    protected void configureWicketBootstrap() {
        final IBootstrapSettings settings = new BootstrapSettings();
        settings.setDeferJavascript(false);
        Bootstrap.install(this, settings);

        getHeaderContributorListeners().add(new IHeaderContributor() {
            private static final long serialVersionUID = 1L;

            @Override
            public void renderHead(IHeaderResponse response) {
                BootstrapBaseBehavior bootstrapBaseBehavior = new BootstrapBaseBehavior();
                bootstrapBaseBehavior.renderHead(settings, response);
            }
        });
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

    protected final static Function<ComponentFactory, Iterable<CssResourceReference>> getCssResourceReferences =
            (ComponentFactory input) -> {
                final CssResourceReference cssResourceReference = input.getCssResourceReference();
                return cssResourceReference != null?
                        Collections.singletonList(cssResourceReference):
                            Collections.<CssResourceReference>emptyList();
            };

    // //////////////////////////////////////

    /**
     * filters Javascript header contributions so rendered to bottom of page.
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

        // nb: action mount cannot contain {actionArgs}, because the default
        // parameters encoder doesn't seem to be able to handle multiple args
        mountPage("/action/${objectOid}/${actionOwningSpec}/${actionId}/${actionType}", PageType.ACTION_PROMPT);

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
            //            if (isisSessionFactory != null) {
            //                isisSessionFactory.destroyServicesAndShutdown();
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
