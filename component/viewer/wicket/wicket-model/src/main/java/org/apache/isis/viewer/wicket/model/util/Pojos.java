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
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

public final class Pojos {

    private Pojos() {
    }

    public static Function<Object, Oid> toOid() {
        return new Function<Object, Oid>() {
            @Override
            public Oid apply(final Object pojo) {
                final ObjectAdapter adapter = getAdapterManager().adapterFor(pojo);
                return adapter.getOid();
            }

        };
    }

    public static Function<ObjectAdapter, Object> forAdapter() {
        return new Function<ObjectAdapter, Object>() {

            @Override
            public Object apply(final ObjectAdapter from) {
                return from.getObject();
            }
        };
    }

    private static AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession().getAdapterManager();
    }

}
