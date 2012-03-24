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

package org.apache.isis.runtimes.dflt.runtime.persistence.objectstore;

import java.util.List;

import org.apache.isis.core.commons.config.IsisConfigurationException;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.factory.InstanceCreationException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.logging.Logger;
import org.apache.isis.runtimes.dflt.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.runtimes.dflt.runtime.persistence.UnsupportedFindException;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.runtimes.dflt.runtime.transaction.ObjectPersistenceException;

public class IsisStoreLogger extends Logger implements ObjectStore {
    private final ObjectStore decorated;

    public IsisStoreLogger(final ObjectStore decorated, final String level) {
        super(level);
        this.decorated = decorated;
    }

    public IsisStoreLogger(final ObjectStore decorated) {
        this.decorated = decorated;
    }

    @Override
    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        log("create object " + object);
        return decorated.createCreateObjectCommand(object);
    }

    @Override
    public void registerService(final String name, final Oid oid) {
        log("register service " + name + " as " + oid);
        decorated.registerService(name, oid);
    }

    @Override
    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter object) {
        log("destroy object " + object);
        return decorated.createDestroyObjectCommand(object);
    }

    @Override
    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter object) {
        log("save object " + object);
        return decorated.createSaveObjectCommand(object);
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        decorated.debugData(debug);
    }

    @Override
    public String debugTitle() {
        return decorated.debugTitle();
    }

    @Override
    protected Class<?> getDecoratedClass() {
        return decorated.getClass();
    }

    @Override
    public ObjectAdapter[] getInstances(final PersistenceQuery criteria) throws ObjectPersistenceException, UnsupportedFindException {
        log("get instances matching " + criteria);
        return decorated.getInstances(criteria);
    }

    @Override
    public ObjectAdapter getObject(final Oid oid, final ObjectSpecification hint) throws ObjectNotFoundException, ObjectPersistenceException {
        final ObjectAdapter object = decorated.getObject(oid, hint);
        log("get object for " + oid + " (of type " + hint.getShortIdentifier() + ")", object.getObject());
        return object;
    }

    @Override
    public Oid getOidForService(ObjectSpecification serviceSpecification, final String name) {
        final Oid oid = decorated.getOidForService(serviceSpecification, name);
        log("get OID for service " + name + ": " + oid);
        return oid;
    }

    @Override
    public boolean hasInstances(final ObjectSpecification specification) throws ObjectPersistenceException {
        final boolean hasInstances = decorated.hasInstances(specification);
        log("has instances of " + specification.getShortIdentifier(), "" + hasInstances);
        return hasInstances;
    }

    @Override
    public boolean isFixturesInstalled() {
        final boolean isInitialized = decorated.isFixturesInstalled();
        log("is initialized: " + isInitialized);
        return isInitialized;
    }

    @Override
    public void open() throws IsisConfigurationException, InstanceCreationException, ObjectPersistenceException {
        log("opening " + name());
        decorated.open();
    }

    @Override
    public String name() {
        return decorated.name();
    }

    @Override
    public void reset() {
        log("reset");
        decorated.reset();
    }

    @Override
    public void resolveField(final ObjectAdapter object, final ObjectAssociation field) throws ObjectPersistenceException {
        log("resolve eagerly object in field " + field + " of " + object);
        decorated.resolveField(object, field);
    }

    @Override
    public void resolveImmediately(final ObjectAdapter object) throws ObjectPersistenceException {
        log("resolve immediately: " + object);
        decorated.resolveImmediately(object);
    }

    @Override
    public void execute(final List<PersistenceCommand> commands) throws ObjectPersistenceException {
        log("execute commands");
        int i = 0;
        for (final PersistenceCommand command : commands) {
            log("  " + (i++) + " " + command);
        }
        decorated.execute(commands);
    }

    @Override
    public void close() throws ObjectPersistenceException {
        log("closing " + decorated);
        decorated.close();
    }

    @Override
    public void startTransaction() {
        decorated.startTransaction();
    }

    @Override
    public void endTransaction() {
        decorated.endTransaction();
    }

    @Override
    public void abortTransaction() {
        decorated.abortTransaction();
    }

}
