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

package org.apache.isis.viewer.wicket.viewer;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.BootstrapBaseBehavior;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;
import de.agilecoders.wicket.webjars.WicketWebjars;
import de.agilecoders.wicket.webjars.settings.IWebjarsSettings;
import de.agilecoders.wicket.webjars.settings.WebjarsSettings;
import net.ftlines.wicketsource.WicketSource;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import javax.servlet.ServletContext;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.vaynberg.wicket.select2.ApplicationSettings;
import org.apache.wicket.Application;
import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.SharedResources;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponseDecorator;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.PageRequestHandlerTracker;
import org.apache.wicket.request.cycle.RequestCycleListenerCollection;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.settings.IRequestCycleSettings.RenderStrategy;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAware;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderPrimer;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderResourceStreams;
import org.apache.isis.core.commons.resource.ResourceStreamSourceComposite;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceCurrentClassClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceFileSystem;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.core.runtime.runner.IsisInjectModule;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerInitParameters;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.webapp.IsisWebAppBootstrapperUtil;
import org.apache.isis.core.webapp.WebAppConstants;
import org.apache.isis.core.webapp.config.ResourceStreamSourceForWebInf;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.string.MultiLineStringPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.Select2BootstrapCssReference;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.Select2JsReference;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountConfirmationMap;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;
import org.apache.isis.viewer.wicket.viewer.integration.isis.DeploymentTypeWicketAbstract;
import org.apache.isis.viewer.wicket.viewer.integration.isis.WicketServer;
import org.apache.isis.viewer.wicket.viewer.integration.isis.WicketServerPrototype;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.AuthenticatedWebSessionForIsis;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.ConverterForObjectAdapter;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.ConverterForObjectAdapterMemento;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.WebRequestCycleForIsis;
import org.apache.isis.viewer.wicket.viewer.settings.IsisResourceSettings;

/**
 * Main application, subclassing the Wicket {@link Application} and
 * bootstrapping Isis.
 *
 * <p>
 * Its main responsibility is to allow the set of {@link ComponentFactory}s used
 * to render the domain objects to be registered. This type of customisation is
 * common place. At a more fundamental level, also allows the {@link Page}
 * implementation for each {@link PageType page type} to be overridden. This is
 * probably less common, because CSS can also be used for this purpose.
 *
 * <p>
 * New {@link ComponentFactory}s can be specified in two ways. The preferred
 * approach is to use the {@link ServiceLoader} mechanism, whereby the
 * {@link ComponentFactory} implementation class is specified in a file under
 * <tt>META-INF/services</tt>. See <tt>views-gmaps2</tt> for an example of this.
 * Including a jar that uses this mechanism on the classpath will automatically
 * make the {@link ComponentFactory} defined within it available.
 *
 * <p>
 * Alternatively, {@link ComponentFactory}s can be specified by overridding {@link #newIsisWicketModule()}.
 * This mechanism allows a number of other aspects to be customized.
 */
public class IsisWicketApplication extends AuthenticatedWebApplication implements ComponentFactoryRegistryAccessor, PageClassRegistryAccessor, AuthenticationSessionProvider {

    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LoggerFactory.getLogger(IsisWicketApplication.class);

    private static final String STRIP_WICKET_TAGS_KEY = "isis.viewer.wicket.stripWicketTags";
    private static final boolean STRIP_WICKET_TAGS_DEFAULT = true;
    private static final String AJAX_DEBUG_MODE_KEY = "isis.viewer.wicket.ajaxDebugMode";
    private static final boolean AJAX_DEBUG_MODE_DEFAULT = false;

    private final IsisLoggingConfigurer loggingConfigurer = new IsisLoggingConfigurer();

    /**
     * Convenience locator, downcasts inherited functionality.
     */
    public static IsisWicketApplication get() {
        return (IsisWicketApplication) AuthenticatedWebApplication.get();
    }

    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @Inject
    private ComponentFactoryRegistry componentFactoryRegistry;

    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @SuppressWarnings("unused")
    @Inject
    private ImageResourceCache imageCache;

    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @SuppressWarnings("unused")
    @Inject
    private WicketViewerSettings wicketViewerSettings;

    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @Inject
    private PageClassRegistry pageClassRegistry;


    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @SuppressWarnings("unused")
    @Inject
    private IsisSystem system;


