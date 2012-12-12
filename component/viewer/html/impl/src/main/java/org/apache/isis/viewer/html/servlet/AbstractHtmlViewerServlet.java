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

package org.apache.isis.viewer.html.servlet;

import javax.servlet.http.HttpServlet;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.html.PathBuilder;
import org.apache.isis.viewer.html.PathBuilderDefault;
import org.apache.isis.viewer.html.component.html.HtmlComponentFactory;

public abstract class AbstractHtmlViewerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private PathBuilder pathBuilder;
    private HtmlComponentFactory componentFactory;

    protected HtmlComponentFactory getHtmlComponentFactory() {
        if(componentFactory == null) {
            componentFactory = new HtmlComponentFactory(getPathBuilder());
        }
        return componentFactory;
    }

    protected PathBuilder getPathBuilder() {
        if (pathBuilder != null) {
            return pathBuilder;
        }
        return pathBuilder = new PathBuilderDefault(getServletContext());
    }


    /**
     * Convenience.
     */
    protected String pathTo(final String prefix) {
        return getPathBuilder().pathTo(prefix);
    }

    // //////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////////

    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

    protected IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

    protected AuthenticationManager getAuthenticationManager() {
        return IsisContext.getAuthenticationManager();
    }

    protected DeploymentType getDeploymentType() {
        return IsisContext.getDeploymentType();
    }

}
