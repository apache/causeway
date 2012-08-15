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

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;

/**
 * Implementation that simply delegates to underlying {@link ObjectStoreSpi}.
 * 
 * <p>
 * Useful for quickly writing decorating implementations.
 */
public class IsisObjectStoreDelegating implements ObjectStoreSpi {

    private final ObjectStoreSpi underlying;
    private final String name;

    public IsisObjectStoreDelegating(final ObjectStoreSpi underlying, final String name) {
        this.underlying = underlying;
        this.name = name;
    }

    // ////////////////////////////////////////////////
    // name
    // ////////////////////////////////////////////////

    @Override
    public String name() {
        return name + "(" + underlying.name() + ")";
    }

    // ////////////////////////////////////////////////
    // init, shutdown, reset, isInitialized
    // ////////////////////////////////////////////////

    @Override
    public void open() {
        underlying.open();
    }

    @Override
    public void close() {
        underlying.close();
    }

    @Override
    public void reset() {
        underlying.reset();
    }

    @Override
    public boolean isFixturesInstalled() {
        return underlying.isFixturesInstalled();
    }

    // ////////////////////////////////////////////////
    // createXxxCommands
    // ////////////////////////////////////////////////

    @Override
    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        return underlying.createCreateObjectCommand(object);
    }

    @Override
    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter object) {
        return underlying.createDestroyObjectCommand(object);
    }

    @Override
    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter object) {
        return underlying.createSaveObjectCommand(object);
    }

    // ////////////////////////////////////////////////
    // execute
    // ////////////////////////////////////////////////

    @Override
    public void execute(final List<PersistenceCommand> commands) {
        underlying.execute(commands);
    }

    // ////////////////////////////////////////////////
    // TransactionManagement
    // ////////////////////////////////////////////////

    @Override
    public void startTransaction() {
        underlying.startTransaction();
    }

    @Override
    public void endTransaction() {
        underlying.endTransaction();
    }

    @Override
    public void abortTransaction() {
        underlying.abortTransaction();
    }

    // ////////////////////////////////////////////////
    // getObject, resolveImmediately, resolveField
    // ////////////////////////////////////////////////

    @Override
    public ObjectAdapter loadInstanceAndAdapt(final TypedOid oid) {
        return underlying.loadInstanceAndAdapt(oid);
    }

    @Override
    public void resolveField(final ObjectAdapter object, final ObjectAssociation field) {
        underlying.resolveField(object, field);
    }

    @Override
    public void resolveImmediately(final ObjectAdapter object) {
        underlying.resolveImmediately(object);
    }

    // ////////////////////////////////////////////////
    // getInstances, hasInstances
    // ////////////////////////////////////////////////

    @Override
    public List<ObjectAdapter> loadInstancesAndAdapt(final PersistenceQuery persistenceQuery) {
        return underlying.loadInstancesAndAdapt(persistenceQuery);
    }

    @Override
    public boolean hasInstances(final ObjectSpecification specification) {
        return underlying.hasInstances(specification);
    }

    // ////////////////////////////////////////////////
    // services
    // ////////////////////////////////////////////////

    @Override
    public RootOid getOidForService(ObjectSpecification serviceSpecification) {
        return underlying.getOidForService(serviceSpecification);
    }

    @Override
    public void registerService(final RootOid rootOid) {
        underlying.registerService(rootOid);
    }

    // ////////////////////////////////////////////////
    // debug
    // ////////////////////////////////////////////////

    @Override
    public void debugData(final DebugBuilder debug) {
        underlying.debugData(debug);
    }

    @Override
    public String debugTitle() {
        return underlying.debugTitle();
    }


}
