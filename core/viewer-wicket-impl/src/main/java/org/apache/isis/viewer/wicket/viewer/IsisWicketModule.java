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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.name.Names;

import org.apache.isis.applib.services.email.EmailService;
import org.apache.isis.applib.services.userreg.EmailNotificationService;
import org.apache.isis.core.runtime.services.email.EmailServiceDefault;
import org.apache.isis.core.runtime.services.userreg.EmailNotificationServiceDefault;
import org.apache.isis.core.commons.config.IsisConfiguration;
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

import static org.apache.isis.viewer.wicket.viewer.IsisWicketApplication.readLines;

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
 *      }
 *  };
 * final Module overridden = Modules.override(isisDefaults).with(myAppOverrides);
 * </pre>
 */
public class IsisWicketModule extends AbstractModule {

    private ServletContext servletContext;
    private IsisConfiguration isisConfigIfAny;

    public IsisWicketModule(
            final ServletContext servletContext,
            final IsisConfiguration isisConfigurationIfAny) {
        this.servletContext = servletContext;
        this.isisConfigIfAny = isisConfigurationIfAny;
    }

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

        if(isisConfigIfAny != null) {
            bind(String.class).annotatedWith(Names.named("applicationName"))
                    .toProvider(
                            new Provider<String>() {
                                @Override public String get() {
                                    return isisConfigIfAny.getString("isis.viewer.wicket.application.name", "Apache Isis ™");
                                }
                            });
            bind(String.class).annotatedWith(Names.named("brandLogoHeader"))
                    .toProvider(
                            new Provider<String>() {
                                @Override public String get() {
                                    return isisConfigIfAny.getString("isis.viewer.wicket.application.brandLogoHeader");
                                }
                            });

            bind(String.class).annotatedWith(Names.named("brandLogoSignin"))
                    .toProvider(
                            new Provider<String>() {
                                @Override public String get() {
                                    return isisConfigIfAny.getString("isis.viewer.wicket.application.brandLogoSignin");
                                }
                            });

            bind(String.class).annotatedWith(Names.named("applicationCss"))
                    .toProvider(
                            new Provider<String>() {
                                @Override public String get() {
                                    return isisConfigIfAny.getString("isis.viewer.wicket.application.css", "css/application.css");
                                }
                            });
            bind(String.class).annotatedWith(Names.named("applicationJs"))
                    .toProvider(
                            new Provider<String>() {
                                @Override public String get() {
                                    return isisConfigIfAny.getString("isis.viewer.wicket.application.js", "css/application.js");
                                }
                            });
            bind(String.class).annotatedWith(Names.named("aboutMessage"))
                    .toProvider(
                            new Provider<String>() {
                                @Override public String get() {
                                    return isisConfigIfAny.getString("isis.viewer.wicket.application.about",
                                            isisConfigIfAny.getString("isis.viewer.wicket.application.name"));
                                }
                            });
            final String welcomeFile = isisConfigIfAny.getString("isis.viewer.wicket.welcome.file", "welcome.html");
            bind(String.class).annotatedWith(Names.named("welcomeMessage"))
                    .toProvider(
                            new Provider<String>() {
                        @Override public String get() {
                            final String fallback = isisConfigIfAny.getString("isis.viewer.wicket.welcome.text");
                            final URL resource;
                            try {
                                resource = servletContext.getResource(prefix("/", welcomeFile));
                                return readLines(resource, fallback);
                            } catch (MalformedURLException e) {
                                return fallback;
                            }
                        }
                    });
            bind(String.class).annotatedWith(Names.named("applicationVersion"))
                    .toProvider(
                            new Provider<String>() {
                        @Override public String get() {
                            return isisConfigIfAny.getString("isis.viewer.wicket.application.version");
                        }
                    });
        }
        bind(InputStream.class).annotatedWith(Names.named("metaInfManifest"))
                .toProvider(new Provider<InputStream>() {
                    @Override public InputStream get() {
                        return servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
                    }
                });
    }

    private static String prefix(final String prefix, final String text) {
        return text.startsWith(prefix) ? text : prefix + text;
    }
}
