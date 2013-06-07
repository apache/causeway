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

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Persistent;

import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.services.settings.ApplicationSetting;
import org.apache.isis.applib.services.settings.SettingType;

@javax.jdo.annotations.PersistenceCapable(identityType = IdentityType.APPLICATION, table="APPLICATION_SETTING")
@javax.jdo.annotations.Queries({ 
     @javax.jdo.annotations.Query(name = "applicationsetting_by_id", language = "JDOQL", value = "SELECT FROM org.apache.isis.objectstore.jdo.applib.service.settings.ApplicationSettingJdo WHERE key == :key") 
    ,@javax.jdo.annotations.Query(name = "applicationsetting_all", language = "JDOQL", value = "SELECT FROM org.apache.isis.objectstore.jdo.applib.service.settings.ApplicationSettingJdo ORDER BY key") 
})
@Named("Application Setting")
public class ApplicationSettingJdo extends SettingAbstractJdo implements ApplicationSetting {

    @javax.jdo.annotations.PrimaryKey
    public String getKey() {
        return super.getKey();
    }
    @Override
    public void setKey(String key) {
        super.setKey(key);
    }

    @Persistent
    @Override
    public String getDescription() {
        return super.getDescription();
    }
    @Override
    public void setDescription(String description) {
        super.setDescription(description);
    }
    @Persistent
    @Override
    public String getValueRaw() {
        return super.getValueRaw();
    }
    @Override
    public void setValueRaw(String valueAsRaw) {
        super.setValueRaw(valueAsRaw);
    }
    @Persistent
    @Override
    public SettingType getType() {
        return super.getType();
    }
    @Override
    public void setType(SettingType type) {
        super.setType(type);
    }
}
