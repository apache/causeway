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

package org.apache.isis.viewer.wicket.viewer;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

import org.apache.isis.applib.services.email.EmailService;
import org.apache.isis.applib.services.userreg.EmailNotificationService;
import org.apache.isis.core.runtime.services.email.EmailServiceDefault;
import org.apache.isis.core.runtime.services.userreg.EmailNotificationServiceDefault;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistrar;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.pages.EmailVerificationUrlService;
import org.apache.isis.viewer.wicket.ui.pages.PageClassList;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageNavigationService;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.EmailVerificationUrlServiceDefault;
import org.apache.isis.viewer.wicket.viewer.imagecache.ImageResourceCacheClassPath;
import org.apache.isis.viewer.wicket.viewer.registries.components.ComponentFactoryRegistrarDefault;
import org.apache.isis.viewer.wicket.viewer.registries.components.ComponentFactoryRegistryDefault;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassListDefault;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassRegistryDefault;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageNavigationServiceDefault;
import org.apache.isis.viewer.wicket.viewer.settings.WicketViewerSettingsDefault;

/**
 * To override
 * 
 * <pre>
 * final Module isisDefaults = new IsisWicketModule();
 *
 * final Module myAppOverrides = new AbstractModule() {
 *     @Override
 *     protected void configure() {
 *         bind(ComponentFactoryRegistrar.class).to(ComponentFactoryRegistrarForMyApp.class);
 *         bind(PageClassList.class).to(PageClassListForMyApp.class);
 *         ...  
 *         bind(String.class).annotatedWith(Names.named("applicationName")).toInstance("My App");
 *         bind(String.class).annotatedWith(Names.named("brandLogoHeader")).toInstance("/images/myapp-logo-header.png");
 *         bind(String.class).annotatedWith(Names.named("brandLogoSignin")).toInstance("/images/myapp-logo-signin.png");
 *         bind(String.class).annotatedWith(Names.named("applicationCss")).toInstance("css/application.css");
 *         bind(String.class).annotatedWith(Names.named("applicationJs")).toInstance("scripts/application.js");
 *         bind(String.class).annotatedWith(Names.named("welcomeMessage")).toInstance("Hello, welcome to my app");
 *         bind(String.class).annotatedWith(Names.named("aboutMessage")).toInstance("MyApp v1.0.0");
 *         bind(AppManifest.class).toInstance(new MyAppManifest());
 *      }
 *  };
 * final Module overridden = Modules.override(isisDefaults).with(myAppOverrides);
 * </pre>
 */
public class IsisWicketModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ComponentFactoryRegistry.class).to(ComponentFactoryRegistryDefault.class);
        bind(PageClassRegistry.class).to(PageClassRegistryDefault.class);
        bind(EmailVerificationUrlService.class).to(EmailVerificationUrlServiceDefault.class);
        bind(PageNavigationService.class).to(PageNavigationServiceDefault.class);
        bind(PageClassList.class).to(PageClassListDefault.class);
        bind(ComponentFactoryRegistrar.class).to(ComponentFactoryRegistrarDefault.class);
        bind(ImageResourceCache.class).to(ImageResourceCacheClassPath.class);
        bind(WicketViewerSettings.class).to(WicketViewerSettingsDefault.class);

        // these services need to be bound because they injected directly into
        // Wicket panels outside of the Isis runtime.
        bind(EmailService.class)
                .to(EmailServiceDefault.class);
        bind(EmailNotificationService.class)
                .to(EmailNotificationServiceDefault.class);

        bind(String.class).annotatedWith(Names.named("applicationName")).toInstance("Apache Isis ™");
        bind(String.class).annotatedWith(Names.named("applicationCss")).toProvider(Providers.of((String) null));
        bind(String.class).annotatedWith(Names.named("applicationJs")).toProvider(Providers.of((String)null));
        bind(String.class).annotatedWith(Names.named("welcomeMessage")).toProvider(Providers.of((String)null));
        bind(String.class).annotatedWith(Names.named("aboutMessage")).toProvider(Providers.of((String)null));
    }
}
