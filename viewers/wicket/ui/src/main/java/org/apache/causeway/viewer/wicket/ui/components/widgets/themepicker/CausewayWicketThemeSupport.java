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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.wicket.Component;
import org.apache.wicket.util.string.Strings;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.extern.slf4j.Slf4j;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.ITheme;
import de.agilecoders.wicket.core.settings.NoopThemeProvider;
import de.agilecoders.wicket.core.settings.ThemeProvider;

/**
 * @since 2.0
 */
@Service
@Named("causeway.viewer.wicket.CausewayWicketThemeSupport")
@Slf4j
public record CausewayWicketThemeSupport(
        ITheme defaultTheme,
        Map<String, ITheme> themeByNameLower,
        Map<String, ThemeProvider> providerByThemeNameLower) {

    @Inject
    public CausewayWicketThemeSupport(final CausewayConfiguration configuration, final ServiceRegistry serviceRegistry) {
        this(Args.create(configuration, serviceRegistry));
    }

    private CausewayWicketThemeSupport(final Args args) {
        this(args.defaultTheme, args.themeByNameLower, args.providerByThemeNameLower);
    }

    public ITheme byName(final String name) {
        if (!Strings.isEmpty(name)) {
            var theme = themeByNameLower.get(name.toLowerCase());
            if(theme!=null)
                return theme;
        }

        log.warn("'{}' theme not found amoung enabled {}, "
                + "falling back to '{}'",
                name,
                available().stream().map(ITheme::name).collect(Collectors.joining(", ")),
                defaultTheme.name());

        return defaultTheme;
    }

    public List<ITheme> available() {
        return themeByNameLower.values()
            .stream()
            .toList();
    }

    public List<String> availableNames() {
        return available()
                .stream()
                .map(ITheme::name)
                .toList();
    }

    public ThemeProvider compositeThemeProvider() {
        return new ThemeProvider() {
            @Override public ITheme defaultTheme() {
                return defaultTheme;
            }
            @Override public ITheme byName(final String name) {
                return CausewayWicketThemeSupport.this.byName(name);
            }
            @Override public List<ITheme> available() {
                return CausewayWicketThemeSupport.this.available();
            }
        };
    }

    /**
     * Required in order for ThemeProvider specific resources to be made available.
     *
     * <p> set during {@link Component#beforeRender()}, un-set during {@link Component#afterRender()}
     *
     * @see #unsetCustomThemeProvider()
     */
    public void setCustomThemeProvider(final ITheme theme) {
        Optional.ofNullable(providerByThemeNameLower.get(theme.name().toLowerCase()))
            .ifPresent(Bootstrap.getSettings()::setThemeProvider);
    }

    public void unsetCustomThemeProvider() {
        Bootstrap.getSettings().setThemeProvider(compositeThemeProvider());
    }

    // -- HELPER

    /**
     * Can be refactored/removed once we have flexible constructor bodies in Java.
     */
    private record Args(
            ITheme defaultTheme,
            Map<String, ITheme> themeByNameLower,
            Map<String, ThemeProvider> providerByThemeNameLower) {

        static Args create(
                final CausewayConfiguration configuration,
                final ServiceRegistry serviceRegistry) {

            var enabledThemeNamesLowercase = _NullSafe.stream(configuration.viewer().wicket().themes().enabled())
                    .map(String::toLowerCase)
                    .collect(Collectors.toCollection(HashSet::new));

            var providerByThemeNameLower = new LinkedHashMap<String, ThemeProvider>();
            var themeByNameLower = new LinkedHashMap<String, ITheme>();
            serviceRegistry.select(ThemeProvider.class)
                .forEach(provider->{
                    _NullSafe.stream(provider.available())
                        .filter(theme->enabledThemeNamesLowercase.contains(theme.name().toLowerCase()))
                        .forEach(theme->{
                            var themeKey = theme.name().toLowerCase();
                            themeByNameLower.put(themeKey, theme);
                            providerByThemeNameLower.put(themeKey, provider);
                        });
                });

            var defaultTheme = Optional.ofNullable(themeByNameLower.get(configuration.viewer().wicket().themes().initial()))
                .orElseGet(()->new NoopThemeProvider().defaultTheme());

            return new Args(defaultTheme, themeByNameLower, providerByThemeNameLower);
        }
    }

}
