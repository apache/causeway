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
package org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity;

import java.util.UUID;

import javax.annotation.Priority;
import javax.jdo.identity.ObjectIdentity;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.IdStringifier;

import lombok.Builder;
import lombok.NonNull;

/**
 * Implementation for application-defined primary keys.
 *
 * <p>
 *     For the most part this relies on the JDO spec (5.4.3), but with special case handling if the primary key is
 *     of type int, long or UUID: rather than encode the fully qualified classname, instead uses a simpler prefix.
 * </p>
 */
@Component
@Priority(PriorityPrecedence.LATE)
@Builder
public class IdStringifierForObjectIdentity extends IdStringifier.Abstract<ObjectIdentity> {

    private static final String PREFIX_UUID = "u_";
    private static final String PREFIX_LONG = "l_";
    private static final String PREFIX_INT = "i_";

    public IdStringifierForObjectIdentity() {
        super(ObjectIdentity.class);
    }

    @Override
    public String enstring(final @NonNull ObjectIdentity value) {
        Object keyAsObject = value.getKeyAsObject();
        if (keyAsObject instanceof Long) {
            return PREFIX_LONG + keyAsObject;
        }
        if (keyAsObject instanceof Integer) {
            return PREFIX_INT + keyAsObject;
        }
        if (keyAsObject instanceof UUID) {
            return PREFIX_UUID + keyAsObject;
        }
        // fall through to JDO spec (5.4.3)
        return keyAsObject.toString();
    }

    @Override
    public ObjectIdentity destring(
            final @NonNull String stringified,
            final @NonNull Class<?> targetEntityClass) {
        if (stringified.startsWith(PREFIX_LONG)) {
            return new ObjectIdentity(targetEntityClass, Long.parseLong(stringified.substring(PREFIX_LONG.length())));
        }
        if (stringified.startsWith(PREFIX_INT)) {
            return new ObjectIdentity(targetEntityClass, Integer.parseInt(stringified.substring(PREFIX_INT.length())));
        }
        if (stringified.startsWith(PREFIX_UUID)) {
            return new ObjectIdentity(targetEntityClass, UUID.fromString(stringified.substring(PREFIX_UUID.length())));
        }
        // fall through to JDO spec (5.4.3)
        return new ObjectIdentity(targetEntityClass, stringified);
    }
}
