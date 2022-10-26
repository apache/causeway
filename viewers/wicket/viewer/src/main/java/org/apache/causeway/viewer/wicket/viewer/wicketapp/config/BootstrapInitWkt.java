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
package org.apache.causeway.viewer.wicket.viewer.wicketapp.config;

import javax.inject.Inject;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.viewer.wicket.model.causeway.WicketApplicationInitializer;
import org.apache.causeway.viewer.wicket.ui.components.widgets.themepicker.CausewayWicketThemeSupport;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.BootstrapBaseBehavior;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import lombok.val;

@Configuration
public class BootstrapInitWkt implements WicketApplicationInitializer {

    @Inject ServiceRegistry serviceRegistry;

    @Override
    public void init(final WebApplication webApplication) {
        val bsSettings = new BootstrapSettings();
        bsSettings.setDeferJavascript(false);
        Bootstrap.install(webApplication, bsSettings);

        webApplication.getHeaderContributorListeners().add(new IHeaderContributor() {
            private static final long serialVersionUID = 1L;
            @Override
            public void renderHead(final IHeaderResponse response) {
                new BootstrapBaseBehavior().renderHead(bsSettings, response);
            }
        });

        serviceRegistry.select(CausewayWicketThemeSupport.class)
        .getFirst()
        .ifPresent(themeSupport->{
            bsSettings.setThemeProvider(themeSupport.getThemeProvider());
        });
    }

}
