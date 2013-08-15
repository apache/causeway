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

package org.apache.isis.objectstore.nosql.db.mongo;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.objectstore.nosql.db.StateReader;

public class MongoStateReader implements StateReader {
    
    private static final Logger LOG = LoggerFactory.getLogger(MongoStateReader.class);
    private final DBObject instance;

    public MongoStateReader(final DB db, final ObjectSpecId objectSpecId, final String mongoId) {
        final DBCollection instances = db.getCollection(objectSpecId.asString());
        instance = instances.findOne(mongoId);
        if (instance == null) {
            throw new ObjectNotFoundException(mongoId);
        }
        if(LOG.isDebugEnabled()) {
            LOG.debug("loading " + instance);
        }
    }

    public MongoStateReader(final DBObject instance) {
        this.instance = instance;
        if(LOG.isDebugEnabled()) {
            LOG.debug("loading " + instance);
        }
    }

    @Override
    public long readLongField(final String id) {
        final Object value = instance.get(id);
        if (value == null || value.equals("null")) {
            return 0;
        } 
        return Long.valueOf((String) value);
    }

    @Override
    public String readField(final String name) {
        final Object value = instance.get(name);
        if (value == null || value.equals("null")) {
            return null;
        } else {
            return (String) value;
        }
    }

    @Override
    public String readEncrytionType() {
        return (String) instance.get(PropertyNames.ENCRYPT);
    }

    @Override
    public String readOid() {
        return readField(PropertyNames.OID);
    }

    @Override
    public String readVersion() {
        return readField(PropertyNames.VERSION);
    }

    @Override
    public String readUser() {
        return readField(PropertyNames.USER);
    }

    @Override
    public String readTime() {
        return readField(PropertyNames.TIME);
    }

    @Override
    public StateReader readAggregate(final String id) {
        DBObject object = (DBObject) instance.get(id);
        return object == null ? null : new MongoStateReader(object);
    }

    @Override
    public List<StateReader> readCollection(final String id) {
        BasicDBList array = (BasicDBList) instance.get(id);
        final List<StateReader> readers = new ArrayList<StateReader>();
        if (array != null) {
            final int size = array.size();
            for (int i = 0; i < size; i++) {
                readers.add(new MongoStateReader((DBObject) array.get(i)));
            }
        }
        return readers;
    }

}
