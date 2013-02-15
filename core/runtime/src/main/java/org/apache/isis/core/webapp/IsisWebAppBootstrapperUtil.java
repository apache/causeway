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
package org.apache.isis.core.webapp;

import javax.servlet.ServletContext;

import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.NotFoundPolicy;


/**
 * Factored out of {@link IsisWebAppBootstrapper} in order that can
 * be reused by other bootstrappers (eg the wicket viewer's
 * <tt>IsisWicketApplication</tt>).
 */
public final class IsisWebAppBootstrapperUtil {
    
    private IsisWebAppBootstrapperUtil(){}

    public static void addConfigurationResourcesForViewers(final IsisConfigurationBuilder configurationLoader, final ServletContext servletContext) {
        addConfigurationResourcesForContextParam(configurationLoader, servletContext, "isis.viewers");
        addConfigurationResourcesForContextParam(configurationLoader, servletContext, "isis.viewer");
    }

    private static void addConfigurationResourcesForContextParam(final IsisConfigurationBuilder configurationLoader, final ServletContext servletContext, final String name) {
        final String viewers = servletContext.getInitParameter(name);
        if (viewers == null) {
            return;
        }
        for (final String viewer : viewers.split(",")) {
            configurationLoader.addConfigurationResource("viewer_" + viewer + ".properties", NotFoundPolicy.CONTINUE);
        }
    }


}
