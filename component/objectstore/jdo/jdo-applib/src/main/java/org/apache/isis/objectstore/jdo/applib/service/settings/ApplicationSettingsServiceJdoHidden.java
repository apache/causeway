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
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.services.settings.ApplicationSetting;

/**
 * An implementation intended to be hidden in the UI, and delegated to by other services.
 */
public class ApplicationSettingsServiceJdoHidden extends ApplicationSettingsServiceJdo {

    @Hidden
    @Override
    public ApplicationSetting find(@Named("Key") String key) {
        return super.find(key);
    }

    // //////////////////////////////////////

    @Hidden
    @Override
    public List<ApplicationSetting> listAll() {
        return super.listAll();
    }

    // //////////////////////////////////////

    @Hidden
    @Override
    public ApplicationSetting newString(String key, String description, String value) {
        return super.newString(key, description, value);
    }
    
    @Hidden
    @Override
    public ApplicationSettingJdo newInt(String key, String description, Integer value) {
        return super.newInt(key, description, value);
    }
    
    @Hidden
    @Override
    public ApplicationSettingJdo newLong(String key, String description, Long value) {
        return super.newLong(key, description, value);
    }
    
    @Hidden
    @Override
    public ApplicationSettingJdo newLocalDate(String key, String description, LocalDate value) {
        return super.newLocalDate(key, description, value);
    }
    
    @Hidden
    @Override
    public ApplicationSettingJdo newBoolean(String key, String description, Boolean value) {
        return super.newBoolean(key, description, value);
    }

}
