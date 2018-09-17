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
import com.google.common.collect.Lists;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(ThemeChooser.class);

    /**
     * A configuration setting which value determines whether the theme chooser should be available in the footer
     */
    private static final String SHOW_THEME_PICKER_KEY = "isis.viewer.wicket.themes.showChooser";
    private static final boolean SHOW_THEME_PICKER_DEFAULT = false;

    /**
     * A configuration setting which value could be a comma separated list of enabled theme names
     */
    private static final String ENABLED_THEMES_KEY  = "isis.viewer.wicket.themes.enabled";

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

        ListView<String> themesView = new ListView<String>("themes", getThemeNames()) {

            @Override
            protected void populateItem(ListItem<String> item) {
                final String themeName = item.getModelObject();

                if (themeName.equals(getActiveThemeProvider().getActiveTheme().name())) {
                    item.add(AttributeModifier.append("class", "active"));
                }
                item.add(new AjaxLink<Void>("themeLink") {
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
            setActiveTheme(activeTheme);
        }
    }

    private void setActiveTheme(String activeTheme) {
        IBootstrapSettings bootstrapSettings = Bootstrap.getSettings();
        ITheme theme = getThemeByName(activeTheme);
        getActiveThemeProvider().setActiveTheme(theme);
        if (theme instanceof BootstrapThemeTheme) {
            bootstrapSettings.setThemeProvider(new SingleThemeProvider(theme));
        } else if (theme instanceof BootswatchTheme) {
            bootstrapSettings.setThemeProvider(new BootswatchThemeProvider((BootswatchTheme) theme));
        } else if (theme instanceof VegibitTheme) {
            bootstrapSettings.setThemeProvider(new VegibitThemeProvider((VegibitTheme) theme));
        }
    }

    private ITheme getThemeByName(String themeName) {
        ITheme theme;
        try {
            if ("bootstrap-theme".equals(themeName)) {
                theme = new BootstrapThemeTheme();
            } else if (themeName.startsWith("veg")) {
                theme = VegibitTheme.valueOf(themeName);
            } else {
                theme = BootswatchTheme.valueOf(themeName);
            }
        } catch (Exception x) {
            LOG.warn("Cannot find a theme with name '{}' in all available theme providers: {}", themeName, x.getMessage());
            // fallback to Bootstrap default theme if the parsing by name failed somehow
            theme = new BootstrapThemeTheme();
        }
        return theme;
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

    private List<String> getThemeNames() {
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

            enabledThemes = Lists.newArrayList(enabled);
        } else {
            enabledThemes = allThemes;
        }

        return enabledThemes;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        boolean shouldShow = getConfiguration().getBoolean(SHOW_THEME_PICKER_KEY, SHOW_THEME_PICKER_DEFAULT);
        setVisible(shouldShow);
    }

    private IsisConfiguration getConfiguration() {
        return getIsisSessionFactory().getConfiguration();
    }

    IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }
}
