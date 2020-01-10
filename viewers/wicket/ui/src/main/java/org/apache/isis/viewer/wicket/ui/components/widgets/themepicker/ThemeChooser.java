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

import javax.inject.Inject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.string.Strings;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.runtime.session.IsisSessionFactory;

import lombok.Getter;
import lombok.val;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.ActiveThemeProvider;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;
import de.agilecoders.wicket.core.settings.ITheme;
import de.agilecoders.wicket.core.settings.SessionThemeProvider;
import de.agilecoders.wicket.core.settings.SingleThemeProvider;
import de.agilecoders.wicket.core.util.Attributes;
import de.agilecoders.wicket.themes.markup.html.bootstrap.BootstrapThemeTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchThemeProvider;
import de.agilecoders.wicket.themes.markup.html.vegibit.VegibitTheme;
import de.agilecoders.wicket.themes.markup.html.vegibit.VegibitThemeProvider;

/**
 * A panel used as a Navbar item to change the application theme/skin
 */
public class ThemeChooser extends Panel {

    private static final long serialVersionUID = 1L;

    /**
     * The name of the cookie that stores the last user selection
     */
    private static final String ISIS_THEME_COOKIE_NAME = "isis.viewer.wicket.themes.selected";

    /**
     * Constructor
     *
     * @param id component id
     */
    public ThemeChooser(String id) {
        super(id);

        final ActiveThemeProvider activeThemeProvider = getActiveThemeProvider();
        if(activeThemeProvider.getClass() == SessionThemeProvider.class) {
            initializeActiveThemeFromCookie();
        } else {
            // if anything other than the default, then we do NOT initialize
            // (on the assumption that it is a persistent store and we don't want to overwrite).
        }

        ListView<String> themesView = new ListView<String>("themes", getThemeSupport().getEnabledThemeNames()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<String> item) {
                final String themeName = item.getModelObject();

                if (themeName.equals(getActiveThemeProvider().getActiveTheme().name())) {
                    item.add(AttributeModifier.append("class", "active"));
                }
                item.add(new AjaxLink<Void>("themeLink") {
                    private static final long serialVersionUID = 1L;
                    // use Ajax link because Link's url looks like /ENTITY:3 and this confuses the browser
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        setActiveTheme(themeName);
                        saveActiveThemeToCookie(themeName);
                        target.add(getPage()); // repaint the whole page
                    }
                }.setBody(Model.of(themeName)));
            }
        };
        add(themesView);
    }

    private void saveActiveThemeToCookie(String themeName) {
        CookieUtils cookieUtils = new CookieUtils();
        cookieUtils.save(ISIS_THEME_COOKIE_NAME, themeName);
    }

    private void initializeActiveThemeFromCookie() {
        CookieUtils cookieUtils = new CookieUtils();
        String activeTheme = cookieUtils.load(ISIS_THEME_COOKIE_NAME);
        if (!Strings.isEmpty(activeTheme)) {
            
            val isAvailable = getThemeSupport().getThemeProvider().available().stream()
                    .anyMatch(theme->activeTheme.equals(theme.name()));
            
            if(isAvailable) {
                setActiveTheme(activeTheme);
            }
        }
    }

    private void setActiveTheme(String activeTheme) {
        IBootstrapSettings bootstrapSettings = Bootstrap.getSettings();
        ITheme theme = getThemeSupport().getThemeProvider().byName(activeTheme);
        getActiveThemeProvider().setActiveTheme(theme);
        if (theme instanceof BootstrapThemeTheme) {
            bootstrapSettings.setThemeProvider(new SingleThemeProvider(theme));
        } else if (theme instanceof BootswatchTheme) {
            bootstrapSettings.setThemeProvider(new BootswatchThemeProvider((BootswatchTheme) theme));
        } else if (theme instanceof VegibitTheme) {
            bootstrapSettings.setThemeProvider(new VegibitThemeProvider((VegibitTheme) theme));
        }
    }

    private ActiveThemeProvider getActiveThemeProvider() {
        return Bootstrap.getSettings().getActiveThemeProvider();
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);

        tag.setName("li");
        Attributes.addClass(tag, "dropdown");
    }


    @Override
    protected void onConfigure() {
        super.onConfigure();

        boolean shouldShow = getConfiguration().getViewer().getWicket().getThemes().isShowChooser();
        setVisible(shouldShow);
    }

    @Inject @Getter private IsisWicketThemeSupport themeSupport;
    @Inject @Getter private IsisConfiguration configuration;
    @Inject @Getter private IsisSessionFactory isisSessionFactory;
}
