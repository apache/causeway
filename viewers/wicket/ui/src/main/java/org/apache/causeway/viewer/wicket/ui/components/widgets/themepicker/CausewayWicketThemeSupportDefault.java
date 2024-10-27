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
package org.apache.causeway.viewer.wicket.ui.components.widgets.themepicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.extern.log4j.Log4j2;

import de.agilecoders.wicket.core.settings.NoopThemeProvider;
import de.agilecoders.wicket.core.settings.ThemeProvider;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchThemeProvider;

/**
 * @since 2.0
 */
@Service
@Named("causeway.viewer.wicket.CausewayWicketThemeSupportDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class CausewayWicketThemeSupportDefault implements CausewayWicketThemeSupport {

    private final _Lazy<ThemeProviderComposite> themeProvider = _Lazy.threadSafe(this::createThemeProvider);

    @Inject private CausewayConfiguration configuration;
    @Inject private ServiceRegistry serviceRegistry;

    @Override
    public ThemeProvider getThemeProvider() {
        return themeProvider.get();
    }

    @Override
    public List<String> getEnabledThemeNames() {

        var composite = themeProvider.get();

        List<String> allThemes = composite.availableNames();

        allThemes = filterThemes(allThemes);

        return allThemes;
    }

    // -- HELPER

    private ThemeProviderComposite createThemeProvider() {

        var providerBeans = serviceRegistry.select(ThemeProvider.class);
        if (providerBeans.isEmpty()) {
            return ThemeProviderComposite.of(Can.ofSingleton(createFallbackThemeProvider()));
        }

        return ThemeProviderComposite.of(providerBeans);
    }

    private ThemeProvider createFallbackThemeProvider() {
        var themeName = configuration.getViewer().getWicket().getThemes().getInitial();
        if ("default".equalsIgnoreCase(themeName)) {
            // in effect uses the bootstrap 'default' theme
            return new NoopThemeProvider();
        }
        BootswatchTheme bootswatchTheme;
        try {
            bootswatchTheme = BootswatchTheme.valueOf(themeName);
        } catch (Exception ex) {
            bootswatchTheme = BootswatchTheme.Flatly;
            log.warn("Did not recognise configured bootswatch theme '{}', defaulting to '{}'",
                    themeName,
                    bootswatchTheme);

        }

        return new BootswatchThemeProvider(bootswatchTheme);
    }

    /**
     * Filters which themes to show in the drop up by using the provided values
     * in {@link CausewayConfiguration.Viewer.Wicket.Themes#getEnabled()}
     *
     * @param availableThemes All available themes
     * @return A list of all enabled themes
     */
    private List<String> filterThemes(final List<String> availableThemes) {

        var configuredThemes = configuration.getViewer().getWicket().getThemes().getEnabled();
        if (configuredThemes == null || configuredThemes.isEmpty()) {
            return Collections.emptyList();
        }

        var enabledThemes = new ArrayList<String>();
        availableThemes.stream()
                .filter(availableTheme -> configuredThemes.stream().anyMatch(configuredTheme -> configuredTheme.equalsIgnoreCase(availableTheme)))
                .forEach(enabledThemes::add);

        return enabledThemes;
    }

}