    private boolean determiningDeploymentType;
    private DeploymentTypeWicketAbstract deploymentType;

    private final List<String> validationErrors = Lists.newArrayList();


    // /////////////////////////////////////////////////
    // constructor, init
    // /////////////////////////////////////////////////

    public IsisWicketApplication() {
    }


    /**
     * Although there are warnings about not overriding this method, it doesn't seem possible
     * to call {@link #setResourceSettings(org.apache.wicket.settings.IResourceSettings)} in the
     * {@link #init()} method.
     */
    @Override
    protected void internalInit() {
        // replace with custom implementation of ResourceSettings that changes the order
        // in which search for i18n properties, to search for the application-specific
        // settings before any other.
        setResourceSettings(new IsisResourceSettings(this));

        super.internalInit();
    }

    /**
     * Initializes the application; in particular, bootstrapping the Isis
     * backend, and initializing the {@link ComponentFactoryRegistry} to be used
     * for rendering.
     */
    @Override
    protected void init() {
        try {
            super.init();

            configureWebJars();
            configureWicketBootstrap();
            configureWicketSelect2();

            String isisConfigDir = getServletContext().getInitParameter("isis.config.dir");

            configureLogging(isisConfigDir);
    
            getRequestCycleSettings().setRenderStrategy(RenderStrategy.REDIRECT_TO_RENDER);

            getResourceSettings().setParentFolderPlaceholder("$up$");

            determineDeploymentTypeIfRequired();

            RequestCycleListenerCollection requestCycleListeners = getRequestCycleListeners();
            IRequestCycleListener requestCycleListenerForIsis = newWebRequestCycleForIsis();
            requestCycleListeners.add(requestCycleListenerForIsis);
            requestCycleListeners.add(new PageRequestHandlerTracker());

            final IsisConfigurationBuilder isisConfigurationBuilder = createConfigBuilder();
    
            final IsisInjectModule isisModule = newIsisModule(deploymentType, isisConfigurationBuilder);
            final Injector injector = Guice.createInjector(isisModule, newIsisWicketModule());
            initWicketComponentInjection(injector);
            injector.injectMembers(this);

            if (requestCycleListenerForIsis instanceof WebRequestCycleForIsis) {
                WebRequestCycleForIsis webRequestCycleForIsis = (WebRequestCycleForIsis) requestCycleListenerForIsis;
                webRequestCycleForIsis.setPageClassRegistry(pageClassRegistry);
            }
            
            final IsisConfiguration configuration = isisConfigurationBuilder.getConfiguration();
            this.getMarkupSettings().setStripWicketTags(determineStripWicketTags(configuration));

            getDebugSettings().setAjaxDebugModeEnabled(determineAjaxDebugModeEnabled(configuration));

            // must be done after injected componentFactoryRegistry into the app itself
            buildCssBundle();

            filterJavascriptContributions();

            configureWicketSourcePlugin();

            // TODO ISIS-987 Either make the API better (no direct access to the map) or use DB records
            int maxEntries = 1000;
            setMetaData(AccountConfirmationMap.KEY, new AccountConfirmationMap(maxEntries, Duration.days(1)));

            mountPages();

            @SuppressWarnings("unused")
            SharedResources sharedResources = getSharedResources();
            
        } catch(RuntimeException ex) {
            List<MetaModelInvalidException> mmies = locateMetaModelInvalidExceptions(ex);
            if(!mmies.isEmpty()) {
                final MetaModelInvalidException mmie = mmies.get(0);
                log("");
                logBanner();
                log("");
                validationErrors.addAll(mmie.getValidationErrors());
                for (String validationError : validationErrors) {
                    logError(validationError);
                }
                log("");
                log("Please inspect the above messages and correct your domain model.");
                log("");
                logBanner();
                log("");
            } else {
                // because Wicket's handling in its WicketFilter (that calls this method) does not log the exception.
                LOG.error("Failed to initialize", ex);
                throw ex;
            }
        }
    }

