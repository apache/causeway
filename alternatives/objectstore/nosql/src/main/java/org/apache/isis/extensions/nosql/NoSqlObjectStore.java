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


package org.apache.isis.extensions.nosql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.runtime.persistence.query.PersistenceQuery;
import org.apache.isis.runtime.persistence.query.PersistenceQueryBuiltIn;

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

    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        // TODO should this be done at a higher level so it is applicable for all OSes
        if (object.getSpecification().isAggregated()) {
            //throw new UnexpectedCallException("Aggregated objects should not be created outside of their owner");
            return null;
        } else {
            return new WriteObjectCommand(false, keyCreator, versionCreator, object);
        }
    }

    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter object) {
        if (object.getSpecification().isAggregated()) {
            throw new NoSqlStoreException("Can't delete an aggregated object");
        } else {
            return new DestroyObjectCommandImplementation(keyCreator, versionCreator, object);
        }
    }

    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter object) {
        // TODO should this be done at a higher level so it is applicable for all OSes
        if (object.getSpecification().isAggregated()) {
            Oid parentOid = ((AggregatedOid) object.getOid()).getParentOid();
            ObjectAdapter parent = IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(parentOid);
            return new WriteObjectCommand(true, keyCreator, versionCreator, parent);
        } else {
            return new WriteObjectCommand(true, keyCreator, versionCreator, object);
        }
    }

    public void execute(List<PersistenceCommand> commands) {
        database.write(commands);
    }

    public ObjectAdapter[] getInstances(PersistenceQuery persistenceQuery) {
        String specificationName = persistenceQuery.getSpecification().getFullName();
        Iterator<StateReader> instanceData = database.instancesOf(specificationName);

        List<ObjectAdapter> instances = new ArrayList<ObjectAdapter>();
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
        return instances.toArray(new ObjectAdapter[instances.size()]);
    }

    public ObjectAdapter getObject(Oid oid, ObjectSpecification hint) {
        String key = keyCreator.key(oid);
        StateReader reader = database.getInstance(key, hint.getFullName());
        return objectReader.load(reader, keyCreator, versionCreator);
    }

    public Oid getOidForService(String name) {
        Oid oid = serviceCache.get(name);
        if (oid == null) {
            String id = database.getService(name);
            oid = id == null ? null : keyCreator.oid(id);
            serviceCache.put(name, oid);
        }
        return oid;
    }

    public boolean hasInstances(ObjectSpecification specification) {
        return database.hasInstances(specification.getFullName());
    }

    public boolean isFixturesInstalled() {
        return isDataLoaded;
    }

    public void registerService(String name, Oid oid) {
        String key = keyCreator.key(oid);
        database.addService(name, key);
    }

    public void reset() {}

    public void resolveField(ObjectAdapter object, ObjectAssociation field) {
        ObjectAdapter fieldValue = field.get(object);
        if (fieldValue != null) {
            resolveImmediately(fieldValue);
        }
    }

    public void resolveImmediately(ObjectAdapter object) {
        Oid oid= object.getOid();;
        if (oid instanceof AggregatedOid) {
            throw new UnexpectedCallException("Aggregated objects should not need to be resolved: " + object);
        } else {
            String specificationName = object.getSpecification().getFullName();
            String key = keyCreator.key(oid);
            StateReader reader = database.getInstance(key, specificationName);
            objectReader.update(reader, keyCreator, versionCreator, object);
        }
    }

    public void debugData(DebugString debug) {
        // TODO show details
    }

    public String debugTitle() {
        return "NoSql Object Store";
    }

    public void close() {
        database.close();
    }

    public void open() {
        database.open();
    }

    public String name() {
        return "personal object store";
    }

    public void abortTransaction() {}

    public void endTransaction() {}

    public void startTransaction() {}

}


