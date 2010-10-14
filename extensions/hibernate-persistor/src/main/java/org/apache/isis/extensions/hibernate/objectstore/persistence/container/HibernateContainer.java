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


package org.apache.isis.extensions.hibernate.objectstore.persistence.container;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.extensions.hibernate.objectstore.util.HibernateUtil;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.transaction.IsisTransactionManager;


public class HibernateContainer extends DomainObjectContainerDefault {
	
	private static final Logger LOG = Logger.getLogger(HibernateContainer.class);

    @Override
    public void resolve(final Object parent, final Object field) {
        // normal resolve for null field
        if (field == null) {
            super.resolve(parent);
            return;
        }

        final ObjectAdapter adapter = getRuntimeContext().getAdapterFor(parent);
        final ResolveState resolveState = adapter.getResolveState();
        if (!resolveState.isResolvable(ResolveState.RESOLVING)) {
            return;
        }

        // This code is here to handle hibernate lazy loading. The field reference may be not null because
        // it is a proxy - this will check if it is a uninitialized proxy.

        // TODO would really like to push this into objectPersistor, but current setup
        // requires a ObjectAdapter field, which we can't get!

        // TODO loadEvent will create adapter for proxy, but collection is a different matter.
        // if adapter exists for collection it will be set to resolved, but can't create new adapter
        // as we can't get the field
        if (Hibernate.isInitialized(field)) {
            return;
        }

        // This method may not be running within the scope of a transaction, so
        // make sure one is active
        getTransactionManager().startTransaction();
        try {
            final Session session = HibernateUtil.getCurrentSession();
            if (field instanceof org.hibernate.collection.PersistentCollection) {
                session.lock(parent, org.hibernate.LockMode.NONE);
                Hibernate.initialize(field);
            } else {
                final LazyInitializer lazy = ((HibernateProxy) field).getHibernateLazyInitializer();
                lazy.setSession((SessionImplementor) session);
                lazy.initialize();
            }
        } catch (final RuntimeException e) {
        	getTransactionManager().abortTransaction();
            throw e;
        }
        getTransactionManager().endTransaction();
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "Container resolved field of type " + field.getClass() + 
                    " for parent " + parent + ", state=" + resolveState);
        }
    }

    
    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////

    protected static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected static IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }
    

}
