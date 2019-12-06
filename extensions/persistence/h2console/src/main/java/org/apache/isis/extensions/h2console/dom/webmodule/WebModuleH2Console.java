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
package org.apache.isis.extensions.h2console.dom.webmodule;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.val;

@Service
@Named("isisExtH2Console.WebModuleH2Console")
@Order(0)
@Log4j2
public class WebModuleH2Console implements WebModule  {

    private final static String SERVLET_NAME = "H2Console";
    private final static String SERVLET_CLASS_NAME = "org.h2.server.web.WebServlet";
    private final static String CONSOLE_PATH = "/db"; //XXX could be made a config value 

    private final IsisSystemEnvironment isisSystemEnvironment;
    private final IsisConfiguration isisConfiguration;

    @Inject
    public WebModuleH2Console(IsisSystemEnvironment isisSystemEnvironment, IsisConfiguration isisConfiguration) {
        this.isisSystemEnvironment = isisSystemEnvironment;
        this.isisConfiguration = isisConfiguration;
    }

    @Getter private LocalResourcePath localResourcePathIfEnabled;

    @Override
    public String getName() {
        return "H2Console";
    }

    @Override
    public void prepare(WebModuleContext ctx) {
        // nothing special required
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        val servlet = ctx.addServlet(SERVLET_NAME, SERVLET_CLASS_NAME);
        ctx.getServletRegistration(SERVLET_NAME)
        .addMapping(CONSOLE_PATH+"/*");

        servlet.setInitParameter("webAllowOthers", "true"); //XXX could be made a config value 

        return null; // does not provide a listener
    }

    @Override
    public boolean isApplicable(WebModuleContext ctx) {
        val enabled = canEnable(ctx);
        if(enabled) {
            localResourcePathIfEnabled = new LocalResourcePath(CONSOLE_PATH);
        }
        return enabled;
    }

    // -- HELPER

    private boolean canEnable(WebModuleContext ctx) {

        if(!isisSystemEnvironment.getDeploymentType().isPrototyping()) {
            return false;
        }

        val connectionUrl = isisConfiguration.getPersistor().getDatanucleus().getImpl().getJavax().getJdo().getOption().getConnectionUrl();

        val usesH2Connection = !_Strings.isNullOrEmpty(connectionUrl) && connectionUrl.contains(":h2:mem:");

        if(!usesH2Connection) {
            return false;
        }

        try {
            _Context.loadClass(SERVLET_CLASS_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
