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

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

import org.apache.isis.commons.internal.collections._Lists;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.commons.lang.ObjectExtensions;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceFileSystem;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.core.runtime.optionhandler.OptionHandler;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerAdditionalProperty;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerAppManifest;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerConfiguration;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerFixture;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerFixtureFromEnvironmentVariable;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerHelp;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerSystemProperties;
import org.apache.isis.core.webapp.WebAppConstants;
import org.apache.isis.core.webserver.internal.OptionHandlerDeploymentTypeWebServer;
import org.apache.isis.core.webserver.internal.OptionHandlerPort;
import org.apache.isis.core.webserver.internal.OptionHandlerStartupMode;

import static org.apache.isis.core.webserver.WebServerConstants.EMBEDDED_WEB_SERVER_PORT_DEFAULT;
import static org.apache.isis.core.webserver.WebServerConstants.EMBEDDED_WEB_SERVER_PORT_KEY;
import static org.apache.isis.core.webserver.WebServerConstants.EMBEDDED_WEB_SERVER_RESOURCE_BASE_DEFAULT;
import static org.apache.isis.core.webserver.WebServerConstants.EMBEDDED_WEB_SERVER_RESOURCE_BASE_KEY;
import static org.apache.isis.core.webserver.WebServerConstants.EMBEDDED_WEB_SERVER_STARTUP_MODE_DEFAULT;
import static org.apache.isis.core.webserver.WebServerConstants.EMBEDDED_WEB_SERVER_STARTUP_MODE_KEY;

public class WebServer {

    private static final Logger LOG = LoggerFactory.getLogger(WebServer.class);
    private static final String SRC_MAIN_WEBAPP = "src/main/webapp";

    public enum StartupMode {
        FOREGROUND, BACKGROUND;

        public static StartupMode lookup(final String value) {
            if (value == null) {
                return null;
            }
            try {
                return valueOf(value.toUpperCase());
            } catch (final Exception e) {
                return null;
            }
        }

        public boolean isForeground() {
            return this == FOREGROUND;
        }
        public boolean isBackground() {
            return this == BACKGROUND;
        }
    }


    private Server jettyServer;

    public static void main(final String[] args) {
        new WebServer().run(args);
    }

    public void run(final int port) {
        String[] args = new String[0];
        args = OptionHandlerStartupMode.appendArg(args, StartupMode.BACKGROUND);
        args = OptionHandlerPort.appendArg(args, port);
        run(args);
    }

    private final IsisLoggingConfigurer loggingConfigurer = new IsisLoggingConfigurer();

    public void run(final String[] args) {

        // setup logging immediately
        loggingConfigurer.configureLogging(guessConfigDirectory(), args);

        // set up the configuration
        final IsisConfigurationBuilder isisConfigurationBuilder = new IsisConfigurationBuilder();
        isisConfigurationBuilder.addResourceStreamSources(resourceStreamSources());

        if(!isisConfigurationBuilder.parseAndPrimeWith(standardHandlers(), args)) {
            return;
        }

        // create and start
        jettyServer = createJettyServerAndBindConfig(isisConfigurationBuilder);

        final IsisConfiguration configuration = isisConfigurationBuilder.peekConfiguration();
        final String startupModeStr = configuration.getString(
                EMBEDDED_WEB_SERVER_STARTUP_MODE_KEY, EMBEDDED_WEB_SERVER_STARTUP_MODE_DEFAULT);
        final StartupMode startupMode = StartupMode.lookup(startupModeStr);

        start(jettyServer, startupMode);
    }

    private static List<OptionHandler> standardHandlers() {
        return _Lists.of(
                new OptionHandlerConfiguration(),
                new OptionHandlerFixture(),
                new OptionHandlerAppManifest(),
                new OptionHandlerAdditionalProperty(),
                new OptionHandlerFixtureFromEnvironmentVariable(),
                new OptionHandlerSystemProperties(),
                new OptionHandlerHelp(),
                new OptionHandlerPort(),
                new OptionHandlerDeploymentTypeWebServer()
                );
    }

    // REVIEW: hacky...
    private static String guessConfigDirectory() {
        return new File(ConfigurationConstants.WEBINF_FULL_DIRECTORY).exists() ?
                ConfigurationConstants.WEBINF_FULL_DIRECTORY :
                    ConfigurationConstants.DEFAULT_CONFIG_DIRECTORY;
    }


    private Server createJettyServerAndBindConfig(final IsisConfigurationBuilder configurationBuilder) {

        // the Isis system is actually bootstrapped by the ServletContextInitializer in the web.xml
        final IsisConfiguration configuration = configurationBuilder.peekConfiguration();
        final int port = configuration.getInteger(
                EMBEDDED_WEB_SERVER_PORT_KEY, EMBEDDED_WEB_SERVER_PORT_DEFAULT);
        final String webappContextPath = configuration.getString(
                EMBEDDED_WEB_SERVER_RESOURCE_BASE_KEY, EMBEDDED_WEB_SERVER_RESOURCE_BASE_DEFAULT);

        LOG.info("Running Jetty on port '{}' to serve the web application", port);

        final Server jettyServer = new Server(port);
        final WebAppContext context = new WebAppContext(SRC_MAIN_WEBAPP, webappContextPath);
        jettyServer.setHandler(context);

        context.setAttribute(WebAppConstants.CONFIGURATION_BUILDER_KEY, configurationBuilder);

        return jettyServer;
    }

    private static void start(final Server jettyServer, final StartupMode startupMode) {
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

    @SuppressWarnings("unused")
    private void copyDeploymentTypeIntoInitParams(final WebAppContext context) {
        Map<String, String> initParams = context.getInitParams();
        Map<String, String> convertedInitParams = ObjectExtensions.asT(initParams);
        initParams.clear();
        initParams.putAll(convertedInitParams);
    }


    /**
     * Set of locations to search for config files.
     */
    private static List<ResourceStreamSource> resourceStreamSources() {
        final List<ResourceStreamSource> rssList = _Lists.newArrayList();
        rssList.addAll(Arrays.asList(
                ResourceStreamSourceFileSystem.create(ConfigurationConstants.DEFAULT_CONFIG_DIRECTORY),
                ResourceStreamSourceFileSystem.create(ConfigurationConstants.WEBINF_FULL_DIRECTORY),
                ResourceStreamSourceContextLoaderClassPath.create(),
                ResourceStreamSourceContextLoaderClassPath.create(ConfigurationConstants.WEBINF_DIRECTORY)));
        return rssList;
    }

    public void stop() {
        if (jettyServer == null) {
            return;
        }
        try {
            jettyServer.stop();
        } catch (final Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public URI getBase() {
        return URI.create(baseFor(jettyServer));
    }

    private String baseFor(final Server jettyServer) {
        final ServerConnector connector = (ServerConnector) jettyServer.getConnectors()[0];
        final String scheme = "http";
        final String host = ArrayExtensions.coalesce(connector.getHost(), "localhost");
        final int port = connector.getPort();

        final WebAppContext handler = (WebAppContext) jettyServer.getHandler();
        final String contextPath = handler.getContextPath();

        final StringBuilder buf = new StringBuilder();

        try(final Formatter formatter = new Formatter(buf)) {
            formatter.format("%s://%s:%d/%s", scheme, host, port, contextPath);
        }

        return appendSlashIfRequired(buf).toString();
    }

    private static StringBuilder appendSlashIfRequired(final StringBuilder buf) {
        if (buf.charAt(buf.length() - 1) != '/') {
            buf.append('/');
        }
        return buf;
    }
}
