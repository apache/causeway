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
package org.apache.isis.viewer.bdd.common.components;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.object.cached.CachedFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.objectstore.InMemoryPersistenceSessionFactory;
import org.apache.isis.core.objectstore.internal.ObjectStorePersistedObjects;
import org.apache.isis.core.runtime.persistence.PersistenceSessionFactoryDelegate;
import org.apache.isis.core.runtime.system.DeploymentType;

/**
 * As per {@link InMemoryPersistenceSessionFactory}, but uses the
 * {@link BddObjectStorePersistedObjects} implementation which stores any
 * {@link CachedFacet cached} {@link ObjectSpecification class}es
 * <tt>static</tt>ally (and thus persisted across multiple setups/teardowns of
 * the {@link NakedObjectsContext}.
 */
public class BddInMemoryPersistenceSessionFactory extends InMemoryPersistenceSessionFactory {

    public BddInMemoryPersistenceSessionFactory(final DeploymentType deploymentType, final IsisConfiguration configuration, final PersistenceSessionFactoryDelegate persistenceSessionFactoryDelegate) {
        super(deploymentType, configuration, persistenceSessionFactoryDelegate);
    }

    @Override
    public ObjectStorePersistedObjects createPersistedObjects() {
        return new BddObjectStorePersistedObjects();
    }

}
