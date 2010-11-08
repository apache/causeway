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


package org.apache.isis.extensions.html.context;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.memento.Memento;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;


public class TransientObjectMapping implements ObjectMapping {
    private final Oid oid;
    private final Memento memento;

    public TransientObjectMapping(final ObjectAdapter adapter) {
        oid = adapter.getOid();
        Assert.assertTrue("OID is for persistent", oid.isTransient());
        Assert.assertTrue("adapter is for persistent", adapter.isTransient());
        memento = new Memento(adapter);
    }

    public void debug(final DebugString debug) {
        memento.debug(debug);
    }

    public Oid getOid() {
        return oid;
    }

    public ObjectAdapter getObject() {
        return getAdapterManager().getAdapterFor(oid);
    }

    @Override
    public int hashCode() {
        return oid.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof TransientObjectMapping) {
            return ((TransientObjectMapping) obj).oid.equals(oid);
        }
        return false;
    }

    @Override
    public String toString() {
        return "TRANSIENT : " + oid + " : " + memento;
    }

    public Version getVersion() {
        return null;
    }

    public void checkVersion(final ObjectAdapter object) {}

    public void restoreToLoader() {
        memento.recreateObject();
    }

    public void updateVersion() {}


    
    //////////////////////////////////////////////////////////////
    // Dependencies (from context)
    //////////////////////////////////////////////////////////////
    
    
    private AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }


}

