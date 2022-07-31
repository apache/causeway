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

import java.lang.reflect.Constructor;

import javax.annotation.Priority;

import org.datanucleus.identity.DatastoreId;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.context._Context;

import lombok.NonNull;
import lombok.SneakyThrows;

@Component
@Priority(PriorityPrecedence.LATE + 100) // after the implementations of DatastoreId; for a custom impl.
public class IdStringifierForDatastoreId extends IdStringifier.Abstract<DatastoreId> {

    public IdStringifierForDatastoreId() {
        super(DatastoreId.class);
    }

    @Override
    public String enstring(final @NonNull DatastoreId value) {
        //
        // the JDO spec (5.4.3) requires that OIDs are serializable toString and
        // re-create-able through the constructor
        //
        // to do this, we also need to capture the class of the Id value class itself, followed by the value (as a string)
        return value.getClass().getName() + SEPARATOR + value.toString();
    }

    @SneakyThrows
    @Override
    public DatastoreId destring(
            final @NonNull String stringified,
            final @Nullable Class<?> targetEntityClassIfAny) {
        int idx = stringified.indexOf(SEPARATOR);
        String clsName = stringified.substring(0, idx);
        String keyStr = stringified.substring(idx + 1);
        final Class<?> cls = _Context.loadClass(clsName);
        final Constructor<?> cons = cls.getConstructor(String.class);
        final Object dnOid = cons.newInstance(keyStr);
        return _Casts.uncheckedCast(dnOid);
    }


}
