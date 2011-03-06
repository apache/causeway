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


package org.apache.isis.runtimes.dflt.objecstores.nosql.mongo;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.isis.runtimes.dflt.objecstores.nosql.StateReader;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.runtimes.dflt.runtime.persistence.ObjectNotFoundException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoStateReader implements StateReader{
    private static final Logger LOG = Logger.getLogger(MongoStateReader.class);
    private DBObject instance;

    public MongoStateReader(DB db, String specName, String key) {
        DBCollection instances = db.getCollection(specName);
        instance = instances.findOne(key);
        if (instance == null) {
            throw new ObjectNotFoundException(key);
        }
        LOG.debug("loading " + instance);
    }
   
    public MongoStateReader(DBObject instance) {
        this.instance = instance;
        LOG.debug("loading " + instance);
    }

    public long readLongField(String id) {
        Object value = instance.get(id);
        if (value == null || value.equals("null")) {
            return 0;
        } else {
            return Long.valueOf((String) value);
        }
    }

    public String readField(String name) {
        Object value = instance.get(name);
        if (value == null || value.equals("null")) {
            return null;
        } else {
            return (String) value;
        }
    }

    public String readObjectType() {
        return (String) instance.get("_type");
    }

    public String readId() {
        return readField("_id");
    }

    public String readVersion() {
        return null;
    }

    public String readUser() {
        return null;
    }

    public String readTime() {
        return null;
    }

    public StateReader readAggregate(String id) {
        throw new UnexpectedCallException();
    }

    public List<StateReader> readCollection(String id) {
        throw new UnexpectedCallException();
    }
    
}


