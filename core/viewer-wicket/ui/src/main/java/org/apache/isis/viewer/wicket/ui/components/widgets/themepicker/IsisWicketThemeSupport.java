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

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.factory.InstanceUtil;
import org.apache.isis.runtime.system.context.IsisContext;

import de.agilecoders.wicket.core.settings.ThemeProvider;
import lombok.val;

/**
 * @since 2.0
 */
public interface IsisWicketThemeSupport {

    // -- INTERFACE

    ThemeProvider getThemeProvider();
    List<String> getEnabledThemeNames();


    // -- LOOKUP

    static IsisWicketThemeSupport getInstance() {
        return _Context.computeIfAbsent(IsisWicketThemeSupport.class, ()->createInstance());
    }

    // -- FACTORY

    /*private*/ static IsisWicketThemeSupport createInstance() {


        val configuration = IsisContext.getConfiguration();

        val themeSupportClassName = configuration.getViewer().getWicket().getThemes().getProvider();

        try {

            val themeSupport = (IsisWicketThemeSupport) InstanceUtil.createInstance(themeSupportClassName);
            return themeSupport;

        } catch (Exception e) {

            val log = org.apache.logging.log4j.LogManager.getLogger(IsisWicketThemeSupport.class);
            log.warn("Could not instantiate configured theme support class '{}', defaulting to '{}'",
                    themeSupportClassName,
                    IsisWicketThemeSupportDefault.class.getName());
        }

        return (IsisWicketThemeSupport) InstanceUtil.createInstance(IsisWicketThemeSupportDefault.class);
    }


}
