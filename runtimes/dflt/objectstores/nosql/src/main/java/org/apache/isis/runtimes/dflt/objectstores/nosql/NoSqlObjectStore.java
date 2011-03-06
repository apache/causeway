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


package org.apache.isis.runtimes.dflt.objecstores.nosql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQuery;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryBuiltIn;

public class NoSqlObjectStore implements ObjectStore {
    private final NoSqlDataDatabase database;
    private final Map<String, Oid> serviceCache = new HashMap<String, Oid>();
    private final KeyCreator keyCreator;
    private final VersionCreator versionCreator;
    private final ObjectReader objectReader = new ObjectReader();
    private final NoSqlOidGenerator oidGenerator;
    private final boolean isDataLoaded;
    
    public NoSqlObjectStore(NoSqlDataDatabase db, NoSqlOidGenerator oidGenerator, KeyCreator keyCreator, VersionCreator versionCreator) {
        this.database = db;
        this.oidGenerator = oidGenerator;
        this.keyCreator = keyCreator;
        this.versionCreator = versionCreator;
        
        db.open();
        isDataLoaded = db.containsData();
        db.close();
    }

    public OidGenerator getOidGenerator() {
        return oidGenerator;
    }

    @Override
    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        // TODO should this be done at a higher level so it is applicable for all OSes
        if (object.getSpecification().isAggregated()) {
            //throw new UnexpectedCallException("Aggregated objects should not be created outside of their owner");
            return null;
        } else {
            return new WriteObjectCommand(false, keyCreator, versionCreator, object);
        }
    }

    @Override
    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter object) {
        if (object.getSpecification().isAggregated()) {
            throw new NoSqlStoreException("Can't delete an aggregated object");
        } else {
            return new DestroyObjectCommandImplementation(keyCreator, versionCreator, object);
        }
    }

    @Override
    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter adapter) {
        // TODO should this be done at a higher level so it is applicable for all OSes
        final ObjectAdapter rootAdapter = aggregateRootAdapterFor(adapter);
        return new WriteObjectCommand(true, keyCreator, versionCreator, rootAdapter);
    }

    /**
     * Returns either itself, or its parent adapter (if aggregated)
     */
    public ObjectAdapter aggregateRootAdapterFor(final ObjectAdapter adapter) {
        return adapter.getAggregateRoot();
    }

    @Override
    public void execute(List<PersistenceCommand> commands) {
        database.write(commands);
    }

    @Override
    public ObjectAdapter[] getInstances(PersistenceQuery persistenceQuery) {
        ObjectSpecification specification = persistenceQuery.getSpecification(); 
        List<ObjectAdapter> instances = new ArrayList<ObjectAdapter>(); 
        instances(persistenceQuery, specification, instances); 
        return instances.toArray(new ObjectAdapter[instances.size()]); 
    } 

    private void instances(PersistenceQuery persistenceQuery, ObjectSpecification specification, List<ObjectAdapter> instances) { 
        String specificationName = specification.getFullIdentifier(); 
        Iterator<StateReader> instanceData = database.instancesOf(specificationName);
        while(instanceData.hasNext()) {
            StateReader reader = instanceData.next();
            ObjectAdapter instance = objectReader.load(reader, keyCreator, versionCreator);
            // TODO deal with this natively
            if (persistenceQuery instanceof PersistenceQueryBuiltIn) {
                if (!((PersistenceQueryBuiltIn)persistenceQuery).matches(instance)) {
                    continue;
                }
            }
            instances.add(instance);
        }
        for (ObjectSpecification spec : specification.subclasses()) { 
            specificationName = spec.getFullIdentifier(); 
            instances(persistenceQuery, spec, instances); 
        } 
    }

    @Override
    public ObjectAdapter getObject(Oid oid, ObjectSpecification hint) {
        String key = keyCreator.key(oid);
        StateReader reader = database.getInstance(key, hint.getFullIdentifier());
        return objectReader.load(reader, keyCreator, versionCreator);
    }

    @Override
    public Oid getOidForService(String name) {
        Oid oid = serviceCache.get(name);
        if (oid == null) {
            String id = database.getService(name);
            oid = id == null ? null : keyCreator.oid(id);
            serviceCache.put(name, oid);
        }
        return oid;
    }

    @Override
    public boolean hasInstances(ObjectSpecification specification) {
        return database.hasInstances(specification.getFullIdentifier());
    }

    @Override
    public boolean isFixturesInstalled() {
        return isDataLoaded;
    }

    @Override
    public void registerService(String name, Oid oid) {
        String key = keyCreator.key(oid);
        database.addService(name, key);
    }

    @Override
    public void reset() {}

    @Override
    public void resolveField(ObjectAdapter object, ObjectAssociation field) {
        ObjectAdapter fieldValue = field.get(object);
        if (fieldValue != null) {
            resolveImmediately(fieldValue);
        }
    }

    @Override
    public void resolveImmediately(ObjectAdapter object) {
        Oid oid= object.getOid();;
        if (oid instanceof AggregatedOid) {
            throw new UnexpectedCallException("Aggregated objects should not need to be resolved: " + object);
        } else {
            String specificationName = object.getSpecification().getFullIdentifier();
            String key = keyCreator.key(oid);
            StateReader reader = database.getInstance(key, specificationName);
            objectReader.update(reader, keyCreator, versionCreator, object);
        }
    }

    @Override
    public void debugData(DebugString debug) {
        // TODO show details
    }

    @Override
    public String debugTitle() {
        return "NoSql Object Store";
    }

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
        return "personal object store";
    }

    @Override
    public void abortTransaction() {}

    @Override
    public void endTransaction() {}

    @Override
    public void startTransaction() {}


    //////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    //////////////////////////////////////////////////////////////////
    
    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }


}


