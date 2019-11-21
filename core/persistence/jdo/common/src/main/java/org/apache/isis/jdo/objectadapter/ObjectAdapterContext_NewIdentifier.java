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
package org.apache.isis.jdo.objectadapter;

import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.persistence.PersistenceSession;

import lombok.RequiredArgsConstructor;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: creates RootOids 
 * </p> 
 * @since 2.0
 */
@RequiredArgsConstructor
class ObjectAdapterContext_NewIdentifier {

    private final PersistenceSession persistenceSession;
    private final SpecificationLoader specificationLoader;

    /**
     * Return an equivalent {@link RootOid}, but being persistent.
     *
     * <p>
     * It is the responsibility of the implementation to determine the new unique identifier.
     * For example, the generator may simply assign a new value from a sequence, or a GUID;
     * or, the generator may use the oid to look up the object and inspect the object in order
     * to obtain an application-defined value.
     *
     * @param pojo - being persisted
     */
    final RootOid createPersistentOid(Object pojo) {

        final ObjectSpecification spec = specificationLoader.loadSpecification(pojo.getClass());

        final String identifier = persistenceSession.identifierFor(pojo);

        final ObjectSpecId objectSpecId = spec.getSpecId();
        return Oid.Factory.persistentOf(objectSpecId, identifier);
    }

}