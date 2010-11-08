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

import static org.apache.isis.core.webserver.WebServerConstants.EMBEDDED_WEB_SERVER_PORT_DEFAULT;
import static org.apache.isis.core.webserver.WebServerConstants.EMBEDDED_WEB_SERVER_PORT_KEY;
import static org.apache.isis.core.webserver.WebServerConstants.EMBEDDED_WEB_SERVER_RESOURCE_BASE_DEFAULT;
import static org.apache.isis.core.webserver.WebServerConstants.EMBEDDED_WEB_SERVER_RESOURCE_BASE_KEY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.CastUtils;
import org.apache.isis.core.metamodel.config.ConfigurationBuilder;
import org.apache.isis.core.metamodel.config.ConfigurationPrimer;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.webapp.WebAppConstants;
import org.apache.isis.runtime.runner.IsisBootstrapper;
import org.apache.isis.runtime.runner.IsisRunner;

import com.google.inject.Injector;

final class WebServerBootstrapper implements
        IsisBootstrapper {
    private static final String SRC_MAIN_WEBAPP = "src/main/webapp";
    private final IsisRunner runner;

    WebServerBootstrapper(IsisRunner runner) {
        this.runner = runner;
    }

    /**
     * ignores the arguments and just bootstraps JettyViewer, come what may.
     */
    public void bootstrap(Injector injector) {

        ConfigurationBuilder configurationBuilder = injector.getInstance(ConfigurationBuilder.class);

        // we don't actually bootstrap the system here; instead we expect it to be bootstrapped
        // from the ServletContextInitializer in the web.xml
        IsisConfiguration configuration = configurationBuilder.getConfiguration();
        int port = configuration.getInteger(EMBEDDED_WEB_SERVER_PORT_KEY, EMBEDDED_WEB_SERVER_PORT_DEFAULT);
        String webappContextPath = configuration.getString(EMBEDDED_WEB_SERVER_RESOURCE_BASE_KEY,
                EMBEDDED_WEB_SERVER_RESOURCE_BASE_DEFAULT);

        Server server = new Server(port);
        WebAppContext context = new WebAppContext(SRC_MAIN_WEBAPP, webappContextPath);

        copyConfigurationPrimersIntoServletContext(context);

        server.setHandler(context);
        try {
            server.start();
            server.join();
        } catch (Exception ex) {
            throw new IsisException("Unable to start Jetty server", ex);
        }
    }

    /**
     * Bound to the {@link WebAppContext} so that they can be used when bootstrapping. 
     * @param context
     */
    @SuppressWarnings("unchecked")
    private void copyConfigurationPrimersIntoServletContext(WebAppContext context) {
        List<ConfigurationPrimer> configurationPrimers = (List<ConfigurationPrimer>) (List<?>) runner.getOptionHandlers();
        context.setAttribute(WebAppConstants.CONFIGURATION_PRIMERS_KEY, configurationPrimers);
    }

    @SuppressWarnings("unused")
    private void copyDeploymentTypeIntoInitParams(WebAppContext context) {
        Map<String, String> initParams = CastUtils.cast(context.getInitParams());
        initParams = new HashMap<String, String>(initParams);
        context.setInitParams(initParams);
    }
}