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
import javax.jdo.annotations.PrimaryKey;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.services.settings.SettingType;
import org.apache.isis.applib.services.settings.UserSetting;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.APPLICATION, 
        objectIdClass=UserSettingPrimaryKey.class,
        table="UserSetting")
@javax.jdo.annotations.Queries({ 
    @javax.jdo.annotations.Query(
            name = "findByUserAndKey", language = "JDOQL", 
            value = "SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.settings.UserSettingJdo "
                    + "WHERE user == :user "
                    + "&& key == :key ") 
    ,@javax.jdo.annotations.Query(
            name = "findByUser", language = "JDOQL", 
            value = "SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.settings.UserSettingJdo "
                    + "WHERE user == :user "
                    + "ORDER BY key") 
    ,@javax.jdo.annotations.Query(
            name = "findAll", language = "JDOQL", 
            value = "SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.settings.UserSettingJdo "
                    + "ORDER BY user, key") 
})
// can't see how to specify this order in the primary key; however HSQLDB objects :-(
//@javax.jdo.annotations.Unique(name="USER_KEY_IDX", members={"user","key"}) 
@Named("User Setting")
public class UserSettingJdo extends SettingAbstractJdo implements UserSetting {

    
    private String user;

    @Title(sequence="5", append=": ")
    @PrimaryKey
    @MemberOrder(sequence = "5")
    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    // //////////////////////////////////////

    @Title(sequence="10")
    @PrimaryKey
    @Override
    public String getKey() {
        return super.getKey();
    }
    @Override
    public void setKey(String key) {
        super.setKey(key);
    }

    // //////////////////////////////////////

    @Persistent
    @Override
    public String getDescription() {
        return super.getDescription();
    }
    @Override
    public void setDescription(String description) {
        super.setDescription(description);
    }
    
    // //////////////////////////////////////

    @javax.jdo.annotations.Column(allowsNull="false")
    @Title(prepend=" = ", sequence="30")
    @Persistent
    @Override
    public String getValueRaw() {
        return super.getValueRaw();
    }
    @Persistent
    @Override
    public void setValueRaw(String valueAsRaw) {
        super.setValueRaw(valueAsRaw);
    }
    
    // //////////////////////////////////////

    @javax.jdo.annotations.Column(allowsNull="false")
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
