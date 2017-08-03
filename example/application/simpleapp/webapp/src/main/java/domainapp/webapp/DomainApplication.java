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
package domainapp.webapp;

import java.io.InputStream;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.google.inject.util.Providers;

import org.apache.isis.viewer.wicket.viewer.IsisWicketApplication;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchThemeProvider;

/**
 * As specified in <tt>web.xml</tt>.
 * 
 * <p>
 * See:
 * <pre>
 * &lt;filter>
 *   &lt;filter-name>wicket&lt;/filter-name>
 *    &lt;filter-class>org.apache.wicket.protocol.http.WicketFilter&lt;/filter-class>
 *    &lt;init-param>
 *      &lt;param-name>applicationClassName&lt;/param-name>
 *      &lt;param-value>domainapp.webapp.DomainApplication&lt;/param-value>
 *    &lt;/init-param>
 * &lt;/filter>
 * </pre>
 * 
 */
public class DomainApplication extends IsisWicketApplication {

    private static final long serialVersionUID = 1L;

    @Override
    protected void init() {
        super.init();

        IBootstrapSettings settings = Bootstrap.getSettings();
        settings.setThemeProvider(new BootswatchThemeProvider(BootswatchTheme.Flatly));
    }

    private static final String APP_NAME = "Simple App";

    @Override
    protected Module newIsisWicketModule() {
        final Module isisDefaults = super.newIsisWicketModule();
        
        final Module overrides = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).annotatedWith(Names.named("applicationName")).toInstance(APP_NAME);
                bind(String.class).annotatedWith(Names.named("applicationCss")).toInstance("css/application.css");
                bind(String.class).annotatedWith(Names.named("applicationJs")).toInstance("scripts/application.js");
                bind(String.class).annotatedWith(Names.named("welcomeMessage")).toInstance(readLines(getClass(), "welcome.html", "This is a simple app"));
                bind(String.class).annotatedWith(Names.named("aboutMessage")).toInstance(APP_NAME);
                bind(InputStream.class).annotatedWith(Names.named("metaInfManifest")).toProvider(
                        Providers.of(getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF")));
                // if uncommented, then overrides isis.appManifest in config file.
                // bind(AppManifest.class).toInstance(new DomainAppAppManifest());
            }
        };

        return Modules.override(isisDefaults).with(overrides);
    }


}
