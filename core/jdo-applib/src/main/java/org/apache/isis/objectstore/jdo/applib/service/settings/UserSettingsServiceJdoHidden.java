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

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.services.settings.UserSetting;

/**
 * An implementation intended to be hidden in the UI, and delegated to by other services.
 */
public class UserSettingsServiceJdoHidden extends UserSettingsServiceJdo {

    @Hidden
    @Override
    public UserSetting find(String user, String key) {
        return super.find(user, key);
    }

    // //////////////////////////////////////

    @Hidden
    @Override
    public List<UserSetting> listAll() {
        return super.listAll();
    }

    @Hidden
    @Override
    public List<UserSetting> listAllFor(String user) {
        return super.listAllFor(user);
    }

    // //////////////////////////////////////
    
    @Hidden
    @Override
    public UserSettingJdo newString(String user, String key, String description, String value) {
        return super.newString(user, key, description, value);
    }

    @Hidden
    @Override
    public UserSettingJdo newInt(String user, String key, String description, Integer value) {
        return super.newInt(user, key, description, value);
    }

    @Hidden
    @Override
    public UserSettingJdo newLong(String user, String key, String description, Long value) {
        return super.newLong(user, key, description, value);
    }

    @Hidden
    @Override
    public UserSettingJdo newLocalDate(String user, String key, String description, LocalDate value) {
        return super.newLocalDate(user, key, description, value);
    }

    @Hidden
    @Override
    public UserSettingJdo newBoolean(String user, String key, String description, Boolean value) {
        return super.newBoolean(user, key, description, value);
    }

    

}
