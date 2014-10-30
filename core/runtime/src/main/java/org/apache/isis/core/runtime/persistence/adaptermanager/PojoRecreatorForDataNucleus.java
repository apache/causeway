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
package org.apache.isis.core.runtime.persistence.adaptermanager;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusObjectStore;

class PojoRecreatorForDataNucleus implements PojoRecreator {

    private final PojoRecreator delegate = new PojoRecreatorDefault();
    
    @Override
    public Object recreatePojo(TypedOid oid) {
        if(oid.isTransient() || oid.isViewModel()) {
            return delegate.recreatePojo(oid);
        }
        return getObjectStore().loadPojo(oid);
    }

    
    @Override
    public ObjectAdapter lazilyLoaded(Object pojo) {
        return getObjectStore().lazilyLoaded(pojo);
    }

    ///////////////////////////////
    

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected DataNucleusObjectStore getObjectStore() {
        return (DataNucleusObjectStore) getPersistenceSession().getObjectStore();
    }

    
    
    
}

