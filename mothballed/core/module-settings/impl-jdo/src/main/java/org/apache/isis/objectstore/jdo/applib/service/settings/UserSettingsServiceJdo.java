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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.joda.time.LocalDate;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.settings.SettingAbstract;
import org.apache.isis.applib.services.settings.SettingType;
import org.apache.isis.applib.services.settings.UserSetting;
import org.apache.isis.applib.services.settings.UserSettingsService;
import org.apache.isis.applib.services.settings.UserSettingsServiceRW;

/**
 * An implementation of {@link UserSettingsService} that persists settings
 * as entities into a JDO-backed database.
 */
@Named("User Settings")
public class UserSettingsServiceJdo extends AbstractService implements UserSettingsServiceRW {

    @Override
    public UserSetting find(
            @Named("User") String user, 
            @Named("Key") String key) {
        return firstMatch(
                new QueryDefault<UserSettingJdo>(UserSettingJdo.class, 
                        "findByUserAndKey", 
                        "user",user,
                        "key", key));
    }


    // //////////////////////////////////////

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<UserSetting> listAllFor(String user) {
        return (List)allMatches(
                new QueryDefault<UserSettingJdo>(UserSettingJdo.class, 
                        "findByUser", 
                        "user", user));
    }
    public List<String> choices0ListAllFor() {
        return existingUsers();
    }

    private List<String> existingUsers() {
        final List<UserSetting> listAll = listAll();
        return Lists.newArrayList(Sets.newTreeSet(Iterables.transform(listAll, GET_USER)));
    }

    private final static Function<UserSetting, String> GET_USER = new Function<UserSetting, String>() {
        public String apply(UserSetting input) {
            return input.getUser();
        }
    };

    // //////////////////////////////////////

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<UserSetting> listAll() {
        return (List)allMatches(
                new QueryDefault<UserSettingJdo>(UserSettingJdo.class, 
                        "findAll"));
    }


    // //////////////////////////////////////
    
    @MemberOrder(sequence="10")
    public UserSettingJdo newString(
            @Named("User") String user, 
            @Named("Key") String key, 
            @Named("Description") @Optional String description, 
            @Named("Value") String value) {
        return newSetting(user, key, description, SettingType.STRING, value);
    }
    public String default0NewString() {
        return getContainer().getUser().getName();
    }

    @MemberOrder(sequence="11")
    public UserSettingJdo newInt(
            @Named("User") String user, 
            @Named("Key") String key, 
            @Named("Description") @Optional String description, 
            @Named("Value") Integer value) {
        return newSetting(user, key, description, SettingType.INT, value.toString());
    }
    public String default0NewInt() {
        return getContainer().getUser().getName();
    }

    @MemberOrder(sequence="12")
    public UserSettingJdo newLong(
            @Named("User") String user, 
            @Named("Key") String key, 
            @Named("Description") @Optional String description, 
            @Named("Value") Long value) {
        return newSetting(user, key, description, SettingType.LONG, value.toString());
    }
    public String default0NewLong() {
        return getContainer().getUser().getName();
    }

    @MemberOrder(sequence="13")
    public UserSettingJdo newLocalDate(
            @Named("User") String user, 
            @Named("Key") String key, 
            @Named("Description") @Optional String description, 
            @Named("Value") LocalDate value) {
        return newSetting(user, key, description, SettingType.LOCAL_DATE, value.toString(SettingAbstract.DATE_FORMATTER));
    }
    public String default0NewLocalDate() {
        return getContainer().getUser().getName();
    }

    @MemberOrder(sequence="14")
    public UserSettingJdo newBoolean(
            @Named("User") String user, 
            @Named("Key") String key, 
            @Named("Description") @Optional String description, 
            @Named("Value") @Optional Boolean value) {
        return newSetting(user, key, description, SettingType.BOOLEAN, new Boolean(value != null && value).toString());
    }
    public String default0NewBoolean() {
        return getContainer().getUser().getName();
    }

    private UserSettingJdo newSetting(
            String user, String key, String description, SettingType settingType, final String valueRaw) {
        final UserSettingJdo setting = newTransientInstance(UserSettingJdo.class);
        setting.setUser(user);
        setting.setKey(key);
        setting.setType(settingType);
        setting.setDescription(description);
        setting.setValueRaw(valueRaw);
        persist(setting);
        return setting;
    }
    

}
