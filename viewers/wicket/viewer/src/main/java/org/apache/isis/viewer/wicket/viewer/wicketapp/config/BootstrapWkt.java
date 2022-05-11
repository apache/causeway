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
package org.apache.isis.viewer.wicket.viewer.wicketapp.config;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.context.annotation.Configuration;

import org.apache.isis.viewer.wicket.model.isis.WicketApplicationInitializer;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.BootstrapBaseBehavior;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;

@Configuration
public class BootstrapWkt implements WicketApplicationInitializer {

    @Override
    public void init(final WebApplication webApplication) {
        final IBootstrapSettings settings = new BootstrapSettings();
        settings.setDeferJavascript(false);
        Bootstrap.install(webApplication, settings);

        webApplication.getHeaderContributorListeners().add(new IHeaderContributor() {
            private static final long serialVersionUID = 1L;
            @Override
            public void renderHead(final IHeaderResponse response) {
                BootstrapBaseBehavior bootstrapBaseBehavior = new BootstrapBaseBehavior();
                bootstrapBaseBehavior.renderHead(settings, response);
            }
        });
    }

}
