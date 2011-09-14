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

package org.apache.isis.runtimes.dflt.objectstores.sql;

import java.util.Calendar;

import org.joda.time.DateTimeZone;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

/**
 * Provides objectstore defaults. Most significantly, maintains the object store default TimeZone, and maintains
 * Calendar.
 * 
 * 
 * @version $Rev$ $Date$
 */
public class Defaults {
    public static void initialise(String propertiesBase) {
        setTimeZone(DateTimeZone.UTC);

        final IsisConfiguration configParameters = IsisContext.getConfiguration();

        setPkIdLabel(getProperty(propertiesBase, configParameters, "pk_id"));
        setIdColumn(getProperty(propertiesBase, configParameters, "id"));
    }

    protected static String getProperty(String propertiesBase, final IsisConfiguration configParameters, String property) {
        return configParameters.getString(propertiesBase + ".default." + property, property);
    }

    // {{ Calendar
    private static Calendar calendar;

    public static Calendar getCalendar() {
        return calendar;
    }

    // }}

    // {{ DateTimeZone
    private static DateTimeZone dateTimeZone;

    public static DateTimeZone getTimeZone() {
        return dateTimeZone;
    }

    public static void setTimeZone(final DateTimeZone timezone) {
        dateTimeZone = timezone;
        calendar = Calendar.getInstance(timezone.toTimeZone());
    }

    // }}

    // {{ Primary Key label, defaults to "pk_id"
    private static String pkIdLabel;

    public static void setPkIdLabel(String pkIdLabel) {
        Defaults.pkIdLabel = pkIdLabel;
    }

    public static String getPkIdLabel() {
        return pkIdLabel;
    }

    // }}

    // {{ Id Column, defaults to "id"
    private static String idColumn;

    public static void setIdColumn(String idColumn) {
        Defaults.idColumn = idColumn;
    }

    public static String getIdColumn() {
        return idColumn;
    }
    // }}

}
