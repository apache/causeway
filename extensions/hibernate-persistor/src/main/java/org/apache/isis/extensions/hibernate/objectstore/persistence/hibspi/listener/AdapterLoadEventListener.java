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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.listener;

import java.io.Serializable;
import java.lang.reflect.Modifier;

import org.apache.log4j.Logger;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.engine.EntityKey;
import org.hibernate.engine.PersistenceContext;
import org.hibernate.event.LoadEvent;
import org.hibernate.event.LoadEventListener;
import org.hibernate.event.def.DefaultLoadEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.apache.isis.commons.ensure.Assert;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.extensions.hibernate.objectstore.persistence.oidgenerator.HibernateOid;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.PersistenceSessionHydrator;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;


/**
 * Implementation of {@link LoadEventListener} that mostly has the same behaviour as
 * {@link DefaultLoadEventListener}, but that which uses [[NAME]]' ghost objects
 * for lazy loading rather than Hibernate's proxies.
 */
public class AdapterLoadEventListener extends DefaultLoadEventListener {
    
    private static final long serialVersionUID = 1L;
    private final static Logger LOG = Logger.getLogger(AdapterLoadEventListener.class);

    

    /**
     * Just adds some debugging, is all.
     */
    @Override
    public void onLoad(final LoadEvent event, final LoadType loadType) throws HibernateException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "LoadEvent - pre onLoad type=" + event.getEntityClassName() + ", " +
                "id=" + event.getEntityId() + ", " +
                "result=" + (event.getResult() == null ? "null" : event.getResult().getClass().getName()) + ", " +
                "instancetoload=" + event.getInstanceToLoad()
            );
        }

        super.onLoad(event, loadType);
    }

    

    /**
     * Similar to the default implementation, but uses [[NAME]]' 'ghost' objects
     * rather than Hibernate's proxies.
     */
    @Override
    protected Object proxyOrLoad(
            final LoadEvent event,
            final EntityPersister persister,
            final EntityKey keyToLoad,
            final LoadEventListener.LoadType options) throws HibernateException {

        if ( LOG.isDebugEnabled() ) {
            LOG.debug(
                    "loading entity: " + 
                    MessageHelper.infoString( persister, event.getEntityId(), event.getSession().getFactory() )
                );
        }

        // as per superclass' implementation:
        // if this class has no proxies, then do a shortcut
        if (!persister.hasProxy()) {
            return load(event, persister, keyToLoad, options);
        }
        
        final PersistenceContext persistenceContext = event.getSession().getPersistenceContext();

        // using [[NAME]] ghost objects, so should never be an existing proxy
        Assert.assertNull(persistenceContext.getProxy(keyToLoad));
        
        if (options.isAllowProxyCreation()) { // original behaviour
            
            // replaced behaviour here; 
            final Class<?> cls = persister.getMappedClass(EntityMode.POJO);
            if (classIsInstantiable(cls)) {
                // [[NAME]] lazy loading involves creating a 'ghost' so 
                // we must be able to instantiate the class
                return loadUnresolvedObject(event, persister, keyToLoad, options, persistenceContext);
            }
        }
        
        // return a newly loaded object
        return load(event, persister, keyToLoad, options);
    }

    
    //////////////////////////////////////////////////////////////////
    // Helpers
    //////////////////////////////////////////////////////////////////

    private boolean classIsInstantiable(final Class<?> cls) {
        int clsModifiers = cls.getModifiers();
        return !Modifier.isAbstract(clsModifiers) && 
               !Modifier.isInterface(cls.getModifiers());
    }

    private Object loadUnresolvedObject(
            final LoadEvent event,
            final EntityPersister persister,
            final EntityKey keyToLoad,
            final LoadEventListener.LoadType options,
            final PersistenceContext persistenceContext) {
        
        Serializable entityId = event.getEntityId();
        final HibernateOid oid = HibernateOid.createPersistent(event.getEntityClassName(), entityId);
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(event.getEntityClassName());
        
        ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
        if (adapter == null) {
            adapter = getHydrator().recreateAdapter(oid, spec);
            Assert.assertFalse(persistenceContext.isEntryFor(adapter.getObject()));
            return adapter.getObject();
        } else {
            return load(event, persister, keyToLoad, options);
        }
    }






    //////////////////////////////////////////////////////////////////
    // Dependencies (from singletons)
    //////////////////////////////////////////////////////////////////

    protected SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private PersistenceSessionHydrator getHydrator() {
        return getPersistenceSession();
    }

    private AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }


}
