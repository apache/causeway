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

import javax.annotation.Priority;

import org.datanucleus.identity.DatastoreIdImpl;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.IdStringifier;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

@Component
@Priority(PriorityPrecedence.LATE)
public class IdStringifierForDatastoreIdImpl extends IdStringifier.Abstract<DatastoreIdImpl> {

    public static final String STRING_DELIMITER = "[OID]"; // as

    public IdStringifierForDatastoreIdImpl() {
        super(DatastoreIdImpl.class);
    }

    @Override
    public String enstring(final @NonNull DatastoreIdImpl value) {
        return value.getKeyAsObject().toString();
    }

    @SneakyThrows
    @Override
    public DatastoreIdImpl destring(
            final @NonNull String stringified,
            final @NonNull Class<?> targetEntityClass) {
        // enString invoked toString() on the original key; invoking toString() on its stringified form does not change it
        val proto = new DatastoreIdImpl(targetEntityClass.getName(), stringified);
        // now render in the form that the DataStoreImpl constructor expects; it will take it apart itself.
        val str = proto.toString();
        return new DatastoreIdImpl(str);
    }

}
