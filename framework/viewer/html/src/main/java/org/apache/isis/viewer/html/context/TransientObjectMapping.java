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

package org.apache.isis.viewer.html.context;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.runtimes.dflt.runtime.memento.Memento;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

public class TransientObjectMapping implements ObjectMapping {
    
    private static final long serialVersionUID = 1L;
    
    private final Oid oid;
    private final Memento memento;

    public TransientObjectMapping(final ObjectAdapter adapter) {
        oid = adapter.getOid();
        Assert.assertTrue("OID is for persistent", oid.isTransient());
        Assert.assertTrue("adapter is for persistent", adapter.isTransient());
        memento = new Memento(adapter);
    }

    @Override
    public void debug(final DebugBuilder debug) {
        memento.debug(debug);
    }

    @Override
    public Oid getOid() {
        return oid;
    }

    @Override
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

    @Override
    public Version getVersion() {
        return null;
    }

    @Override
    public void checkVersion(final ObjectAdapter object) {
    }

    @Override
    public void restoreToLoader() {
        memento.recreateObject();
    }

    @Override
    public void updateVersion() {
    }

    // ////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ////////////////////////////////////////////////////////////

    private AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}
