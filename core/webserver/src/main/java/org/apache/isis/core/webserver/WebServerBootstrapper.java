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

package org.apache.isis.core.webserver;

import java.util.List;
import java.util.Map;
import com.google.inject.Injector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderPrimer;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ObjectExtensions;
import org.apache.isis.core.runtime.runner.IsisBootstrapper;
import org.apache.isis.core.runtime.runner.IsisRunner;
import org.apache.isis.core.webapp.WebAppConstants;
import org.apache.isis.core.webserver.WebServer.StartupMode;

import static org.apache.isis.core.webserver.WebServerConstants.*;

final class WebServerBootstrapper implements IsisBootstrapper {

    private static final Logger LOG = LoggerFactory.getLogger(WebServerBootstrapper.class);
    private static final String SRC_MAIN_WEBAPP = "src/main/webapp";

    private final IsisRunner runner;

    private Server jettyServer;

    WebServerBootstrapper(final IsisRunner runner) {
        this.runner = runner;
    }

    /**
     * ignores the arguments and just bootstraps JettyViewer, come what may.
     */
    @Override
    public void bootstrap(final Injector injector) {

        final IsisConfigurationBuilder isisConfigurationBuilder = runner.getStartupConfiguration();

        // we don't actually bootstrap the system here; instead we expect it to
        // be bootstrapped
        // from the ServletContextInitializer in the web.xml
        final IsisConfiguration configuration = isisConfigurationBuilder.getConfiguration();
        final int port = configuration.getInteger(EMBEDDED_WEB_SERVER_PORT_KEY, EMBEDDED_WEB_SERVER_PORT_DEFAULT);
        final String webappContextPath = configuration.getString(EMBEDDED_WEB_SERVER_RESOURCE_BASE_KEY, EMBEDDED_WEB_SERVER_RESOURCE_BASE_DEFAULT);
        final StartupMode startupMode = StartupMode.lookup(configuration.getString(EMBEDDED_WEB_SERVER_STARTUP_MODE_KEY, EMBEDDED_WEB_SERVER_STARTUP_MODE_DEFAULT));
        // TODO get and use the address

        jettyServer = new Server(port);
        Connector[] connectors = jettyServer.getConnectors();
        Connector connector = connectors[0];
        connector.setRequestHeaderSize(8192);
        connector.setResponseHeaderSize(8192);
        final WebAppContext context = new WebAppContext(SRC_MAIN_WEBAPP, webappContextPath);

        copyConfigurationPrimersIntoServletContext(context);

        jettyServer.setHandler(context);

        LOG.info("Starting Jetty on port '{}' to serve the web application", port);
        long start = System.currentTimeMillis();
        try {
            jettyServer.start();
            LOG.info("Started the application in {}ms", System.currentTimeMillis() - start);
            if (startupMode.isForeground()) {
                System.in.read();
                System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
                jettyServer.stop();
                jettyServer.join();
            }
        } catch (final Exception ex) {
             throw new IsisException("Unable to start Jetty server", ex);
        }
    }

    public Server getJettyServer() {
        return jettyServer;
    }

    /**
     * Bound to the {@link WebAppContext} so that they can be used when
     * bootstrapping.
     *
     * @param context
     */
    @SuppressWarnings("unchecked")
    private void copyConfigurationPrimersIntoServletContext(final WebAppContext context) {
        final List<IsisConfigurationBuilderPrimer> isisConfigurationBuilderPrimers = (List<IsisConfigurationBuilderPrimer>) (List<?>) runner.getOptionHandlers();
        context.setAttribute(WebAppConstants.CONFIGURATION_PRIMERS_KEY, isisConfigurationBuilderPrimers);
    }

    @SuppressWarnings("unused")
    private void copyDeploymentTypeIntoInitParams(final WebAppContext context) {
        Map<String, String> initParams = context.getInitParams();
        Map<String, String> convertedInitParams = ObjectExtensions.asT(initParams);
        initParams.clear();
        initParams.putAll(convertedInitParams);
    }
}
