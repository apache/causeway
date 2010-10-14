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


package org.apache.isis.runtime.objectstore.inmemory;

import java.util.Collections;

import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtime.testsystem.ProxyJunit3TestCase;


public class InMemoryObjectStore_debug extends ProxyJunit3TestCase {
    private InMemoryObjectStore store;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        store = new InMemoryObjectStore();
        store.open();
    }

    public void testObject() throws Exception {
        final ObjectAdapter object = system.createPersistentTestObject();

        final CreateObjectCommand command = store.createCreateObjectCommand(object);
        store.execute(Collections.<PersistenceCommand>singletonList(command));

        store.debugTitle();
        final DebugString debug = new DebugString();
        store.debugData(debug);
    }

    public void testEmpty() throws Exception {
        store.debugTitle();
        final DebugString debug = new DebugString();
        store.debugData(debug);
    }
}

