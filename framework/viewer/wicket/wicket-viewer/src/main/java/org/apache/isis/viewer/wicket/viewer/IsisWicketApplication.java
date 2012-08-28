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

import java.util.List;
import java.util.ServiceLoader;

import javax.servlet.ServletContext;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

import org.apache.wicket.Application;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.settings.IRequestCycleSettings.RenderStrategy;
import org.apache.wicket.util.convert.ConverterLocator;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAware;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderPrimer;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderResourceStreams;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceCurrentClassClassPath;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.webapp.config.ResourceStreamSourceForWebInf;
import org.apache.isis.runtimes.dflt.runtime.runner.IsisModule;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.IsisSystem;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.webapp.WebAppConstants;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.app.cssrenderer.ApplicationCssRenderer;
import org.apache.isis.viewer.wicket.ui.app.imagecache.ImageCache;
import org.apache.isis.viewer.wicket.ui.app.imagecache.ImageCacheAccessor;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryList;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageClassList;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageType;
import org.apache.isis.viewer.wicket.viewer.imagecache.ImageCacheClassPath;
import org.apache.isis.viewer.wicket.viewer.integration.isis.WicketServer;
import org.apache.isis.viewer.wicket.viewer.integration.isis.WicketServerPrototype;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.AuthenticatedWebSessionForIsis;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.ConverterForObjectAdapter;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.ConverterForObjectAdapterMemento;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.WebRequestCycleForIsis;

/**
 * Main application, subclassing the Wicket {@link Application} and
 * bootstrapping Isis.
 * 
 * <p>
 * Its main responsibility is to allow the set of {@link ComponentFactory}s used
 * to render the domain objects to be registered. This type of customisation is
 * commonplace. At a more fundamental level, also allows the {@link Page}
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
 * Alternatively, {@link ComponentFactory}s can be specified by overridding
 * {@link #newComponentFactoryList()}. This offers more fine-grained control for
 * the ordering, but is more fiddly.
 * 
 * <p>
 * There are also a number of other pluggable hooks (similar way to other Wicket
 * customizations)
 * <ul>
 * <li> {@link #newComponentFactoryList()} (mentioned above)</li>
 * <li> {@link #newComponentFactoryRegistry()} (uses the
 * {@link ComponentFactoryList} provided by {@link #newComponentFactoryList()})</li>
 * <li> {@link #newPageClassList()}</li>
 * <li> {@link #newPageRegistry()} (uses the {@link PageClassList} provided by
 * {@link #newPageClassList()})</li>
 * <li> {@link #newConverterLocator()} (probably should not be changed.)</li>
 * <li> {@link #newRequestCycle(Request, Response)} (probably should not be
 * changed.)</li>
 * </ul>
 */
public class IsisWicketApplication extends AuthenticatedWebApplication implements ComponentFactoryRegistryAccessor, PageClassRegistryAccessor, ImageCacheAccessor, ApplicationCssRenderer, AuthenticationSessionProvider {

    private static final long serialVersionUID = 1L;

    private static final String WICKET_CONFIGURATION_TYPE_DEVELOPMENT = Application.DEVELOPMENT;

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
    @Inject
    private ImageCacheClassPath imageCache;

    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @Inject
    private PageClassRegistry pageClassRegistry;

    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @ApplicationCssUrl
    @Inject
    private String applicationCssUrl;

    /**
     * {@link Inject}ed when {@link #init() initialized}.
     */
    @Inject
    @SuppressWarnings("unused")
    private IsisSystem system;

    // /////////////////////////////////////////////////
    // constructor, init
    // /////////////////////////////////////////////////

    public IsisWicketApplication() {
    }

    /**
     * Initializes the application; in particular, bootstrapping the Isis
     * backend, and initializing the {@link ComponentFactoryRegistry} to be used
     * for rendering.
     */
    @Override
    protected void init() {
        super.init();
        getResourceSettings().setParentFolderPlaceholder("$up$");

        final DeploymentType deploymentType = determineDeploymentType();

        final IsisConfigurationBuilder isisConfigurationBuilder = createConfigBuilder();

        final IsisModule isisModule = newIsisModule(deploymentType, isisConfigurationBuilder);
        final Injector injector = Guice.createInjector(isisModule, newIsisWicketModule());
        injector.injectMembers(this);

        initWicketComponentInjection(injector);
    }

    protected IsisModule newIsisModule(final DeploymentType deploymentType, final IsisConfigurationBuilder isisConfigurationBuilder) {
        return new IsisModule(deploymentType, isisConfigurationBuilder);
    }

