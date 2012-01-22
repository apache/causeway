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

package org.apache.isis.runtimes.dflt.objectstores.dflt.internal.commands;

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.dflt.internal.ObjectStoreInstances;
import org.apache.isis.runtimes.dflt.objectstores.dflt.internal.ObjectStorePersistedObjects;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommandAbstract;
import org.apache.isis.runtimes.dflt.runtime.transaction.ObjectPersistenceException;

public abstract class AbstractInMemoryPersistenceCommand extends PersistenceCommandAbstract {

    private final static Logger LOG = Logger.getLogger(AbstractInMemoryPersistenceCommand.class);

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
        final ObjectStoreInstances ins = instancesFor(specification);
        ins.save(adapter); // also sets the version
    }

    protected void destroy(final ObjectAdapter adapter) {
        final ObjectSpecification specification = adapter.getSpecification();
        if (LOG.isDebugEnabled()) {
            LOG.debug("   destroy object " + adapter + " as instance of " + specification.getShortIdentifier());
        }
        final ObjectStoreInstances ins = instancesFor(specification);
        ins.remove(adapter.getOid());
    }

    private ObjectStoreInstances instancesFor(final ObjectSpecification spec) {
        return persistedObjects.instancesFor(spec);
    }
}