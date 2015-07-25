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

package org.apache.isis.core.objectstore.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.objectstore.internal.ObjectStoreInstances;
import org.apache.isis.core.objectstore.internal.ObjectStorePersistedObjects;
import org.apache.isis.core.runtime.persistence.ObjectPersistenceException;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommandAbstract;

public abstract class AbstractInMemoryPersistenceCommand extends PersistenceCommandAbstract {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractInMemoryPersistenceCommand.class);

    private final ObjectStorePersistedObjects persistedObjects;

    public AbstractInMemoryPersistenceCommand(final ObjectAdapter adapter, final ObjectStorePersistedObjects persistedObjects) {
        super(adapter);
        this.persistedObjects = persistedObjects;
    }

    protected void save(final ObjectAdapter adapter) throws ObjectPersistenceException {
        final ObjectSpecification specification = adapter.getSpecification();
        if (LOG.isDebugEnabled()) {
            LOG.debug("   saving object " + adapter + " as instance of " + specification.getShortIdentifier());
        }
        final ObjectStoreInstances ins = instancesFor(specification.getSpecId());
        ins.save(adapter); // also sets the version
    }

    protected void destroy(final ObjectAdapter adapter) {
        final ObjectSpecification specification = adapter.getSpecification();
        if (LOG.isDebugEnabled()) {
            LOG.debug("   destroy object " + adapter + " as instance of " + specification.getShortIdentifier());
        }
        final ObjectStoreInstances ins = instancesFor(specification.getSpecId());
        ins.remove(adapter.getOid());
    }

    private ObjectStoreInstances instancesFor(final ObjectSpecId spec) {
        return persistedObjects.instancesFor(spec);
    }
}
