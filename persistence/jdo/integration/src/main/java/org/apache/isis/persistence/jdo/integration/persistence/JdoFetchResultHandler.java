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
package org.apache.isis.persistence.jdo.integration.persistence;

import javax.annotation.Nullable;
import javax.jdo.PersistenceManager;

import org.datanucleus.enhancement.Persistable;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.persistence.jdo.integration.lifecycles.FetchResultHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JdoFetchResultHandler implements FetchResultHandler {

    private final MetaModelContext metaModelContext;
    private final PersistenceManager persistenceManager;
    private final EntityChangeTracker entityChangeTracker;
    
    @Override
    public ManagedObject initializeEntityAfterFetched(final Persistable pojo) {

        final ManagedObject entity = _Utils
                .identify(metaModelContext, persistenceManager, pojo);

        entityChangeTracker.recognizeLoaded(entity);

        return entity;
    }
    
    @Override
    public ManagedObject initializeValueAfterFetched(final @Nullable Object pojo) {
        return _Utils.adaptNullableAndInjectServices(metaModelContext, pojo);
    }
}
