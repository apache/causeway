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

import javax.jdo.annotations.PersistenceCapable;

import org.joda.time.LocalDate;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.services.settings.SettingType;

/**
 * Factors out common implementation; however this is annotated with {@link PersistenceCapable},
 * so that each subclass is its own root entity.
 */
public abstract class SettingAbstractJdo extends org.apache.isis.applib.services.settings.SettingAbstract implements org.apache.isis.applib.services.settings.ApplicationSetting {

    private String key;

    @javax.jdo.annotations.Column(allowsNull="false")
    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    // //////////////////////////////////////

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @MemberOrder(name="Description", sequence="1")
    @Named("Update")
    public SettingAbstractJdo updateDescription(@Named("Description") @Optional String description) {
        setDescription(description);
        return this;
    }
    public String default0UpdateDescription() {
        return getDescription();
    }
    
    // //////////////////////////////////////

    private SettingType type;

    @javax.jdo.annotations.Column(allowsNull="false")
    public SettingType getType() {
        return type;
    }

    public void setType(final SettingType type) {
        this.type = type;
    }

    // //////////////////////////////////////

    private String valueRaw;

    @javax.jdo.annotations.Column(allowsNull="false")
    public String getValueRaw() {
        return valueRaw;
    }

    public void setValueRaw(final String valueAsRaw) {
        this.valueRaw = valueAsRaw;
    }

    // //////////////////////////////////////
    
    @MemberOrder(name="ValueAsString", sequence="1")
    @Named("Update")
    public SettingAbstractJdo updateAsString(@Named("Value") String value) {
        setValueRaw(value);
        return this;
    }
    public String default0UpdateAsString() {
        return getValueAsString();
    }
    public boolean hideUpdateAsString() {
        return typeIsNot(SettingType.STRING);
    }
    
    @MemberOrder(name="ValueAsInt", sequence="1")
    @Named("Update")
    public SettingAbstractJdo updateAsInt(@Named("Value") Integer value) {
        setValueRaw(value.toString());
        return this;
    }
    public Integer default0UpdateAsInt() {
        return getValueAsInt();
    }
    public boolean hideUpdateAsInt() {
        return typeIsNot(SettingType.INT);
    }
    
    @MemberOrder(name="ValueAsLong", sequence="1")
    @Named("Update")
    public SettingAbstractJdo updateAsLong(@Named("Value") Long value) {
        setValueRaw(value.toString());
        return this;
    }
    public Long default0UpdateAsLong() {
        return getValueAsLong();
    }
    public boolean hideUpdateAsLong() {
        return typeIsNot(SettingType.LONG);
    }
    
    @MemberOrder(name="ValueAsLocalDate", sequence="1")
    @Named("Update")
    public SettingAbstractJdo updateAsLocalDate(@Named("Value") LocalDate value) {
        setValueRaw(value.toString(DATE_FORMATTER));
        return this;
    }
    public LocalDate default0UpdateAsLocalDate() {
        return getValueAsLocalDate();
    }
    public boolean hideUpdateAsLocalDate() {
        return typeIsNot(SettingType.LOCAL_DATE);
    }

    @MemberOrder(name="ValueAsBoolean", sequence="1")
    @Named("Update")
    public SettingAbstractJdo updateAsBoolean(@Named("Value") Boolean value) {
        setValueRaw(value.toString());
        return this;
    }
    public Boolean default0UpdateAsBoolean() {
        return getValueAsBoolean();
    }
    public boolean hideUpdateAsBoolean() {
        return typeIsNot(SettingType.BOOLEAN);
    }
    
    // //////////////////////////////////////
    
    
    public SettingAbstractJdo delete(
            @Named("Are you sure?") @Optional Boolean confirm) {
        if(confirm == null || !confirm) {
            container.informUser("Setting NOT deleted");
            return this;
        }
        container.remove(this);
        container.informUser("Setting deleted");
        return null;
    }
    

    
 
    // //////////////////////////////////////
    
    private DomainObjectContainer container;

    public void setDomainObjectContainer(final DomainObjectContainer container) {
        this.container = container;
    }


}
