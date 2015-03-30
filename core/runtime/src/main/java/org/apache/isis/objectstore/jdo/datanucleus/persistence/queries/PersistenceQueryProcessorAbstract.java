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
package org.apache.isis.objectstore.jdo.datanucleus.persistence.queries;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.metadata.TypeMetadata;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusObjectStore;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.FrameworkSynchronizer;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.FrameworkSynchronizer.CalledFrom;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.IsisLifecycleListener;
import org.datanucleus.enhancer.Persistable;

public abstract class PersistenceQueryProcessorAbstract<T extends PersistenceQuery>
        implements PersistenceQueryProcessor<T> {

    private final PersistenceManager persistenceManager;
    private final FrameworkSynchronizer frameworkSynchronizer;

    protected PersistenceQueryProcessorAbstract(final PersistenceManager persistenceManager, final FrameworkSynchronizer frameworkSynchronizer) {
        this.persistenceManager = persistenceManager;
        this.frameworkSynchronizer = frameworkSynchronizer;
    }

    protected PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }
    
    
    // /////////////////////////////////////////////////////////////
    // helpers for subclasses
    // /////////////////////////////////////////////////////////////

    protected PersistenceManagerFactory getPersistenceManagerFactory() {
        return getPersistenceManager().getPersistenceManagerFactory();
    }
    
    protected TypeMetadata getTypeMetadata(final String classFullName) {
        return getPersistenceManagerFactory().getMetadata(classFullName);
    }
    
    /**
     * Traversing the provided list causes (or should cause) the
     * {@link IsisLifecycleListener#postLoad(InstanceLifecycleEvent) {
     * to be called.
     */
    protected List<ObjectAdapter> loadAdapters(
            final ObjectSpecification specification, final List<?> pojos) {
        final List<ObjectAdapter> adapters = Lists.newArrayList();
        for (final Object pojo : pojos) {
        	// ought not to be necessary, however for some queries it seems that the 
        	// lifecycle listener is not called
            ObjectAdapter adapter;
            if(pojo instanceof Persistable) {
                // an entity
                frameworkSynchronizer.postLoadProcessingFor((Persistable) pojo, CalledFrom.OS_QUERY);
                adapter = getAdapterManager().getAdapterFor(pojo);
            } else {
                // a value type
                adapter = getAdapterManager().adapterFor(pojo);
            }
            Assert.assertNotNull(adapter);
            adapters.add(adapter);
        }
        return adapters;
    }

    // /////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////////////

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession().getAdapterManager();
    }

    protected DataNucleusObjectStore getJdoObjectStore() {
        return (DataNucleusObjectStore) IsisContext.getPersistenceSession().getObjectStore();
    }

}
