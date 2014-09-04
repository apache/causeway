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

package org.apache.isis.core.webserver.embedded.jetty;

import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;

import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.runtime.viewer.web.FilterSpecification;
import org.apache.isis.core.runtime.viewer.web.ServletSpecification;
import org.apache.isis.core.runtime.viewer.web.WebAppSpecification;
import org.apache.isis.core.runtime.web.EmbeddedWebServerAbstract;
import org.apache.isis.core.webserver.WebServerConstants;
import org.apache.isis.core.webserver.WebServerException;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedWebServerJetty extends EmbeddedWebServerAbstract {
    private final static Logger LOG = LoggerFactory.getLogger(EmbeddedWebServerJetty.class);

    // ///////////////////////////////////////////////////////
    // init, shutdown
    // ///////////////////////////////////////////////////////

    @Override
    public void init() {
        super.init();

        final HandlerList handlers = createHandlers();

        final ContextHandler contextHandler = createContextHandler(handlers);

        startServer(contextHandler);
    }

    private HandlerList createHandlers() {
        final HandlerList handlers = new HandlerList();

        addResourcesAndWelcomeFiles(handlers);

        final ServletHandler servletHandler = new ServletHandler();
        addServletsAndFilters(servletHandler);

        final SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setHandler(servletHandler);
        handlers.addHandler(sessionHandler);

        // commenting out; this grabs '/' but we might want to use it ourselves,
        // instead?
        // handlers.addHandler(new DefaultHandler());

        // TODO use properties to set up
        final RequestLogHandler requestLogHandler = new RequestLogHandler();
        handlers.addHandler(requestLogHandler);
        final NCSARequestLog requestLog = new NCSARequestLog("./logs/jetty-yyyy_mm_dd.request.log");
        requestLog.setRetainDays(90);
        requestLog.setAppend(true);
        requestLog.setExtended(false);
        requestLog.setLogTimeZone("GMT");
        requestLogHandler.setRequestLog(requestLog);

        return handlers;
    }

    /**
     * TODO: the welcome files don't seem to be picked up.
     * 
     * <p>
     * not sure if meant to add welcome files here or at the context handler
     * level, in fact, doesn't seem to work even when register in both...
     * 
     * @see #setWelcomeFiles(ContextHandler)
     */
    private void addResourcesAndWelcomeFiles(final HandlerList handlers) {
        for (final WebAppSpecification specification : getSpecifications()) {
            final List<String> files = specification.getWelcomeFiles();
            final String[] welcomeFiles = files.toArray(new String[files.size()]);
            for (final String resourcePath : specification.getResourcePaths()) {
                final ResourceHandler resourceHandler = new ResourceHandler();
                resourceHandler.setResourceBase(resourcePath);
                resourceHandler.setWelcomeFiles(welcomeFiles);
                handlers.addHandler(resourceHandler);
            }
        }
    }

    private void addServletsAndFilters(final ServletHandler servletHandler) {
        for (final WebAppSpecification requirement : getSpecifications()) {
            addServletMappings(servletHandler, requirement);
            addFilterMappings(servletHandler, requirement);
        }
    }

    private void addServletMappings(final ServletHandler servletHandler, final WebAppSpecification webAppSpec) {
        for (final ServletSpecification servletSpec : webAppSpec.getServletSpecifications()) {

            final ServletHolder servletHolder = new ServletHolder("servlet", (Class<? extends Servlet>) servletSpec.getServletClass());
            servletHolder.setInitParameters(servletSpec.getInitParams());
            servletHandler.addServlet(servletHolder);

            final ServletMapping servletMapping = new ServletMapping();
            servletMapping.setServletName(servletHolder.getName());
            servletMapping.setPathSpecs(servletSpec.getPathSpecs().toArray(new String[] {}));

            servletHandler.addServletMapping(servletMapping);
        }
    }

    private void addFilterMappings(final ServletHandler servletHandler, final WebAppSpecification webAppSpec) {
        for (final FilterSpecification filterSpec : webAppSpec.getFilterSpecifications()) {

            final FilterHolder filterHolder = new FilterHolder((Class<? extends Filter>) filterSpec.getFilterClass());
            filterHolder.setInitParameters(filterSpec.getInitParams());
            servletHandler.addFilter(filterHolder);

            final FilterMapping filterMapping = new FilterMapping();
            filterMapping.setFilterName(filterHolder.getName());
            filterMapping.setPathSpecs(filterSpec.getPathSpecs().toArray(new String[] {}));
            filterMapping.setDispatches(FilterMapping.DEFAULT);
            servletHandler.addFilterMapping(filterMapping);
        }
    }

    private ContextHandler createContextHandler(final HandlerList handlers) {
        final ContextHandler contextHandler = buildContextHandler(handlers);
        addContextParams(contextHandler);
        addServletContextListeners(contextHandler);
        setWelcomeFiles(contextHandler);
        return contextHandler;
    }

    private ContextHandler buildContextHandler(final HandlerList handlers) {
        final ContextHandler contextHandler = new ContextHandler("/");
        contextHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
        contextHandler.setHandler(handlers);
        return contextHandler;
    }

    private void addContextParams(final ContextHandler contextHandler) {
        Map<String, String> initParams = contextHandler.getInitParams();
        for (final WebAppSpecification specification : getSpecifications()) {
            initParams.putAll(specification.getContextParams());
        }
    }

    private void addServletContextListeners(final ContextHandler contextHandler) {
        for (final WebAppSpecification specification : getSpecifications()) {
            for (final Class<?> servletContextListenerClass : specification.getServletContextListeners()) {
                final ServletContextListener servletContext = (ServletContextListener) InstanceUtil.createInstance(servletContextListenerClass);
                contextHandler.addEventListener(servletContext);
            }
        }
    }

    /**
     * TODO: this doesn't seem to be being picked up
     * 
     * <p>
     * not sure if meant to add welcome files here or at the resource base
     * level, in fact, doesn't seem to work even when register in both...
     * 
     * @see #addResourcesAndWelcomeFiles(HandlerList)
     */
    private void setWelcomeFiles(final ContextHandler contextHandler) {
        for (final WebAppSpecification specification : getSpecifications()) {
            contextHandler.setWelcomeFiles(specification.getWelcomeFiles().toArray(new String[] {}));
        }
    }

    private void startServer(final ContextHandler contextHandler) {
        final int port = getConfiguration().getInteger(WebServerConstants.EMBEDDED_WEB_SERVER_PORT_KEY, WebServerConstants.EMBEDDED_WEB_SERVER_PORT_DEFAULT);
        if (LOG.isInfoEnabled()) {
            LOG.info("Starting web server on http://localhost:" + port);
            for (final WebAppSpecification specification : getSpecifications()) {
                final String logHint = specification.getLogHint();
                if (logHint != null) {
                    LOG.info(logHint);
                }
            }
        }
        try {
            final Server server = new Server(port);
            server.addBean(contextHandler);
            server.start();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new WebServerException("Web server failed to start", e);
        }
    }

    @Override
    public void shutdown() {
        // does nothing
    }

}
