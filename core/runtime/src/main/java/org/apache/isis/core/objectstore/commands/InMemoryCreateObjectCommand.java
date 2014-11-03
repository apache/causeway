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
import org.apache.isis.core.objectstore.internal.ObjectStorePersistedObjects;
import org.apache.isis.core.runtime.persistence.ObjectPersistenceException;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommandContext;

public final class InMemoryCreateObjectCommand extends AbstractInMemoryPersistenceCommand implements CreateObjectCommand {
    private final static Logger LOG = LoggerFactory.getLogger(InMemoryCreateObjectCommand.class);

    public InMemoryCreateObjectCommand(final ObjectAdapter object, final ObjectStorePersistedObjects persistedObjects) {
        super(object, persistedObjects);
    }

    @Override
    public void execute(final PersistenceCommandContext context) throws ObjectPersistenceException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("  create object " + onAdapter());
        }
        save(onAdapter());
    }

    @Override
    public String toString() {
        return "CreateObjectCommand [object=" + onAdapter() + "]";
    }
}
