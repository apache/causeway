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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.cookies.CookieUtils;

import org.apache.causeway.commons.internal.base._StableValue;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.ITheme;
import de.agilecoders.wicket.core.settings.SessionThemeProvider;
import de.agilecoders.wicket.core.util.Attributes;

/**
 * A panel used as a Navbar item to change the application theme/skin
 */
public class ThemeChooser
extends PanelAbstract<Void, IModel<Void>> {

    private static final long serialVersionUID = 1L;

    /**
     * The name of the cookie that stores the last user selection
     */
    private static final String CAUSEWAY_THEME_COOKIE_NAME = "causeway.viewer.wicket.themes.selected";

    private final _StableValue<CausewayWicketThemeSupport> themeSupport = new _StableValue<>();

    /**
     * Constructor
     *
     * @param id component id
     */
    public ThemeChooser(final String id) {
        super(id);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(getWicketViewerSettings().themes().showChooser());
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        initializeActiveThemeFromCookie();
        Wkt.ajaxEnable(this);
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        buildGui();
    }

    void buildGui() {
        final String activeThemeName = getActiveTheme().name();

        Wkt.listViewAdd(this, "themes", themeSupport().availableNames(), item->{

            final String themeName = item.getModelObject();

            // use Ajax link because Link's url looks like /object:3 and this confuses the browser
            var link = Wkt.link("themeLink", target->{
                    setActiveTheme(themeName);
                    target.add(getPage()); // repaint the whole page
                })
                .setBody(Model.of(themeName));

            if (themeName.equals(activeThemeName)) {
                Wkt.cssAppend(link, "active");
            }

            Wkt.add(item, link);
        });
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);
        tag.setName("li");
        Attributes.addClass(tag, "dropdown");
    }

    // -- HELPER

    private void initializeActiveThemeFromCookie() {
        // legacy code - perhaps could be refactored to use CookieThemeProvider
        if(!Bootstrap.getSettings().getActiveThemeProvider().getClass().equals(SessionThemeProvider.class))
            return;
        _Strings.nonEmpty(new CookieUtils().load(CAUSEWAY_THEME_COOKIE_NAME))
            .filter(themeSupport().availableNames()::contains)
            .ifPresent(this::setActiveTheme);
    }

    private void setActiveTheme(final String themeName) {
        ITheme theme = themeSupport().byName(themeName);
        if(theme==null)
            return;
        new CookieUtils().save(CAUSEWAY_THEME_COOKIE_NAME, themeName);
        Bootstrap.getSettings().getActiveThemeProvider().setActiveTheme(theme);
    }

    private ITheme getActiveTheme() {
        return Bootstrap.getSettings().getActiveThemeProvider().getActiveTheme();
    }

    private CausewayWicketThemeSupport themeSupport() {
        return themeSupport.orElseSet(()->MetaModelContext.instance()
            .flatMap(mmc->mmc.lookupService(CausewayWicketThemeSupport.class))
            .orElseThrow(()->_Exceptions.illegalState("no CausewayWicketThemeSupport found")));
    }

}
