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


package org.apache.isis.core.runtime.persistence.objectstore;

import java.util.List;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.factory.InstanceCreationException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.config.ConfigurationException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.logging.Logger;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.persistence.UnsupportedFindException;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.core.runtime.persistence.query.PersistenceQuery;
import org.apache.isis.core.runtime.transaction.ObjectPersistenceException;


public class IsisStoreLogger extends Logger implements ObjectStore {
    private final ObjectStore decorated;

    public IsisStoreLogger(final ObjectStore decorated, final String level) {
        super(level);
        this.decorated = decorated;
    }

    public IsisStoreLogger(final ObjectStore decorated) {
        this.decorated = decorated;
    }

    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        log("Create object " + object);
        return decorated.createCreateObjectCommand(object);
    }

    public void registerService(final String name, final Oid oid) {
        log("register service " + name + " as " + oid);
        decorated.registerService(name, oid);
    }

    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter object) {
        log("Destroy object " + object);
        return decorated.createDestroyObjectCommand(object);
    }

    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter object) {
        log("Save object " + object);
        return decorated.createSaveObjectCommand(object);
    }

    public void debugData(final DebugString debug) {
        decorated.debugData(debug);
    }

    public String debugTitle() {
        return decorated.debugTitle();
    }

    @Override
    protected Class<?> getDecoratedClass() {
        return decorated.getClass();
    }

    public ObjectAdapter[] getInstances(final PersistenceQuery criteria) throws ObjectPersistenceException,
            UnsupportedFindException {
        log("Get instances matching " + criteria);
        return decorated.getInstances(criteria);
    }

    public ObjectAdapter getObject(final Oid oid, final ObjectSpecification hint) throws ObjectNotFoundException,
            ObjectPersistenceException {
        final ObjectAdapter object = decorated.getObject(oid, hint);
        log("Get object for " + oid + " (of type " + hint.getShortName() + ")", object.getObject());
        return object;
    }

    public Oid getOidForService(final String name) {
        final Oid oid = decorated.getOidForService(name);
        log("Get OID for service " + name + ": " + oid);
        return oid;
    }

    public boolean hasInstances(final ObjectSpecification specification)
            throws ObjectPersistenceException {
        final boolean hasInstances = decorated.hasInstances(specification);
        log("Has instances of " + specification.getShortName(), "" + hasInstances);
        return hasInstances;
    }

    public boolean isFixturesInstalled() {
        final boolean isInitialized = decorated.isFixturesInstalled();
        log("is initialized: " + isInitialized);
        return isInitialized;
    }

    public void open() throws ConfigurationException, InstanceCreationException, ObjectPersistenceException {
        log("Opening " + name());
        decorated.open();
    }

    public String name() {
        return decorated.name();
    }

    public void reset() {
        log("Reset");
        decorated.reset();
    }

    public void resolveField(final ObjectAdapter object, final ObjectAssociation field) throws ObjectPersistenceException {
        log("Resolve eagerly object in field " + field + " of " + object);
        decorated.resolveField(object, field);
    }

    public void resolveImmediately(final ObjectAdapter object) throws ObjectPersistenceException {
        log("Resolve immediately: " + object);
        decorated.resolveImmediately(object);
    }

    public void execute(final List<PersistenceCommand> commands) throws ObjectPersistenceException {
        log("Execute commands");
        int i = 0;
        for (PersistenceCommand command: commands) {
            log("  " + (i++) + " " + command);
        }
        decorated.execute(commands);
    }

    public void close() throws ObjectPersistenceException {
        log("Closing " + decorated);
        decorated.close();
    }

    public void startTransaction() {
        decorated.startTransaction();
    }

    public void endTransaction() {
        decorated.endTransaction();
    }
    
    public void abortTransaction() {
        decorated.abortTransaction();
    }

}
