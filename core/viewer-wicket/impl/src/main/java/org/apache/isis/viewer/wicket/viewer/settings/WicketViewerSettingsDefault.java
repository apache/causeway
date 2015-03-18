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

import com.google.inject.Singleton;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.ui.components.scalars.datepicker.TextFieldWithDatePicker;

@Singleton
public class WicketViewerSettingsDefault implements WicketViewerSettings {

    private static final long serialVersionUID = 1L;

    IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

    /**
     * The maximum length that a title of an object will be shown when rendered in a standalone table;
     * will be truncated beyond this (with ellipses to indicate the truncation). 
     */
    @Override
    public int getMaxTitleLengthInStandaloneTables() {
        return getConfiguration().getInteger("isis.viewer.wicket.maxTitleLengthInStandaloneTables", getMaxTitleLengthInTables());
    }

    /**
     * The maximum length that a title of an object will be shown when rendered in a parented table;
     * will be truncated beyond this (with ellipses to indicate the truncation). 
     */
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

    /**
     * The pattern used for rendering and parsing dates.
     */
    @Override
    public String getDatePattern() {
        return getConfiguration().getString("isis.viewer.wicket.datePattern", "dd-MM-yyyy");
    }

    /**
     * The pattern used for rendering and parsing date/times.
     */
    @Override
    public String getDateTimePattern() {
        return getConfiguration().getString("isis.viewer.wicket.dateTimePattern", "dd-MM-yyyy HH:mm");
    }

    /**
     * The pattern used for rendering and parsing timestamps.
     */
    @Override
    public String getTimestampPattern() {
        return getConfiguration().getString("isis.viewer.wicket.timestampPattern", "yyyy-MM-dd HH:mm:ss.SSS");
    }
    
    /**
     * The pattern used for rendering dates chosen by the {@link TextFieldWithDatePicker}.
     * 
     * <p>
     * This pattern is different from {@link #getDatePattern()} because it is interpreted by
     * <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime Picker</a> component
     * that uses <a href="http://momentjs.com/docs/#/parsing/string-format/">Moment.js formats</a>, rather
     * than by Java code. 
     */
    @Override
    public String getDatePickerPattern() {
        return getConfiguration().getString("isis.viewer.wicket.datePickerPattern", "DD-MM-YYYY");
    }
}
