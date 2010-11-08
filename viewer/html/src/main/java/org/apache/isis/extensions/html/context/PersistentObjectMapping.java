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
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;


public class PersistentObjectMapping implements ObjectMapping {
    private final Oid oid;
    private final ObjectSpecification specification;
    private Version version;

    public PersistentObjectMapping(final ObjectAdapter adapter) {
        oid = adapter.getOid();
        Assert.assertFalse("OID is for transient", oid.isTransient());
        Assert.assertFalse("adapter is for transient", adapter.isTransient());
        specification = adapter.getSpecification();
        version = adapter.getVersion();
    }

    public void debug(final DebugString debug) {
        debug.appendln(specification.getFullName());
        if (version != null) {
            debug.appendln(version.toString());
        }
    }

    public Oid getOid() {
        return oid;
    }

    public ObjectAdapter getObject() {
        return getPersistenceSession().loadObject(oid, specification);
    }

    @Override
    public int hashCode() {
        return oid.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj.getClass() == PersistentObjectMapping.class) {
            return ((PersistentObjectMapping) obj).oid.equals(oid);
        }
        return false;
    }

    @Override
    public String toString() {
        return (specification == null ? "null" : specification.getSingularName()) + " : " + oid + " : " + version;
    }

    public Version getVersion() {
        return version;
    }

    public void checkVersion(final ObjectAdapter object) {
        object.checkLock(getVersion());
    }

    public void updateVersion() {
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
        version = adapter.getVersion();
    }

    public void restoreToLoader() {
        final Oid oid = getOid();
        final ObjectAdapter adapter = getPersistenceSession().recreateAdapter(oid, specification);
        adapter.setOptimisticLock(getVersion());
    }
    
    
    
    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////
    
    private static AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }


}

