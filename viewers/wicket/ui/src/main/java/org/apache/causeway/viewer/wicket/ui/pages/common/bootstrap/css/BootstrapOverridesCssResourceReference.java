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
package org.apache.causeway.viewer.wicket.ui.pages.common.bootstrap.css;

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.request.resource.ResourceReference;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;
import de.agilecoders.wicket.core.settings.ITheme;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * A CSS resource reference that provides CSS rules which override the CSS rules
 * provided by the currently active Bootstrap theme.
 * Usually the overrides rules are about sizes and weights, but should not change any colors
 */
public class BootstrapOverridesCssResourceReference extends CssResourceReference {

    private static final long serialVersionUID = 1L;

    @Getter(lazy = true) @Accessors(fluent = true)
    private static final BootstrapOverridesCssResourceReference instance =
        new BootstrapOverridesCssResourceReference();

    public static CssHeaderItem asHeaderItem() {
        return CssHeaderItem.forReference(BootstrapOverridesCssResourceReference.instance());
    }

    private BootstrapOverridesCssResourceReference() {
        super(BootstrapOverridesCssResourceReference.class, "bootstrap-overrides-all-v2.css");
    }

    /**
     * Contributes theme specific Bootstrap CSS overrides if there is such resource
     *
     * @param response The header response to contribute to
     */
    public static void contributeThemeSpecificOverrides(
            final Application application,
            final IHeaderResponse response) {

        final IBootstrapSettings bootstrapSettings = Bootstrap.getSettings(application);

        final ITheme activeTheme = bootstrapSettings.getActiveThemeProvider().getActiveTheme();
        final String name = activeTheme.name().toLowerCase(Locale.ENGLISH);
        final String themeSpecificOverride = "bootstrap-overrides-" + name + ".css";
        final ResourceReference.Key themeSpecificOverrideKey =
                new ResourceReference.Key(BootstrapOverridesCssResourceReference.class.getName(),
                        themeSpecificOverride, null, null, null);
        if (PackageResource.exists(themeSpecificOverrideKey)) {
            response.render(CssHeaderItem.forReference(new CssResourceReference(themeSpecificOverrideKey)));
        }
    }

}
