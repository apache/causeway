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
package org.apache.isis.core.webapp.modules;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.isis.core.webapp.IsisWebAppBootstrapper;

/**
 * Package private mixin for WebModule implementing WebModule.
 * @since 2.0.0
 */
final class WebModule_FallbackBootstrapper implements WebModule  {
    
    @Override
    public String getName() {
        return "Fallback Bootstrapper";
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {
        ctx.setInitParameter("deploymentType", "SERVER_PROTOTYPE");
        return ctx.createListener(IsisWebAppBootstrapper.class);
    }

    @Override
    public boolean isApplicable(ServletContext ctx) {
        // not required if another bootstrapper module is on the context 
        // e.g. the Wicket module
        return ctx.getAttribute("bootstrapper")!=null;
    }
    
}
