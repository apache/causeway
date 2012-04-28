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

package org.apache.isis.runtimes.dflt.objectstores.nosql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.objectstores.nosql.db.NoSqlDataDatabase;
import org.apache.isis.runtimes.dflt.objectstores.nosql.db.StateReader;
import org.apache.isis.runtimes.dflt.objectstores.nosql.encryption.DataEncryption;
import org.apache.isis.runtimes.dflt.objectstores.nosql.keys.KeyCreator;
import org.apache.isis.runtimes.dflt.objectstores.nosql.versions.VersionCreator;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryBuiltIn;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

public class NoSqlObjectStore implements ObjectStore {
    
    private final NoSqlDataDatabase database;
    private final Map<String, RootOid> servicesByObjectType = Maps.newHashMap();
    private final KeyCreator keyCreator;
    private final VersionCreator versionCreator;
    private final ObjectReader objectReader = new ObjectReader();
    private final NoSqlOidGenerator oidGenerator;
    private final DataEncryption wrtingDataEncrypter;
    private final Map<String, DataEncryption> availableDataEncrypters;
    private final boolean isDataLoaded;

    public NoSqlObjectStore(final NoSqlDataDatabase db, final NoSqlOidGenerator oidGenerator, final KeyCreator keyCreator, final VersionCreator versionCreator, final DataEncryption writingDataEncrypter, final Map<String, DataEncryption> availableDataEncrypters) {
        this.database = db;
        this.oidGenerator = oidGenerator;
        this.keyCreator = keyCreator;
        this.versionCreator = versionCreator;
        this.wrtingDataEncrypter = writingDataEncrypter;
        this.availableDataEncrypters = availableDataEncrypters;

        db.open();
        isDataLoaded = db.containsData();
        db.close();
    }

    public OidGenerator getOidGenerator() {
        return oidGenerator;
    }

    @Override
    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        // TODO should this be done at a higher level so it is applicable for
        // all OSes
        if (object.getSpecification().isParented()) {
            // throw new
            // UnexpectedCallException("Aggregated objects should not be created outside of their owner");
            return null;
        } else {
            return new NoSqlCreateObjectCommand(keyCreator, versionCreator, wrtingDataEncrypter, object);
        }
    }

    @Override
    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter adapter) {
        if (adapter.getSpecification().isParented()) {
            throw new NoSqlStoreException("Can't delete an aggregated object");
        } else {
            return new NoSqlDestroyObjectCommand(keyCreator, versionCreator, adapter);
        }
    }

    @Override
    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter adapter) {
        // TODO should this be done at a higher level so it is applicable for
        // all OSes
        final ObjectAdapter rootAdapter = adapter.getAggregateRoot();
        if (rootAdapter.getOid() instanceof AggregatedOid) {
            throw new NoSqlStoreException("Unexpected aggregated object to save: " + rootAdapter + " (" + adapter + ")");
        }
        return new NoSqlSaveObjectCommand(keyCreator, versionCreator, wrtingDataEncrypter, rootAdapter);
    }

    @Override
    public void execute(final List<PersistenceCommand> commands) {
        database.write(commands);
    }

    @Override
    public ObjectAdapter[] getInstances(final PersistenceQuery persistenceQuery) {
        final ObjectSpecification specification = persistenceQuery.getSpecification();
        final List<ObjectAdapter> instances = new ArrayList<ObjectAdapter>();
        instances(persistenceQuery, specification, instances);
        return instances.toArray(new ObjectAdapter[instances.size()]);
    }

    private void instances(final PersistenceQuery persistenceQuery, final ObjectSpecification specification, final List<ObjectAdapter> instances) {
        String specificationName = specification.getFullIdentifier();
        final Iterator<StateReader> instanceData = database.instancesOf(specificationName);
        while (instanceData.hasNext()) {
            final StateReader reader = instanceData.next();
            final ObjectAdapter instance = objectReader.load(reader, keyCreator, versionCreator, availableDataEncrypters);
            // TODO deal with this natively
            if (persistenceQuery instanceof PersistenceQueryBuiltIn) {
                if (!((PersistenceQueryBuiltIn) persistenceQuery).matches(instance)) {
                    continue;
                }
            }
            instances.add(instance);
        }
        for (final ObjectSpecification spec : specification.subclasses()) {
            specificationName = spec.getFullIdentifier();
            instances(persistenceQuery, spec, instances);
        }
    }

    @Override
    public ObjectAdapter getObject(final Oid oid, final ObjectSpecification hint) {
        final String key = keyCreator.key(oid);
        final StateReader reader = database.getInstance(key, hint.getFullIdentifier());
        return objectReader.load(reader, keyCreator, versionCreator, availableDataEncrypters);
    }

    @Override
    public boolean hasInstances(final ObjectSpecification specification) {
        return database.hasInstances(specification.getFullIdentifier());
    }

    @Override
    public boolean isFixturesInstalled() {
        return isDataLoaded;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resolveField(final ObjectAdapter object, final ObjectAssociation field) {
        final ObjectAdapter fieldValue = field.get(object);
        if (fieldValue != null && !fieldValue.getResolveState().isResolved() && !fieldValue.getSpecification().isParented()) {
            resolveImmediately(fieldValue);
        }
    }

    @Override
    public void resolveImmediately(final ObjectAdapter object) {
        final Oid oid = object.getOid();
        ;
        if (oid instanceof AggregatedOid) {
            // throw new
            // UnexpectedCallException("Aggregated objects should not need to be resolved: "
            // +
            // object);
        } else {
            final String specificationName = object.getSpecification().getFullIdentifier();
            final String key = keyCreator.key(oid);
            final StateReader reader = database.getInstance(key, specificationName);
            objectReader.update(reader, keyCreator, versionCreator, availableDataEncrypters, object);
        }
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        // TODO show details
    }

    // ////////////////////////////////////////////////////////////////
    // open, close
    // ////////////////////////////////////////////////////////////////

    @Override
    public void close() {
        database.close();
    }

    @Override
    public void open() {
        database.open();
    }

    @Override
    public String name() {
        return "nosql";
    }

    
    // ////////////////////////////////////////////////////////////////
    // Services
    // ////////////////////////////////////////////////////////////////
    
    @Override
    public void registerService(final RootOid rootOid) {
        final String key = keyCreator.key(rootOid);
        database.addService(rootOid.getObjectType(), key);
    }

    @Override
    public RootOid getOidForService(ObjectSpecification serviceSpec) {
        final String objectType = serviceSpec.getObjectType();
        RootOid oid = servicesByObjectType.get(objectType);
        if (oid == null) {
            final String id = database.getService(objectType);
            if (id == null) {
                oid = null;
            } else {
                oid = keyCreator.oid(serviceSpec, id);
            }
            servicesByObjectType.put(objectType, oid);
        }
        return oid;
    }


    // ////////////////////////////////////////////////////////////////
    // Transaction Mgmt
    // ////////////////////////////////////////////////////////////////

    @Override
    public void abortTransaction() {
    }

    @Override
    public void endTransaction() {
    }

    @Override
    public void startTransaction() {
    }

    
    // ////////////////////////////////////////////////////////////////
    // debugging
    // ////////////////////////////////////////////////////////////////

    @Override
    public String debugTitle() {
        return "NoSql Object Store";
    }


    // ////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ////////////////////////////////////////////////////////////////

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}
