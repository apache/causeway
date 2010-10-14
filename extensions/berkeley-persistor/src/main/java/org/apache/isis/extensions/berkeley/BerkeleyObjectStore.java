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


package org.apache.isis.extensions.berkeley;

import java.util.List;

import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SimpleOidGenerator;
import org.apache.isis.runtime.persistence.query.PersistenceQuery;

public class BerkeleyObjectStore implements ObjectStore {
    private static final String SERIAL_NUMBER = "serialNumber";
    private SimpleOidGenerator oidGenerator;
    private BerkleyDb db;
    private boolean isDataLoaded;
    
    public BerkeleyObjectStore(IsisConfiguration configuration) {
        db = new BerkleyDb();
        
        
        db.open();
        isDataLoaded = db.containsData();
        byte[] data = db.read(SERIAL_NUMBER);
        long loadSerialNumber;
        if( data == null) {
            loadSerialNumber = 1;
        } else {
            loadSerialNumber = Long.valueOf(new String(data));
        }
        oidGenerator = new SimpleOidGenerator(loadSerialNumber);
        db.close();
    }

    public OidGenerator getOidGenerator() {
        return oidGenerator;
    }

    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        return new CreateObjectCommand() {
            public void execute(PersistenceCommandContext context) {
                db.write(new ObjectData(object));
            }

            public ObjectAdapter onObject() {
                return object;
            }
        };
    }

    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter object) {
        return new DestroyObjectCommand() {
            public void execute(PersistenceCommandContext context) {
                db.delete(new ObjectData(object));
            }

            public ObjectAdapter onObject() {
                return object;
            }
        };
    }

    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter object) {
        return new SaveObjectCommand() {
            public void execute(PersistenceCommandContext context) {
                db.write(new ObjectData(object));
            }

            public ObjectAdapter onObject() {
                return object;
            }
        };
    }

    public void execute(List<PersistenceCommand> commands) {
        db.startTransaction();
        for (PersistenceCommand command : commands) {
            // TODO excute should be called with an "execution context" so commands can be run directly; there should be no need to pass in db to the command objects 
            command.execute(null);
        }
        saveOidSequence();
        db.endTransaction();
    }

    public ObjectAdapter[] getInstances(PersistenceQuery persistenceQuery) {
        ObjectData[] data = db.getAll(persistenceQuery.getSpecification());
        ObjectAdapter[] array = new ObjectAdapter[data.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = data[i].getObject();
        }
        return array;
    }

    public ObjectAdapter getObject(Oid oid, ObjectSpecification hint) {
        return db.getObject(oid, hint).getObject();
    }

    public Oid getOidForService(String name) {
        return db.getService(name);
    }

    public boolean hasInstances(ObjectSpecification specification) {
        return db.hasInstances(specification);
    }

    public boolean isFixturesInstalled() {
        return isDataLoaded;
    }

    public void registerService(String name, Oid oid) {
        db.addService(name, oid);
    }

    public void reset() {}

    public void resolveField(ObjectAdapter object, ObjectAssociation field) {
        ObjectAdapter fieldValue = field.get(object);
        if (fieldValue != null) {
            resolveImmediately(fieldValue);
        }
    }

    public void resolveImmediately(ObjectAdapter object) {
        db.update(new ObjectData(object));
    }

    public void debugData(DebugString debug) {
        // TODO show details
    }

    public String debugTitle() {
        return "Personal Object Store";
    }

    public void close() {
        db.close();
    }

    private void saveOidSequence() {
        long serialNumber = oidGenerator.getMemento().getPersistentSerialNumber();
        db.write(SERIAL_NUMBER, Long.toString(serialNumber));
    }

    public void open() {
        db.open();
    }

    public String name() {
        return "personal object store";
    }

    public void abortTransaction() {}

    public void endTransaction() {}

    public void startTransaction() {}

}