    private DeploymentType determineDeploymentType() {
        if (getConfigurationType().equalsIgnoreCase(WICKET_CONFIGURATION_TYPE_DEVELOPMENT)) {
            return new WicketServerPrototype();
        } else {
            return new WicketServer();
        }
    }

    private IsisConfigurationBuilder createConfigBuilder() {
        final ResourceStreamSource rssServletContext = new ResourceStreamSourceForWebInf(getServletContext());
        final ResourceStreamSource rssTcl = ResourceStreamSourceContextLoaderClassPath.create();
        final ResourceStreamSource rssClasspath = new ResourceStreamSourceCurrentClassClassPath();
        final IsisConfigurationBuilderResourceStreams configurationBuilder = new IsisConfigurationBuilderResourceStreams(rssTcl, rssClasspath, rssServletContext);
        primeConfigurationBuilder(configurationBuilder, getServletContext());
        return configurationBuilder;
    }

    @SuppressWarnings("unchecked")
    private void primeConfigurationBuilder(final IsisConfigurationBuilder isisConfigurationBuilder, final ServletContext servletContext) {
        final List<IsisConfigurationBuilderPrimer> isisConfigurationBuilderPrimers = (List<IsisConfigurationBuilderPrimer>) servletContext.getAttribute(WebAppConstants.CONFIGURATION_PRIMERS_KEY);
        if (isisConfigurationBuilderPrimers == null) {
            return;
        }
        for (final IsisConfigurationBuilderPrimer isisConfigurationBuilderPrimer : isisConfigurationBuilderPrimers) {
            isisConfigurationBuilderPrimer.primeConfigurationBuilder(isisConfigurationBuilder);
        }
    }

    protected void initWicketComponentInjection(final Injector injector) {
        addComponentInstantiationListener(new GuiceComponentInjector(this, injector));
    }

    /**
     * Override if required
     */
    protected Module newIsisWicketModule() {
        return new IsisWicketModule();
    }

    // /////////////////////////////////////////////////
    // Wicket Hooks
    // /////////////////////////////////////////////////

    @Override
    public IRequestCycleSettings getRequestCycleSettings() {
        final IRequestCycleSettings requestCycleSettings = super.getRequestCycleSettings();
        requestCycleSettings.setRenderStrategy(IRequestCycleSettings.REDIRECT_TO_RENDER);
        return requestCycleSettings;
    }
    
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
     * Installs a {@link WebRequestCycleForIsis custom implementation} of
     * Wicket's own {@link RequestCycle}, hooking in to provide session and
     * transaction management across potentially multiple concurrent requests
     * for the same Wicket session.
     * 
     * <p>
     * In general, it shouldn't be necessary to override this method.
     */
    @Override
    public RequestCycle newRequestCycle(final Request request, final Response response) {
        return new WebRequestCycleForIsis(this, (WebRequest) request, response);
    }

    /**
     * Installs a {@link ConverterLocator} preconfigured with a number of
     * implementations to support Isis specific objects.
     * 
     * <p>
     * In general, it shouldn't be necessary to override this method.
     */
    @Override
    protected IConverterLocator newConverterLocator() {
        final ConverterLocator converterLocator = new ConverterLocator();
        converterLocator.set(ObjectAdapter.class, new ConverterForObjectAdapter());
        converterLocator.set(ObjectAdapterMemento.class, new ConverterForObjectAdapterMemento());
        return converterLocator;
    }

    // /////////////////////////////////////////////////
    // Application Css
    // /////////////////////////////////////////////////

    protected String getApplicationCssUrl() {
        return applicationCssUrl;
    }

    /**
     * Renders the {@link #getApplicationCssUrl() application-supplied CSS}, if
     * any.
     * 
     * <p>
     * TODO: doing it this way, as opposed to simply
     * {@link #addRenderHeadListener(IHeaderContributor) registering} an
     * {@link IHeaderContributor} does mean that the header is not first in the
     * list, so can override other page-level CSS. However, it still comes after
     * any component-level CSS, so is not ideal.
     */
    @Override
    public void renderApplicationCss(final HtmlHeaderContainer container) {
        final String cssUrl = getApplicationCssUrl();
        if (cssUrl == null) {
            return;
        }
        final IHeaderResponse headerResponse = container.getHeaderResponse();
        headerResponse.renderCSSReference(cssUrl);
    }

    // /////////////////////////////////////////////////
    // Component Factories
    // /////////////////////////////////////////////////

    /**
     * The {@link ComponentFactoryRegistry} created in
     * {@link #newComponentFactoryRegistry()}.
     */
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

    // /////////////////////////////////////////////////
    // Images
    // /////////////////////////////////////////////////

    @Override
    public ImageCache getImageCache() {
        return imageCache;
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
