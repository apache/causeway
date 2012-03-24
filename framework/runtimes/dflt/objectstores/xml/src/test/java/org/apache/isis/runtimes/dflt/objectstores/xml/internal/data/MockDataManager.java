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

package org.apache.isis.runtimes.dflt.objectstores.xml.internal.data;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import com.google.common.collect.Lists;

import org.apache.isis.runtimes.dflt.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;
import org.apache.isis.runtimes.dflt.runtime.transaction.ObjectPersistenceException;

public class MockDataManager implements DataManager {
    
    private final List<ObjectData> actions = Lists.newArrayList();

    public void assertAction(final int i, final String action) {
        if (i >= actions.size()) {
            Assert.fail("No such action " + action);
        }
    }

    public MockDataManager() {
        super();
    }

    public RootOidDefault createOid() throws PersistorException {
        return null;
    }

    @Override
    public void insertObject(final ObjectData data) throws ObjectPersistenceException {
    }

    @Override
    public boolean isFixturesInstalled() {
        return true;
    }

    @Override
    public void remove(final RootOidDefault oid) throws ObjectNotFoundException, ObjectPersistenceException {
        final Iterator<ObjectData> iter = actions.iterator();
        while (iter.hasNext()) {
            final ObjectData data = iter.next();
            if (data.getOid().equals(oid)) {
                iter.remove();
            }
        }
    }

    @Override
    public void save(final Data data) throws ObjectPersistenceException {
        actions.add((ObjectData) data);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public ObjectDataVector getInstances(final ObjectData pattern) {
        final ObjectDataVector vector = new ObjectDataVector();
        final Iterator<ObjectData> i = actions.iterator();
        while (i.hasNext()) {
            final ObjectData data = (ObjectData) i.next();
            if (pattern.getSpecification().equals(data.getSpecification())) {
                vector.addElement(data);
            }
        }
        return vector;
    }

    @Override
    public Data loadData(final RootOidDefault oid) {
        final Iterator<ObjectData> i = actions.iterator();
        while (i.hasNext()) {
            final ObjectData data = (ObjectData) i.next();
            if (data.getOid().equals(oid)) {
                return data;
            }
        }
        return null;
    }

    @Override
    public int numberOfInstances(final ObjectData pattern) {
        return actions.size();
    }

    @Override
    public String getDebugData() {
        return null;
    }

}
