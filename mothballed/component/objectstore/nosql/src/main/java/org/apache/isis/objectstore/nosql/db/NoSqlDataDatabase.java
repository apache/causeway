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

package org.apache.isis.objectstore.nosql.db;

import java.util.Iterator;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;

public interface NoSqlDataDatabase {

    void open();
    void close();
    
    boolean containsData();

    void addService(ObjectSpecId objectSpecId, String key);
    String getService(ObjectSpecId objectSpecId);

    boolean hasInstances(ObjectSpecId objectSpecId);
    StateReader getInstance(String key, ObjectSpecId objectSpecId);
    Iterator<StateReader> instancesOf(ObjectSpecId objectSpecId);
    Iterator<StateReader> instancesOf(ObjectSpecId specId, ObjectAdapter pattern);

    long nextSerialNumberBatch(ObjectSpecId objectSpecId, int batchSize);

    void write(List<PersistenceCommand> commands);
}
