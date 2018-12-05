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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.runtime.system.context.IsisContext;

import de.agilecoders.wicket.core.settings.ThemeProvider;

/**
 * @since 2.0.0-M2
 */
public interface IsisWicketThemeSupport {

    // -- INTERFACE
    
    ThemeProvider getThemeProvider();
    List<String> getEnabledThemeNames();
    
    // -- CONSTANTS
    
    /**
     * A configuration setting which value could be a comma separated list of enabled theme names
     */
    static final String ENABLED_THEMES_KEY  = "isis.viewer.wicket.themes.enabled";
    static final String DEFAULT_THEME_KEY  = "isis.viewer.wicket.themes.initial";
    static final Class<? extends IsisWicketThemeSupport> THEME_SUPPORT_DEFAULT_CLASS = 
            IsisWicketThemeSupportDefault.class;
    
    // -- LOOKUP
    
    static IsisWicketThemeSupport getInstance() {
        return _Context.computeIfAbsent(IsisWicketThemeSupport.class, __->createInstance());
    }
    
    // -- FACTORY
    
    /*private*/ static IsisWicketThemeSupport createInstance() {
        
        
        final IsisConfiguration configuration = IsisContext.getConfiguration();
        
        final String themeSupportClassName = configuration.getString(
                "isis.viewer.wicket.themes.provider", THEME_SUPPORT_DEFAULT_CLASS.getName());
        
        try {
            
            IsisWicketThemeSupport themeSupport = 
                    (IsisWicketThemeSupport) InstanceUtil.createInstance(themeSupportClassName);
            return themeSupport;
            
        } catch (Exception e) {
            final Logger LOG = LoggerFactory.getLogger(IsisWicketThemeSupport.class);
            LOG.warn("Could not instantiate configured theme support class '{}', defaulting to '{}'",
                    themeSupportClassName,
                    THEME_SUPPORT_DEFAULT_CLASS.getName());
        }
        
        return (IsisWicketThemeSupport) InstanceUtil.createInstance(THEME_SUPPORT_DEFAULT_CLASS);
    }
    

}
