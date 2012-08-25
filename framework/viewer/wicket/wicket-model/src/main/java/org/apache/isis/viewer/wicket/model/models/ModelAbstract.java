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

package org.apache.isis.viewer.wicket.model.models;

import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.Persistor;

/**
 * Adapter for {@link LoadableDetachableModel}s, providing access to some of the
 * Isis' dependencies.
 */
public abstract class ModelAbstract<T> extends LoadableDetachableModel<T> {

    private static final long serialVersionUID = 1L;

    public ModelAbstract() {
    }

    public ModelAbstract(final T t) {
        super(t);
    }

    // //////////////////////////////////////////////////////////////
    // Dependencies
    // //////////////////////////////////////////////////////////////

    protected Persistor getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected List<ObjectAdapter> getServiceAdapters() {
        return IsisContext.getPersistenceSession().getServices();
    }

    protected AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession().getAdapterManager();
    }

}