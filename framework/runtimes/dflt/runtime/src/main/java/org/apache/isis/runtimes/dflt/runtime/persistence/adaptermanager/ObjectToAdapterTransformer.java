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

package org.apache.isis.runtimes.dflt.runtime.persistence.adaptermanager;

import org.apache.commons.collections.Transformer;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManagerSpi;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

/**
 * Uses the Commons Collection API to transform {@link Object}s into
 * {@link ObjectAdapter} adapters.
 * 
 */
public final class ObjectToAdapterTransformer implements Transformer {

    public ObjectToAdapterTransformer() {
    }

    @Override
    public Object transform(final Object object) {
        return getAdapterManager().adapterFor(object);
    }

    // //////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////////////

    private static AdapterManagerSpi getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}