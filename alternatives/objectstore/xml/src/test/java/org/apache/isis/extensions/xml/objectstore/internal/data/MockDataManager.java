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


package org.apache.isis.extensions.xml.objectstore.internal.data;

import java.util.Iterator;
import java.util.Vector;

import junit.framework.Assert;

import org.apache.isis.core.runtime.transaction.ObjectPersistenceException;
import org.apache.isis.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SerialOid;


public class MockDataManager implements DataManager {
    private final Vector actions = new Vector();

    public void assertAction(final int i, final String action) {
        if (i >= actions.size()) {
            Assert.fail("No such action " + action);
        }
        // Assert.assertEquals(action, actions.elementAt(i));
    }

    public MockDataManager() {
        super();
    }

    public SerialOid createOid() throws PersistorException {
        return null;
    }

    public void insertObject(final ObjectData data) throws ObjectPersistenceException {}

    public boolean isFixturesInstalled() {
        return true;
    }

    public void remove(final SerialOid oid) throws ObjectNotFoundException, ObjectPersistenceException {
    	ObjectDataVector vector = new ObjectDataVector();
        Iterator i = actions.iterator();
        while (i.hasNext()) {
        	ObjectData data = (ObjectData)i.next();
            if (data.getOid().equals(oid))
             	actions.remove(data);        		  
        }
    }

    public void save(final Data data) throws ObjectPersistenceException {
        actions.addElement(data);
    }

    public void shutdown() {}

    public ObjectDataVector getInstances(final ObjectData pattern) {
    	ObjectDataVector vector = new ObjectDataVector();
        Iterator i = actions.iterator();
        while (i.hasNext()) {
        	ObjectData data = (ObjectData)i.next();
          if (pattern.getSpecification().equals(data.getSpecification()))
        		  vector.addElement(data);
        }
    	
        return vector;
    }

    public Data loadData(final SerialOid oid) {
       	ObjectDataVector vector = new ObjectDataVector();
        Iterator i = actions.iterator();
        while (i.hasNext()) {
        	ObjectData data = (ObjectData)i.next();
          if (data.getOid().equals(oid))
        		  return data;
        }
         return null;
    }

    public int numberOfInstances(final ObjectData pattern) {
        return actions.size();
    }

    public String getDebugData() {
        return null;
    }

}
