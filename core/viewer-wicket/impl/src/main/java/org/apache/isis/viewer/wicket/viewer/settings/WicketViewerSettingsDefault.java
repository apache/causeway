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

package org.apache.isis.viewer.wicket.viewer.settings;

import javax.inject.Singleton;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.facets.object.promptStyle.PromptStyleConfiguration;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;

@Singleton
public class WicketViewerSettingsDefault implements WicketViewerSettings {

    private static final long serialVersionUID = 1L;

    IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

    @Override
    public int getMaxTitleLengthInStandaloneTables() {
        return getConfiguration().getInteger("isis.viewer.wicket.maxTitleLengthInStandaloneTables", getMaxTitleLengthInTables());
    }

    @Override
    public int getMaxTitleLengthInParentedTables() {
        return getConfiguration().getInteger("isis.viewer.wicket.maxTitleLengthInParentedTables", getMaxTitleLengthInTables());
    }

    /**
     * Fallback for either {@link #getMaxTitleLengthInParentedTables()} and {@link #getMaxTitleLengthInParentedTables()}
     */
    private int getMaxTitleLengthInTables() {
        return getConfiguration().getInteger("isis.viewer.wicket.maxTitleLengthInTables", 12);
    }

    @Override
    public String getDatePattern() {
        return getConfiguration().getString("isis.viewer.wicket.datePattern", "dd-MM-yyyy");
    }

    @Override
    public String getDateTimePattern() {
        return getConfiguration().getString("isis.viewer.wicket.dateTimePattern", "dd-MM-yyyy HH:mm");
    }

    @Override
    public String getTimestampPattern() {
        return getConfiguration().getString("isis.viewer.wicket.timestampPattern", "yyyy-MM-dd HH:mm:ss.SSS");
    }

    @Override
    public boolean isReplaceDisabledTagWithReadonlyTag() {
        return getConfiguration().getBoolean("isis.viewer.wicket.replaceDisabledTagWithReadonlyTag", true);
    }

    @Override
    public boolean isPreventDoubleClickForFormSubmit() {
        return getConfiguration().getBoolean("isis.viewer.wicket.preventDoubleClickForFormSubmit", true);
    }

    @Override
    public boolean isPreventDoubleClickForNoArgAction() {
        return getConfiguration().getBoolean("isis.viewer.wicket.preventDoubleClickForNoArgAction", true);
    }

    @Override
    public boolean isUseIndicatorForFormSubmit() {
        return getConfiguration().getBoolean("isis.viewer.wicket.useIndicatorForFormSubmit", true);
    }

    @Override
    public boolean isUseIndicatorForNoArgAction() {
        return getConfiguration().getBoolean("isis.viewer.wicket.useIndicatorForNoArgAction", true);
    }

    @Override
    public PromptStyle getPromptStyle() {
        return PromptStyleConfiguration.parse(getConfiguration());
    }

    @Override
    public boolean isRedirectEvenIfSameObject() {
        return getConfiguration().getBoolean("isis.viewer.wicket.redirectEvenIfSameObject", false);
    }
}
