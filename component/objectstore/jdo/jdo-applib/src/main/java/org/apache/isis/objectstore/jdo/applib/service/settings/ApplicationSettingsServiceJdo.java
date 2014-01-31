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

package org.apache.isis.objectstore.jdo.applib.service.settings;

import java.util.List;

import org.joda.time.LocalDate;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.settings.ApplicationSetting;
import org.apache.isis.applib.services.settings.ApplicationSettingsService;
import org.apache.isis.applib.services.settings.ApplicationSettingsServiceRW;
import org.apache.isis.applib.services.settings.SettingAbstract;
import org.apache.isis.applib.services.settings.SettingType;

/**
 * An implementation of {@link ApplicationSettingsService} that persists settings
 * as entities into a JDO-backed database.
 */
@Named("Application Settings")
public class ApplicationSettingsServiceJdo extends AbstractService implements ApplicationSettingsServiceRW {

    @ActionSemantics(Of.SAFE)
    @Override
    public ApplicationSetting find(@Named("Key") String key) {
        return firstMatch(
                new QueryDefault<ApplicationSettingJdo>(ApplicationSettingJdo.class, 
                        "findByKey", 
                        "key", key));
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="1")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<ApplicationSetting> listAll() {
        return (List)allMatches(
                new QueryDefault<ApplicationSettingJdo>(ApplicationSettingJdo.class, 
                        "findAll"));
    }

    // //////////////////////////////////////

    @MemberOrder(sequence="10")
    @Override
    public ApplicationSetting newString(
            @Named("Key") String key, 
            @Named("Description") @Optional String description, 
            @Named("Value") String value) {
        return newSetting(key, description, SettingType.STRING, value);
    }
    @MemberOrder(sequence="11")
    @Override
    public ApplicationSettingJdo newInt(
            @Named("Key") String key, 
            @Named("Description") @Optional String description, 
            @Named("Value") Integer value) {
        return newSetting(key, description, SettingType.INT, value.toString());
    }
    @MemberOrder(sequence="12")
    @Override
    public ApplicationSettingJdo newLong(
            @Named("Key") String key, 
            @Named("Description") @Optional String description, 
            @Named("Value") Long value) {
        return newSetting(key, description, SettingType.LONG, value.toString());
    }
    @MemberOrder(sequence="13")
    @Override
    public ApplicationSettingJdo newLocalDate(
            @Named("Key") String key, 
            @Named("Description") @Optional String description, 
            @Named("Value") LocalDate value) {
        return newSetting(key, description, SettingType.LOCAL_DATE, value.toString(SettingAbstract.DATE_FORMATTER));
    }
    @MemberOrder(sequence="14")
    @Override
    public ApplicationSettingJdo newBoolean(
            @Named("Key") String key, 
            @Named("Description") @Optional String description, 
            @Named("Value") @Optional Boolean value) {
        return newSetting(key, description, SettingType.BOOLEAN, new Boolean(value != null && value).toString());
    }

    private ApplicationSettingJdo newSetting(
            String key, String description, SettingType settingType, final String valueRaw) {
        final ApplicationSettingJdo setting = newTransientInstance(ApplicationSettingJdo.class);
        setting.setKey(key);
        setting.setDescription(description);
        setting.setValueRaw(valueRaw);
        setting.setType(settingType);
        persist(setting);
        return setting;
    }

}