    private void configureWicketSelect2() {
        ApplicationSettings select2Settings = ApplicationSettings.get();
        select2Settings.setCssReference(new Select2BootstrapCssReference());
        select2Settings.setJavaScriptReference(new Select2JsReference());
        select2Settings.setIncludeJqueryUI(false);
    }

    private void configureWicketSourcePlugin() {
        if(!deploymentType.isProduction()) {
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
        Bootstrap.install(this, settings);

        getHeaderContributorListenerCollection().add(new IHeaderContributor() {
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
    protected void configureLogging(String isisConfigDir) {
        final String loggingPropertiesDir;
        if(isisConfigDir != null) {
            loggingPropertiesDir = isisConfigDir;
        } else {
            loggingPropertiesDir = getServletContext().getRealPath("/WEB-INF");
        }

        loggingConfigurer.configureLogging(loggingPropertiesDir, new String[0]);
    }

    // //////////////////////////////////////

    /**
     * Factored out for easy (informal) pluggability.
     */
    protected IRequestCycleListener newWebRequestCycleForIsis() {
        return new WebRequestCycleForIsis();
    }

    // //////////////////////////////////////

    /**
     * Made protected visibility for easy (informal) pluggability.
     */
    protected void determineDeploymentTypeIfRequired() {
        if(deploymentType != null) {
            return;
        }

        determiningDeploymentType = true;
        try {
            final IsisConfigurationBuilder isisConfigurationBuilder = createConfigBuilder();
            final IsisConfiguration configuration = isisConfigurationBuilder.getConfiguration();
            String deploymentTypeFromConfig = configuration.getString("isis.deploymentType");
            deploymentType = determineDeploymentType(deploymentTypeFromConfig);
        } finally {
            determiningDeploymentType = false;
        }
    }

    /**
     * Made protected visibility for easy (informal) pluggability.
     */
    protected DeploymentTypeWicketAbstract determineDeploymentType(String deploymentTypeFromConfig) {
        final DeploymentTypeWicketAbstract prototype = new WicketServerPrototype();
        final DeploymentTypeWicketAbstract deployment = new WicketServer();

        if(deploymentTypeFromConfig != null) {
            final DeploymentType deploymentType = DeploymentType.lookup(deploymentTypeFromConfig);
            return !deploymentType.getDeploymentCategory().isProduction()
                    ? prototype
                    : deployment;
        } else {
            return usesDevelopmentConfig()
                    ? prototype
                    : deployment;
        }
    }


    // //////////////////////////////////////

    private IsisConfigurationBuilder createConfigBuilder() {
        return createConfigBuilder(getServletContext());
    }

    protected IsisConfigurationBuilder createConfigBuilder(final ServletContext servletContext) {

        final String configLocation = servletContext.getInitParameter(WebAppConstants.CONFIG_DIR_PARAM);
        final ResourceStreamSourceForWebInf rssWebInf = new ResourceStreamSourceForWebInf(servletContext);
        final ResourceStreamSourceContextLoaderClassPath rssContextLoaderClassPath = ResourceStreamSourceContextLoaderClassPath.create();
        final ResourceStreamSourceCurrentClassClassPath rssCurrentClassPath = new ResourceStreamSourceCurrentClassClassPath();
        final ResourceStreamSourceComposite compositeSource = new ResourceStreamSourceComposite(rssWebInf, rssContextLoaderClassPath, rssCurrentClassPath);

        if ( configLocation != null ) {
            LOG.info( "Config override location: " + configLocation );
            compositeSource.addResourceStreamSource(ResourceStreamSourceFileSystem.create(configLocation));
        } else {
            LOG.info( "Config override location: No override location configured!" );
        }

        final IsisConfigurationBuilder configurationBuilder = new IsisConfigurationBuilderResourceStreams(compositeSource);

        primeConfigurationBuilder(configurationBuilder, servletContext);
        configurationBuilder.addDefaultConfigurationResources();

        IsisWebAppBootstrapperUtil.addConfigurationResourcesForViewers(configurationBuilder, servletContext);
        return configurationBuilder;
    }

    @SuppressWarnings("unchecked")
    private static void primeConfigurationBuilder(final IsisConfigurationBuilder isisConfigurationBuilder, final ServletContext servletContext) {
        LOG.info("loading properties from option handlers");
        final List<IsisConfigurationBuilderPrimer> isisConfigurationBuilderPrimers = Lists.newArrayList();
        final List<IsisConfigurationBuilderPrimer> primers = (List<IsisConfigurationBuilderPrimer>) servletContext.getAttribute(WebAppConstants.CONFIGURATION_PRIMERS_KEY);
        if(primers != null) {
            isisConfigurationBuilderPrimers.addAll(primers);
        }
        // also support loading from init parameters (specifically, to support simplericity's jetty-console)
        isisConfigurationBuilderPrimers.add(new OptionHandlerInitParameters(servletContext));
        for (final IsisConfigurationBuilderPrimer isisConfigurationBuilderPrimer : isisConfigurationBuilderPrimers) {
            LOG.info("priming configurations for " + isisConfigurationBuilderPrimer);
            isisConfigurationBuilderPrimer.primeConfigurationBuilder(isisConfigurationBuilder);
        }
    }

    // //////////////////////////////////////

    /**
     * Override if required
     */
    protected Module newIsisWicketModule() {
        return new IsisWicketModule();
    }

    // //////////////////////////////////////

    /**
     * Made protected visibility for easy (informal) pluggability.
     */
    protected void buildCssBundle() {
        // get the css for all components built by component factories
        final Set<CssResourceReference> references = cssResourceReferencesForAllComponents();

        // some additional special cases.
        addSpecialCasesToCssBundle(references);

        // create the bundle
        getResourceBundles().addCssBundle(
                IsisWicketApplication.class, "isis-wicket-viewer-bundle.css",
                references.toArray(new CssResourceReference[]{}));
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
            new Function<ComponentFactory, Iterable<CssResourceReference>>(){
                @Override
                public Iterable<CssResourceReference> apply(final ComponentFactory input) {
                    final CssResourceReference cssResourceReference = input.getCssResourceReference();
                    return cssResourceReference != null?
                            Collections.singletonList(cssResourceReference):
                            Collections.<CssResourceReference>emptyList();
                }
            };


    protected Set<CssResourceReference> cssResourceReferencesForAllComponents() {
        // TODO mgrigorov: ISIS-537 temporary disabled to not mess up with Bootstrap styles
//        Collection<ComponentFactory> componentFactories = getComponentFactoryRegistry().listComponentFactories();
        return Sets.newLinkedHashSet(
//                Iterables.concat(
//                        Iterables.transform(
//                                componentFactories,
//                                getCssResourceReferences))
        );
    }

    // //////////////////////////////////////

    /**
     * filters Javascript header contributions so rendered to bottom of page.
     *
     * <p>
     * Factored out for easy (informal) pluggability.
     * </p>
     */
    protected void filterJavascriptContributions() {
        setHeaderResponseDecorator(new IHeaderResponseDecorator()
        {
            @Override
            public IHeaderResponse decorate(IHeaderResponse response)
            {
                // use this header resource decorator to load all JavaScript resources in the page
                // footer (after </body>)
                return new JavaScriptFilteredIntoFooterHeaderResponse(response, "footerJS");
            }
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
    }

    protected void mountPage(final String mountPath, final PageType pageType) {
        final Class<? extends Page> pageClass = this.pageClassRegistry.getPageClass(pageType);
        mount(new MountedMapper(mountPath, pageClass));
    }


    // //////////////////////////////////////

    protected List<MetaModelInvalidException> locateMetaModelInvalidExceptions(RuntimeException ex) {
        return Lists.newArrayList(
                Iterables.filter(Throwables.getCausalChain(ex), MetaModelInvalidException.class));
    }

    /**
     * The validation errors, if any, that occurred on {@link #init() startup}.
     */
    public List<String> getValidationErrors() {
        return validationErrors;
    }
    
    private void logError(String validationError) {
        log(validationError);
    }

    private static void logBanner() {
        String msg = "################################################ ISIS METAMODEL VALIDATION ERRORS ################################################################";
        log(msg);
    }

    private static void log(String msg) {
        System.err.println(msg);
        LOG.error(msg);
    }

    // //////////////////////////////////////

    /**
     * Whether Wicket tags should be stripped from the markup, as specified by configuration settings..
     * 
     * <p>
     * If the <tt>isis.viewer.wicket.stripWicketTags</tt> is set, then this is used, otherwise the default is to strip 
     * the tags because they may break some CSS rules.
     */
    private boolean determineStripWicketTags(IsisConfiguration configuration) {
        final boolean strip = configuration.getBoolean(STRIP_WICKET_TAGS_KEY, STRIP_WICKET_TAGS_DEFAULT);
        return strip;
    }

    // //////////////////////////////////////

    /**
     * Whether the Ajax debug should be shown, as specified by configuration settings.
     *
     * <p>
     * If the <tt>isis.viewer.wicket.ajaxDebugMode</tt> is set, then this is used, otherwise the default is to disable.
     */
    private boolean determineAjaxDebugModeEnabled(IsisConfiguration configuration) {
        final boolean debugMode = configuration.getBoolean(AJAX_DEBUG_MODE_KEY, AJAX_DEBUG_MODE_DEFAULT);
        return debugMode;
    }

    // //////////////////////////////////////

    @Override
    protected void onDestroy() {
        try {
            if (system != null) {
                system.shutdown();
            }
            IsisContext.shutdown();
            super.onDestroy();
        } catch(final RuntimeException ex) {
            // symmetry with #init()
            LOG.error("Failed to destroy", ex);
            throw ex;
        }
    }

    // //////////////////////////////////////

    @Override
    public RuntimeConfigurationType getConfigurationType() {
        if(determiningDeploymentType) {
            // avoiding an infinite loop; have already passed through here once before
            // this time around, just delegate to web-inf
            return super.getConfigurationType();
        }
        determineDeploymentTypeIfRequired();
        return deploymentType.getConfigurationType();
    }
    
    protected IsisInjectModule newIsisModule(final DeploymentType deploymentType, final IsisConfigurationBuilder isisConfigurationBuilder) {
        return new IsisInjectModule(deploymentType, isisConfigurationBuilder);
    }

    // //////////////////////////////////////


    protected void initWicketComponentInjection(final Injector injector) {
        // if serializable, then brings in dependency on cglib, and in turn asm.
        // This would block us from migrating to DN 4.0.x
        getComponentInstantiationListeners().add(new GuiceComponentInjector(this, injector, false));
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
        converterLocator.set(ObjectAdapter.class, new ConverterForObjectAdapter());
        converterLocator.set(ObjectAdapterMemento.class, new ConverterForObjectAdapterMemento());
        return converterLocator;
    }

    // /////////////////////////////////////////////////
    // Component Factories
    // /////////////////////////////////////////////////

    @Override
    public final ComponentFactoryRegistry getComponentFactoryRegistry() {
        return componentFactoryRegistry;
    }

    // /////////////////////////////////////////////////
    // Page Registry
    // /////////////////////////////////////////////////

    /**
     * Access to other page types.
     *
     * <p>
     * Non-final only for testing purposes; should not typically be overridden.
     */
    @Override
    public PageClassRegistry getPageClassRegistry() {
        return pageClassRegistry;
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


    // /////////////////////////////////////////////////
    // Authentication Session
    // /////////////////////////////////////////////////

    @Override
    public AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }


    // /////////////////////////////////////////////////
    // *Provider impl.
    // /////////////////////////////////////////////////

    @Override
    public void injectInto(final Object candidate) {
        if (AuthenticationSessionProviderAware.class.isAssignableFrom(candidate.getClass())) {
            final AuthenticationSessionProviderAware cast = AuthenticationSessionProviderAware.class.cast(candidate);
            cast.setAuthenticationSessionProvider(this);
        }
    }
}
