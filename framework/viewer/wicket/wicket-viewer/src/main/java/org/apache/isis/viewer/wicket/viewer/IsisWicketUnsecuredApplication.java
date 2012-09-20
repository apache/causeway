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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.settings.IRequestCycleSettings.RenderStrategy;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAware;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderResourceStreams;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceCurrentClassClassPath;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.runtime.authentication.standard.AuthenticatorAbstract;
import org.apache.isis.core.webapp.config.ResourceStreamSourceForWebInf;
import org.apache.isis.runtimes.dflt.runtime.runner.IsisModule;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.IsisSystem;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;
import org.apache.isis.viewer.wicket.ui.app.cssrenderer.ApplicationCssRenderer;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageType;
import org.apache.isis.viewer.wicket.viewer.integration.isis.WicketServer;
import org.apache.isis.viewer.wicket.viewer.integration.isis.WicketServerPrototype;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.AnonymousWebSessionForIsis;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.ConverterForObjectAdapter;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.ConverterForObjectAdapterMemento;
import org.apache.isis.viewer.wicket.viewer.integration.wicket.WebRequestCycleForIsis;

public class IsisWicketUnsecuredApplication extends WebApplication implements ComponentFactoryRegistryAccessor, PageClassRegistryAccessor, ApplicationCssRenderer, AuthenticationSessionProvider {

    private static final long serialVersionUID = 1L;

    //private static final String WICKET_CONFIGURATION_TYPE_DEVELOPMENT = Application.DEVELOPMENT;

    /**
     * Convenience locator, downcasts inherited functionality.
     */
    public static IsisWicketUnsecuredApplication get() {
        return (IsisWicketUnsecuredApplication) WebApplication.get();
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
    private ImageResourceCache imageCache;

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

    public IsisWicketUnsecuredApplication() {
    }

    /**
     * Initializes the application; in particular, bootstrapping the Isis
     * backend, and initializing the {@link ComponentFactoryRegistry} to be used
     * for rendering.
     */
    @Override
    protected void init() {
        super.init();

        // 6.0.0 rather than overriding getRequestCycleSettings
        getRequestCycleSettings().setRenderStrategy(RenderStrategy.REDIRECT_TO_RENDER);
        // 6.0.0 instead of subclassing newRequestCycle 
        getRequestCycleListeners().add(new WebRequestCycleForIsis());

        
        
        getResourceSettings().setParentFolderPlaceholder("$up$");
        final DeploymentType deploymentType = determineDeploymentType();

        final IsisConfigurationBuilder isisConfigurationBuilder = createConfigBuilder();

        final IsisModule isisModule = new IsisModule(deploymentType, isisConfigurationBuilder);
        final Injector injector = Guice.createInjector(isisModule, newIsisWicketModule());
        injector.injectMembers(this);

        initWicketComponentInjection(injector);
    }

    private DeploymentType determineDeploymentType() {
        if(usesDevelopmentConfig()) {
        //if (getConfigurationType().equalsIgnoreCase(WICKET_CONFIGURATION_TYPE_DEVELOPMENT)) {
            return new WicketServerPrototype();
        } else {
            return new WicketServer();
        }
    }

    private IsisConfigurationBuilder createConfigBuilder() {
        final ResourceStreamSource rssServletContext = new ResourceStreamSourceForWebInf(getServletContext());
        final ResourceStreamSource rssTcl = ResourceStreamSourceContextLoaderClassPath.create();
        final ResourceStreamSource rssClasspath = new ResourceStreamSourceCurrentClassClassPath();
        final IsisConfigurationBuilder isisConfigurationBuilder = new IsisConfigurationBuilderResourceStreams(rssTcl, rssClasspath, rssServletContext);
        return isisConfigurationBuilder;
    }

    protected void initWicketComponentInjection(final Injector injector) {
        getComponentInstantiationListeners().add(new GuiceComponentInjector(this, injector));
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
    public Session newSession(final Request request, final Response response) {
        final AuthenticationManager authenticationManager = anonymousAuthenticationManager();

        final AnonymousWebSessionForIsis anonymousWebSession = new AnonymousWebSessionForIsis(request, authenticationManager);
        anonymousWebSession.authenticate(null, null);
        return anonymousWebSession;
    }

    private AuthenticationManager authenticationManager;

    /**
     * Lazily creates an {@link AuthenticationManager} that will authenticate
     * all requests.
     */
    private AuthenticationManager anonymousAuthenticationManager() {
        if (authenticationManager == null) {
            final IsisConfiguration configuration = IsisContext.getConfiguration();
            final AuthenticationManagerStandard authenticationManager = new AuthenticationManagerStandard(configuration);
            authenticationManager.addAuthenticator(new AuthenticatorAbstract(configuration) {
                @Override
                public boolean isValid(final AuthenticationRequest request) {
                    return true;
                }

                @Override
                public boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
                    return true;
                }
            });
            authenticationManager.init();
            this.authenticationManager = authenticationManager;
        }
        return authenticationManager;
    }

//    /**
//     * Installs a {@link WebRequestCycleForIsis custom implementation} of
//     * Wicket's own {@link RequestCycle}, hooking in to provide session and
//     * transaction management across potentially multiple concurrent requests
//     * for the same Wicket session.
//     * 
//     * <p>
//     * In general, it shouldn't be necessary to override this method.
//     */
//    @Override
//    public RequestCycle newRequestCycle(final Request request, final Response response) {
//        return new WebRequestCycleForIsis(this, (WebRequest) request, response);
//    }

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
        final IHeaderResponse response = container.getHeaderResponse();
        //response.renderCSSReference(cssUrl);
        response.render(CssHeaderItem.forReference(new CssResourceReference(this.getClass(), cssUrl)));
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
