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
 *
 */
package org.apache.causeway.persistence.jdo.datanucleus.mixins;

import javax.jdo.JDOHelper;

import org.datanucleus.enhancement.Persistable;

import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.LayoutConstants;

import lombok.RequiredArgsConstructor;

/**
 * Contributes the value of the version (introduced by enhancing, and used by
 * the ORM for optimistic locking) as a property.
 *
 * <p>
 * Only visible if the version can be cast to a {@link java.sql.Timestamp}.
 * </p>
 *
 * @see Persistable_datanucleusVersionLong
 *
 * @since 2.0 {@index}
 */
@Property(
        domainEvent = Persistable_datanucleusVersionTimestamp.PropertyDomainEvent.class)
@PropertyLayout(
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        hidden = Where.ALL_TABLES,
        named = "Version",
        sequence = "500.2"
        )
@RequiredArgsConstructor
public class Persistable_datanucleusVersionTimestamp {

    private final Persistable persistable;

    public static class PropertyDomainEvent
    extends org.apache.causeway.applib.CausewayModuleApplib.PropertyDomainEvent
    <Persistable_datanucleusVersionTimestamp, java.sql.Timestamp> {}

    @MemberSupport public java.sql.Timestamp prop() {
        final Object version = JDOHelper.getVersion(persistable);
        return version instanceof java.sql.Timestamp ? (java.sql.Timestamp) version : null;
    }

    @MemberSupport public boolean hideProp() {
        return prop() == null;
    }

}
