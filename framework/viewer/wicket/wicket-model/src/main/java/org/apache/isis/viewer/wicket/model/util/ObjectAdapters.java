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

package org.apache.isis.viewer.wicket.model.util;

import com.google.common.base.Function;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

public final class ObjectAdapters {

    private ObjectAdapters() {
    }

    public static Function<Object, ObjectAdapter> fromPojo() {
        return new Function<Object, ObjectAdapter>() {
            @Override
            public ObjectAdapter apply(final Object pojo) {
                return getAdapterManager().getAdapterFor(pojo);
            }
        };
    }

    public static Function<ObjectAdapterMemento, ObjectAdapter> fromMemento() {
        return new Function<ObjectAdapterMemento, ObjectAdapter>() {
            @Override
            public ObjectAdapter apply(final ObjectAdapterMemento from) {
                return from.getObjectAdapter(ConcurrencyChecking.NO_CHECK);
            }
        };
    }

    public static Function<Oid, ObjectAdapter> fromOid() {
        return new Function<Oid, ObjectAdapter>() {
            @Override
            public ObjectAdapter apply(final Oid from) {
                final ObjectAdapter adapterFor = getAdapterManager().getAdapterFor(from);
                return adapterFor;
            }
        };
    }

    private static AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }
}
