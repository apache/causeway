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
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.core.runtime.persistence.query.PersistenceQuery;

/**
 * Implementation that simply delegates to underlying {@link ObjectStore}.
 * 
 * <p>
 * Useful for quickly writing decorating implementations.
 */
public abstract class IsisStoreDelegating implements ObjectStore {
    
    private final ObjectStore underlying;
    private final String name;

    public IsisStoreDelegating(final ObjectStore underlying, final String name) {
        this.underlying = underlying;
        this.name = name;
    }

    
    //////////////////////////////////////////////////
    // name
    //////////////////////////////////////////////////

    public String name() {
        return name + "(" + underlying.name() + ")";
    }

    //////////////////////////////////////////////////
    // init, shutdown, reset, isInitialized
    //////////////////////////////////////////////////

    public void open() {
        underlying.open();
    }

    public void close() {
        underlying.close();
    }
    
    public void reset() {
        underlying.reset();
    }

    public boolean isFixturesInstalled() {
        return underlying.isFixturesInstalled();
    }


    //////////////////////////////////////////////////
    // createXxxCommands
    //////////////////////////////////////////////////

    public CreateObjectCommand createCreateObjectCommand(ObjectAdapter object) {
        return underlying.createCreateObjectCommand(object);
    }

    public DestroyObjectCommand createDestroyObjectCommand(ObjectAdapter object) {
        return underlying.createDestroyObjectCommand(object);
    }

    public SaveObjectCommand createSaveObjectCommand(ObjectAdapter object) {
        return underlying.createSaveObjectCommand(object);
    }

    //////////////////////////////////////////////////
    // execute
    //////////////////////////////////////////////////

    public void execute(final List<PersistenceCommand> commands) {
        underlying.execute(commands);
    }

    
    //////////////////////////////////////////////////
    // TransactionManagement
    //////////////////////////////////////////////////
    
    public void startTransaction() {
        underlying.startTransaction();
    }
    
    public void endTransaction() {
        underlying.endTransaction();
    }

    public void abortTransaction() {
        underlying.abortTransaction();
    }
    
    //////////////////////////////////////////////////
    // getObject, resolveImmediately, resolveField
    //////////////////////////////////////////////////

    public ObjectAdapter getObject(Oid oid, ObjectSpecification hint) {
        return underlying.getObject(oid, hint);
    }

    public void resolveField(ObjectAdapter object, ObjectAssociation field) {
        underlying.resolveField(object, field);
    }

    public void resolveImmediately(ObjectAdapter object) {
        underlying.resolveImmediately(object);
    }

    
    //////////////////////////////////////////////////
    // getInstances, hasInstances
    //////////////////////////////////////////////////

    public ObjectAdapter[] getInstances(PersistenceQuery persistenceQuery) {
        return underlying.getInstances(persistenceQuery);
    }

    public boolean hasInstances(ObjectSpecification specification) {
        return underlying.hasInstances(specification);
    }

        
    //////////////////////////////////////////////////
    // services
    //////////////////////////////////////////////////

    public Oid getOidForService(String name) {
        return underlying.getOidForService(name);
    }

    public void registerService(String name, Oid oid) {
        underlying.registerService(name, oid);
    }

    //////////////////////////////////////////////////
    // debug
    //////////////////////////////////////////////////

    public void debugData(DebugString debug) {
        underlying.debugData(debug);
    }

    public String debugTitle() {
        return underlying.debugTitle();
    }

}
