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
package org.apache.isis.viewer.wicket.ui.components.widgets.themepicker;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import de.agilecoders.wicket.core.settings.ThemeProvider;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchThemeProvider;

/**
 * @since 2.0
 */
@Service
@Named("isis.viewer.wicket.IsisWicketThemeSupportDefault")
@javax.annotation.Priority(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class IsisWicketThemeSupportDefault implements IsisWicketThemeSupport {

    private final _Lazy<ThemeProviderComposite> themeProvider = _Lazy.of(this::createThemeProvider);

    @Inject private IsisConfiguration configuration;
    @Inject private ServiceRegistry serviceRegistry;

    @Override
    public ThemeProvider getThemeProvider() {
        return themeProvider.get();
    }

    @Override
    public List<String> getEnabledThemeNames() {

        val composite = themeProvider.get();

        List<String> allThemes = composite.availableNames();

        allThemes = filterThemes(allThemes);

        return allThemes;
    }


    // -- HELPER

    private ThemeProviderComposite createThemeProvider() {

        val providerBeans = serviceRegistry.select(ThemeProvider.class);
        if(providerBeans.isEmpty()) {
            return ThemeProviderComposite.of(Can.ofSingleton(createFallbackThemeProvider()));
        }

        return ThemeProviderComposite.of(providerBeans);
    }

    private ThemeProvider createFallbackThemeProvider() {
        val themeName = configuration.getViewer().getWicket().getThemes().getInitial();
        BootswatchTheme bootswatchTheme;
        try {
            bootswatchTheme = BootswatchTheme.valueOf(themeName);
        } catch(Exception ex) {
            bootswatchTheme = BootswatchTheme.Flatly;
            log.warn("Did not recognise configured bootswatch theme '{}', defaulting to '{}'",
                    themeName,
                    bootswatchTheme);

        }

        return new BootswatchThemeProvider(bootswatchTheme);
    }

    /**
     * Filters which themes to show in the drop up by using the provided values
     * in {@link IsisConfiguration.Viewer.Wicket.Themes#getEnabled()}
     *
     * @param allThemes All available themes
     * @return A list of all enabled themes
     */
    private List<String> filterThemes(List<String> allThemes) {
        List<String> enabledThemes;

        final Set<String> enabledThemesSet =
        _NullSafe.stream(configuration.getViewer().getWicket().getThemes().getEnabled())
        .collect(Collectors.toSet());

        if (enabledThemesSet.size() > 0) {

            enabledThemes = allThemes.stream()
                    .filter(enabledThemesSet::contains)
                    .collect(Collectors.toList());

        } else {
            enabledThemes = allThemes;
        }

        return enabledThemes;
    }

}
