/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.settings;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;


/**
 * Convenience class to implement {@link Setting}.
 */
@Immutable
public abstract class SettingAbstract implements Setting {

    public final static DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Title(sequence="10")
    @MemberOrder(sequence="10")
    public abstract String getKey();

    @Optional
    @MemberOrder(sequence="80")
    public abstract String getDescription();

    @MemberOrder(sequence="90")
    public abstract SettingType getType();

    // //////////////////////////////////////

    @Title(prepend=" = ", sequence="30")
    @MemberOrder(sequence="30")
    @Hidden(where=Where.OBJECT_FORMS)
    public abstract String getValueRaw();

    // //////////////////////////////////////

    @Programmatic
    public String valueAsString() {
        ensure(SettingType.STRING);
        return getValueRaw();
    }

    // //////////////////////////////////////

    @Programmatic
    public LocalDate valueAsLocalDate() {
        ensure(SettingType.LOCAL_DATE);
        return parseValueAsLocalDate();
    }

    protected LocalDate parseValueAsLocalDate() {
        return LocalDate.parse(getValueRaw(), DATE_FORMATTER);
    }

    // //////////////////////////////////////

    @Programmatic
    public Integer valueAsInt() {
        ensure(SettingType.INT);
        return parseValueAsInt();
    }

    protected int parseValueAsInt() {
        return Integer.parseInt(getValueRaw());
    }

    // //////////////////////////////////////
    
    @Programmatic
    public Long valueAsLong() {
        ensure(SettingType.LONG);
        return parseValueAsLong();
    }
    
    protected long parseValueAsLong() {
        return Long.parseLong(getValueRaw());
    }
    
    // //////////////////////////////////////
    
    @Programmatic
    public Boolean valueAsBoolean() {
        ensure(SettingType.BOOLEAN);
        return parseValueAsBoolean();
    }

    protected boolean parseValueAsBoolean() {
        return Boolean.parseBoolean(getValueRaw());
    }

    // //////////////////////////////////////
    
    @MemberOrder(sequence="30")
    @Hidden(where=Where.ALL_TABLES)
    @Named("Value")
    public String getValueAsString() {
        return getValueRaw();
    }
    public boolean hideValueAsString() {
        return typeIsNot(SettingType.STRING);
    }

    // //////////////////////////////////////
    
    @MemberOrder(sequence="30")
    @Hidden(where=Where.ALL_TABLES)
    @Named("Value")
    public LocalDate getValueAsLocalDate() {
        return parseValueAsLocalDate();
    }
    public boolean hideValueAsLocalDate() {
        return typeIsNot(SettingType.LOCAL_DATE);
    }
    
    // //////////////////////////////////////
    
    
    @MemberOrder(sequence="30")
    @Hidden(where=Where.ALL_TABLES)
    @Named("Value")
    public Integer getValueAsInt() {
        return parseValueAsInt();
    }
    public boolean hideValueAsInt() {
        return typeIsNot(SettingType.INT);
    }
    
    // //////////////////////////////////////
    
    
    
    @MemberOrder(sequence="30")
    @Hidden(where=Where.ALL_TABLES)
    @Named("Value")
    public Long getValueAsLong() {
        return parseValueAsLong();
    }
    public boolean hideValueAsLong() {
        return typeIsNot(SettingType.LONG);
    }

    // //////////////////////////////////////
    
    
    
    @MemberOrder(sequence="30")
    @Hidden(where=Where.ALL_TABLES)
    @Named("Value")
    public Boolean getValueAsBoolean() {
        return parseValueAsBoolean();
    }
    public boolean hideValueAsBoolean() {
        return typeIsNot(SettingType.BOOLEAN);
    }

    // //////////////////////////////////////

    private void ensure(SettingType settingType) {
        if(typeIsNot(settingType)) {
            throw new IllegalStateException("Setting '" + getKey() + "' is of type " + getType() + ", not of type " + settingType);
        }
    }
    
    protected boolean typeIsNot(SettingType settingType) {
        return getType() != settingType;
    }
    
}
