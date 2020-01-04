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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.util.string.Strings;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import de.agilecoders.wicket.core.settings.ITheme;
import de.agilecoders.wicket.core.settings.ThemeProvider;

/**
 * 
 * @since 2.0
 *
 */
@RequiredArgsConstructor(staticName = "of") @Log4j2
public class ThemeProviderComposite implements ThemeProvider {

    private final Can<ThemeProvider> themeProviders;
    
    private ITheme defaultTheme;
    private Map<String, ITheme> themesByName;
    private List<String> availableNames;
    private List<ITheme> availableThemes;
    
    @Override
    public ITheme byName(String name) {
        if (!Strings.isEmpty(name)) {
            ensureInit();
            val theme = themesByName.get(name.toLowerCase());
            if(theme!=null) {
                return theme;
            }
        }
        log.warn("'{}' theme not found amoung providers {} provinding {}, "
                + "using default '{}' instead", 
                name, 
                themeProviders, 
                available().stream().map(ITheme::name).collect(Collectors.joining(", ")),
                defaultTheme().name());
        
        return defaultTheme();
    }

    @Override
    public List<ITheme> available() {
        ensureInit();
        return availableThemes;
    }

    @Override
    public ITheme defaultTheme() {
        ensureInit();
        return defaultTheme;
    }
    
    public List<String> availableNames() {
        ensureInit();
        return availableNames;
    }
 
    // -- HELPER
    
    private void ensureInit() {
        if(themesByName!=null) {
            return;
        }
        themesByName = _Maps.newLinkedHashMap();
        themeProviders.forEach(themeProvider->{
            
            if(defaultTheme==null) {
                defaultTheme = themeProvider.defaultTheme();
            }
            
            themeProvider.available().forEach(theme->{
                themesByName.put(theme.name().toLowerCase(), theme);
            });
            
        });
        availableThemes = Collections.unmodifiableList(_Lists.newArrayList(themesByName.values()));
        availableNames = Collections.unmodifiableList(_Lists.map(availableThemes, ITheme::name));
    }
    

}
