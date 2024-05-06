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

import org.apache.wicket.protocol.http.WebApplication;

import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.viewer.wicket.model.causeway.WicketApplicationInitializer;

import de.agilecoders.wicket.webjars.WicketWebjars;
import de.agilecoders.wicket.webjars.settings.IWebjarsSettings;
import de.agilecoders.wicket.webjars.settings.WebjarsSettings;

@Configuration
@Import({
    WebjarsInitWkt.JavaScriptModuleMimeSupport.class
})
public class WebjarsInitWkt implements WicketApplicationInitializer {

    @Override
    public void init(final WebApplication webApplication) {
        final IWebjarsSettings settings = new WebjarsSettings();
        WicketWebjars.install(webApplication, settings);
    }

    @Configuration
    public static class JavaScriptModuleMimeSupport
    implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
        @Override
        public void customize(final ConfigurableServletWebServerFactory factory) {
            var mappings = _Casts.castTo(AbstractServletWebServerFactory.class, factory)
                .map(AbstractServletWebServerFactory::getMimeMappings)
                .orElseGet(()->new MimeMappings(MimeMappings.DEFAULT));
            mappings.remove("mjs");
            mappings.add("mjs", "application/javascript;charset=utf-8");
            factory.setMimeMappings(mappings);
        }
    }

}
