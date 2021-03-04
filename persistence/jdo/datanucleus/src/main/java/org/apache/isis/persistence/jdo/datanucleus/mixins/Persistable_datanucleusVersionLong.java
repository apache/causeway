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
package org.apache.isis.persistence.jdo.datanucleus.mixins;

import javax.jdo.JDOHelper;

import org.datanucleus.enhancement.Persistable;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.mixins.layout.LayoutMixinConstants;

import lombok.RequiredArgsConstructor;

/**
 * Contributes the value of the version (introduced by enhancing, and used by
 * the ORM for optimistic locking) as a property.
 *
 * <p>
 * Only visible if the version can be cast to a long.
 * </p>
 *
 * @see Persistable_datanucleusIdLong
 * @see Persistable_datanucleusVersionTimestamp
 *
 * @since 2.0 {@index}
 */
@Property(
        domainEvent = Persistable_datanucleusVersionLong.PropertyDomainEvent.class)
@PropertyLayout(
        named = "Version",
        hidden = Where.ALL_TABLES
        )
@RequiredArgsConstructor
public class Persistable_datanucleusVersionLong {

    private final Persistable persistable;

    public static class PropertyDomainEvent
    extends org.apache.isis.applib.IsisModuleApplib.PropertyDomainEvent
    <Persistable_datanucleusVersionLong, Long> {}

    @MemberOrder(name = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME, sequence = "800.2")
    public Long prop() {
        final Object version = JDOHelper.getVersion(persistable);
        return version != null && version instanceof Long ? (Long) version : null;
    }

    public boolean hideProp() {
        return prop() == null;
    }

}
