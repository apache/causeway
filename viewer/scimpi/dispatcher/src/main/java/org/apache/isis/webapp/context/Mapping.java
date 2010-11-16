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


package org.apache.isis.webapp.context;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.runtime.memento.Memento;

interface Mapping {
    ObjectAdapter getObject();

    Oid getOid();

    String debug();

    void reload();
    
    void update();
}

class TransientObjectMapping implements Mapping {
    private Oid oid;
    private Memento memento;

    public TransientObjectMapping(ObjectAdapter adapter) {
        oid = adapter.getOid();
        Assert.assertTrue("OID is for persistent", oid.isTransient());
        Assert.assertTrue("adapter is for persistent", adapter.isTransient());
        memento = new Memento(adapter);
    }

    public ObjectAdapter getObject() {
        return IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(oid);
    }

    public Oid getOid() {
        return oid;
    }

    public String debug() {
        DebugString debug = new DebugString();
        memento.debug(debug);
        return debug.toString();
    }

    public void reload() {
        memento.recreateObject();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof TransientObjectMapping) {
            return ((TransientObjectMapping) obj).oid.equals(oid);
        }
        return false;
    }

    public int hashCode() {
        return oid.hashCode();
    }

    public void update() {
        memento = new Memento((ObjectAdapter) getObject());
    }
}

class PersistentObjectMapping implements Mapping {
    private Oid oid;
    private ObjectSpecification spec;

    public PersistentObjectMapping(ObjectAdapter object) {
        this.oid = object.getOid();
        this.spec = object.getSpecification();
    }

    public Oid getOid() {
        return oid;
    }

    public String debug() {
        return oid + "  " + spec.getShortName() + "  " + IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(oid);
    }

    public ObjectAdapter getObject() {
    	if (!IsisContext.inTransaction()) {
    		throw new IllegalStateException(getClass().getSimpleName() + " requires transaction in order to load");
    	}
        return IsisContext.getPersistenceSession().loadObject(oid, spec);
    }

    public void reload() {
        if (IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(oid) == null) {
            IsisContext.getPersistenceSession().recreateAdapter(oid, spec);
        }
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof PersistentObjectMapping) {
            PersistentObjectMapping other = (PersistentObjectMapping) obj;
            return oid.equals(other.oid) && spec == other.spec;
        }

        return false;
    }

    public int hashCode() {
        int hash = 37;
        hash = hash * 17 + oid.hashCode();
        hash = hash * 17 + spec.hashCode();
        return hash;
    }

    public void update() {}

}

