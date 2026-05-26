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
package org.apache.causeway.viewer.wicket.ui;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.viewer.commons.services.CausewayModuleViewerCommonsServices;
import org.apache.causeway.viewer.wicket.model.CausewayModuleViewerWicketModel;
import org.apache.causeway.viewer.wicket.ui.app.logout.LogoutHandlerWkt;
import org.apache.causeway.viewer.wicket.ui.components.widgets.themepicker.CausewayWicketThemeSupport;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.ITheme;
import de.agilecoders.wicket.core.settings.NoopThemeProvider;
import de.agilecoders.wicket.core.settings.ThemeProvider;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchThemeProvider;

/**
 * @since 1.x {@index}
 */
@Configuration(proxyBeanMethods = false)
@Import({
        // Modules
        CausewayModuleViewerCommonsServices.class,
        CausewayModuleViewerWicketModel.class,

        // @Service's
        CausewayWicketThemeSupport.class,
        LogoutHandlerWkt.class,
})
public class CausewayModuleViewerWicketUi {

    @Bean
    ThemeProvider bootstrapDefaultThemeProvider() {
        return new BootstrapDefaultThemeProvider();
    }

    @Bean
    ThemeProvider bootswatchThemeProvider() {
        return new BootswatchThemeProvider(BootswatchTheme.Flatly);
    }

    /**
     * Default Bootstrap Theme
     *
     * @see NoopThemeProvider
     */
    private static class BootstrapDefaultThemeProvider implements ThemeProvider {
        private final ITheme theme = new BootstrapDefaultTheme();
        @Override public ITheme byName(final String name) {
            return theme;
        }
        @Override public List<ITheme> available() {
            return List.of(theme);
        }
        @Override public ITheme defaultTheme() {
            return theme;
        }
        private static final class BootstrapDefaultTheme implements ITheme {
            @Override public String name() {
                return "Default";
            }
            @Override public List<HeaderItem> getDependencies() {
                return List.of();
            }
            @Override public void renderHead(final IHeaderResponse response) {
                response.render(CssHeaderItem.forReference(Bootstrap.getSettings().getCssResourceReference()));
            }
            @Override public Iterable<String> getCdnUrls() {
                return List.of();
            }
        }
    }

}
