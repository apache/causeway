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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.runtime.system.context.IsisContext;

import de.agilecoders.wicket.core.settings.ITheme;
import de.agilecoders.wicket.core.settings.ThemeProvider;
import de.agilecoders.wicket.themes.markup.html.bootstrap.BootstrapThemeTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchThemeProvider;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0
 */
@Log4j2
public class IsisWicketThemeSupportDefault implements IsisWicketThemeSupport {

    private static final BootswatchTheme BOOTSWATCH_THEME_DEFAULT = BootswatchTheme.Flatly;

    private final _Lazy<ThemeProvider> themeProvider = _Lazy.of(this::createThemeProvider); 


    @Override
    public ThemeProvider getThemeProvider() {
        return themeProvider.get();
    }

    @Override
    public List<String> getEnabledThemeNames() {
        final BootstrapThemeTheme bootstrapTheme = new BootstrapThemeTheme();
        List<BootswatchTheme> bootswatchThemes = Arrays.asList(BootswatchTheme.values());
        //        List<VegibitTheme> vegibitThemes = Arrays.asList(VegibitTheme.values());

        List<String> allThemes = new ArrayList<>();
        allThemes.add(bootstrapTheme.name());

        for (ITheme theme : bootswatchThemes) {
            allThemes.add(theme.name());
        }

        //        for (ITheme theme : vegibitThemes) {
        //            allThemes.add(theme.name());
        //        }

        allThemes = filterThemes(allThemes);

        return allThemes;
    }


    // -- HELPER

    private ThemeProvider createThemeProvider() {

        final String themeName = getConfiguration().getString(
                DEFAULT_THEME_KEY, BOOTSWATCH_THEME_DEFAULT.name());
        BootswatchTheme bootswatchTheme;
        try {
            bootswatchTheme = BootswatchTheme.valueOf(themeName);
        } catch(Exception ex) {
            bootswatchTheme = BOOTSWATCH_THEME_DEFAULT;
            log.warn("Did not recognise configured bootswatch theme '{}', defaulting to '{}'", 
                    themeName, 
                    bootswatchTheme);

        }

        return new BootswatchThemeProvider(bootswatchTheme);/* {
            @Override
            public ITheme byName(String name) {
                // legacy behavior
                return getThemeByName(name);
            }
        };*/
    }

    // legacy code ...    
    //    private ITheme getThemeByName(String themeName) {
    //        ITheme theme;
    //        try {
    //            if ("bootstrap-theme".equals(themeName)) {
    //                theme = new BootstrapThemeTheme();
    //            } else if (themeName.startsWith("veg")) {
    //                theme = VegibitTheme.valueOf(themeName);
    //            } else {
    //                theme = BootswatchTheme.valueOf(themeName);
    //            }
    //        } catch (Exception x) {
    //            LOG.warn("Cannot find a theme with name '{}' in all available theme providers: {}", themeName, x.getMessage());
    //            // fallback to Bootstrap default theme if the parsing by name failed somehow
    //            theme = new BootstrapThemeTheme();
    //        }
    //        return theme;
    //    }

    /**
     * Filters which themes to show in the drop up by using the provided values
     * in {@value #ENABLED_THEMES_KEY}
     *
     * @param allThemes All available themes
     * @return A list of all enabled themes
     */
    private List<String> filterThemes(List<String> allThemes) {
        List<String> enabledThemes;

        final String[] enabledThemesArray = getConfiguration().getList(ENABLED_THEMES_KEY);
        if (enabledThemesArray.length > 0) {
            final Set<String> enabledThemesSet = _NullSafe.stream(enabledThemesArray)
                    .collect(Collectors.toSet());

            Iterable<String> enabled = Iterables.filter(allThemes, new Predicate<String>() {
                @Override
                public boolean apply(String themeName) {
                    return enabledThemesSet.contains(themeName);
                }
            });

            enabledThemes = _Lists.newArrayList(enabled);
        } else {
            enabledThemes = allThemes;
        }

        return enabledThemes;
    }

    private IsisConfigurationLegacy getConfiguration() {
        return IsisContext.getConfiguration();
    }

}
