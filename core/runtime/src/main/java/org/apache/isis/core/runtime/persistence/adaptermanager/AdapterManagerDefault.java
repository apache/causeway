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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 * Responsible for managing the {@link ObjectAdapter adapter}s and {@link Oid
 * identities} for each and every POJO that is being used by the framework.
 *
 * <p>
 * It provides a consistent set of adapters in memory, providing an
 * {@link ObjectAdapter adapter} for the POJOs that are in use ensuring that the
 * same object is not loaded twice into memory.
 *
 * <p>
 * Each POJO is given an {@link ObjectAdapter adapter} so that the framework can
 * work with the POJOs even though it does not understand their types. Each POJO
 * maps to an {@link ObjectAdapter adapter} and these are reused.
 */
public class AdapterManagerDefault implements AdapterManager,
        SessionScopedComponent {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterManagerDefault.class);

    //region > constructor and fields

    private final PersistenceSession persistenceSession;

    /**
     * For object store implementations (eg JDO) that do not provide any mechanism
     * to allow transient objects to be reattached.
     * 
     * @see <a href="http://www.datanucleus.org/servlet/forum/viewthread_thread,7238_lastpage,yes#35976">this thread</a>
     */
    public AdapterManagerDefault(
            final PersistenceSession persistenceSession) {
        this.persistenceSession = persistenceSession;

    }
    //endregion

    //region > open, close
    public void open() {
    }

    public void close() {
    }
    //endregion

    //region > getAdapterFor
    @Override
    public ObjectAdapter getAdapterFor(final Object pojo) {
        return persistenceSession.getAdapterFor(pojo);
    }

    @Override
    public ObjectAdapter getAdapterFor(final Oid oid) {
        return persistenceSession.getAdapterFor(oid);
    }
    //endregion

    //region > adapterFor

    @Override
    public ObjectAdapter adapterFor(final Object pojo) {
        return persistenceSession.adapterFor(pojo);
    }

    @Override
    public ObjectAdapter adapterFor(
            final Object pojo, final ObjectAdapter parentAdapter, final OneToManyAssociation collection) {
        return persistenceSession.adapterFor(pojo, parentAdapter, collection);
    }

    //endregion


    //region > mapRecreatedPojo

    @Override
    public ObjectAdapter mapRecreatedPojo(final Oid oid, final Object recreatedPojo) {
        return persistenceSession.mapRecreatedPojo(oid, recreatedPojo);
    }

    //endregion

    //region > removeAdapter
    @Override
    public void removeAdapter(final ObjectAdapter adapter) {
        persistenceSession.removeAdapter(adapter);
    }

    //endregion


    //region > Injectable

    @Override
    public void injectInto(final Object candidate) {
        if (AdapterManagerAware.class.isAssignableFrom(candidate.getClass())) {
            final AdapterManagerAware cast = AdapterManagerAware.class.cast(candidate);
            cast.setAdapterManager(this);
        }
    }
    //endregion



}
