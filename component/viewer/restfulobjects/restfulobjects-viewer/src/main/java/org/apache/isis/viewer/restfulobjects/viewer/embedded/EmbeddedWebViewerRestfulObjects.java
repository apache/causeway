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
package org.apache.isis.viewer.restfulobjects.viewer.embedded;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;

import org.apache.isis.core.commons.lang.MapUtils;
import org.apache.isis.core.webapp.content.ResourceCachingFilter;
import org.apache.isis.core.webapp.content.ResourceServlet;
import org.apache.isis.runtimes.dflt.runtime.viewer.web.WebAppSpecification;
import org.apache.isis.runtimes.dflt.runtime.web.EmbeddedWebViewer;
import org.apache.isis.runtimes.dflt.webapp.IsisSessionFilter;
import org.apache.isis.runtimes.dflt.webapp.IsisWebAppBootstrapper;
import org.apache.isis.viewer.restfulobjects.viewer.RestfulObjectsApplication;
import org.apache.isis.viewer.restfulobjects.viewer.authentication.AuthenticationSessionStrategyTrusted;

final class EmbeddedWebViewerRestfulObjects extends EmbeddedWebViewer {
    @Override
    public WebAppSpecification getWebAppSpecification() {
        final WebAppSpecification webAppSpec = new WebAppSpecification();

        webAppSpec.addServletContextListener(IsisWebAppBootstrapper.class);
        
        webAppSpec.addContextParams("isis.viewers", "restfulobjects");

        webAppSpec.addContextParams(RestfulObjectsViewerInstaller.JAVAX_WS_RS_APPLICATION, RestfulObjectsApplication.class.getName());

        webAppSpec.addFilterSpecification(IsisSessionFilter.class, 
                MapUtils.asMap(
                        IsisSessionFilter.AUTHENTICATION_SESSION_STRATEGY_KEY, AuthenticationSessionStrategyTrusted.class.getName(),
                        IsisSessionFilter.WHEN_NO_SESSION_KEY, IsisSessionFilter.WhenNoSession.CONTINUE.name().toLowerCase()), 
                RestfulObjectsViewerInstaller.EVERYTHING);

        webAppSpec.addFilterSpecification(ResourceCachingFilter.class, RestfulObjectsViewerInstaller.STATIC_CONTENT);
        webAppSpec.addServletSpecification(ResourceServlet.class, RestfulObjectsViewerInstaller.STATIC_CONTENT);

        
        webAppSpec.addServletContextListener(ResteasyBootstrap.class);
        webAppSpec.addServletSpecification(HttpServletDispatcher.class, RestfulObjectsViewerInstaller.ROOT);


        return webAppSpec;
    }
}