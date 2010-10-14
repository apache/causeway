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


package org.apache.isis.extensions.hibernate.objectstore;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.commons.ensure.Assert;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.extensions.hibernate.objectstore.metamodel.criteria.HibernateInstancesCriteria;
import org.apache.isis.extensions.hibernate.objectstore.persistence.oidgenerator.HibernateOid;
import org.apache.isis.extensions.hibernate.objectstore.util.HibernateUtil;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.PersistenceSessionHydrator;
import org.apache.isis.runtime.persistence.PersistenceSessionHydratorAware;
import org.apache.isis.runtime.persistence.PersistorUtil;
import org.apache.isis.runtime.persistence.UnsupportedFindException;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtime.persistence.query.PersistenceQuery;
import org.apache.isis.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.runtime.persistence.query.PersistenceQueryFindByPattern;
import org.apache.isis.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.runtime.transaction.IsisTransaction;
import org.apache.isis.runtime.transaction.ObjectPersistenceException;
import org.apache.isis.runtime.transaction.PersistenceCommand;


public class HibernateObjectStore implements ObjectStore, PersistenceSessionHydratorAware {
    private static final Logger LOG = Logger.getLogger(HibernateObjectStore.class);

    private static boolean isInitialized = false;

    private PersistenceSessionHydrator hydrator;

    ///////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////

    public HibernateObjectStore() {}


    ///////////////////////////////////////////////////////////
    // name
    ///////////////////////////////////////////////////////////

    public String name() {
        return "Hibernate Object Store";
    }


    ///////////////////////////////////////////////////////////
    // init, shutdown, reset, isInitialized
    ///////////////////////////////////////////////////////////


    public void open() {
        // will only return true on first execution
        isInitialized = HibernateUtil.init();
    }

    
    /**
     * TODO: commit transaction (?) and close underlying Hibernate session.
     */
    public void close() {}
    
    

    public boolean isFixturesInstalled() {
        return isInitialized;
    }

    public void reset() {
        LOG.debug("resetting object store");
        // HibernateUtil.rebuildSessionFactory();
    }
    
    ///////////////////////////////////////////////////////////
    // Transaction
    ///////////////////////////////////////////////////////////

    public void startTransaction() {
        LOG.debug("Start TX");
        // do nothing - control hibernate transactions locally
    }

    public void abortTransaction() {
        LOG.debug("Abort TX");
        HibernateUtil.rollbackTransaction();
    }

    public void endTransaction() {
        LOG.debug("Committing TX");
        // do nothing - control hibernate transactions locally
    }


    ///////////////////////////////////////////////////////////
    // Hibernate Transaction (protected so can be overridden)
    ///////////////////////////////////////////////////////////

    /**
     * Is protected so can be overridden.
     * @return
     */
    protected boolean startHibernateTransaction() {
        Assert.assertFalse(HibernateUtil.inTransaction());
        HibernateUtil.startTransaction();
        return true;
    }

    protected void commitHibernateTransaction(final boolean started) {
        if (started) {
            Assert.assertTrue(HibernateUtil.inTransaction());
            HibernateUtil.commitTransaction();
        }
    }

    ///////////////////////////////////////////////////////////
    // createXxxCommand
    ///////////////////////////////////////////////////////////

    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        LOG.debug("  create object " + object);
        return new CreateObjectCommand() {
            public void execute(final IsisTransaction context) {
                ensureInHibernateTransaction();
                LOG.debug("  create object - actual save " + object);
                HibernateUtil.getCurrentSession().save(object.getObject());
            }

            public ObjectAdapter onObject() {
                return object;
            }

            @Override
            public String toString() {
                return "CreateObjectCommand [object=" + object + "]";
            }
        };
    }

    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter object) {
        LOG.debug("  destroy object " + object);
        return new DestroyObjectCommand() {
            public void execute(final IsisTransaction context) {
                ensureInHibernateTransaction();
                LOG.debug("  destroy object - actual delete " + object);
                HibernateUtil.getCurrentSession().delete(object.getObject());
            }

            public ObjectAdapter onObject() {
                return object;
            }

            @Override
            public String toString() {
                return "DestroyObjectCommand [object=" + object + "]";
            }
        };
    }

    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter object) {
        LOG.debug("  save object " + object);
        return new SaveObjectCommand() {
            public void execute(final IsisTransaction context) {
                ensureInHibernateTransaction();
                LOG.debug("  save object - actual update " + object);
                Object toUpdate;
                toUpdate = object.getObject();
                HibernateUtil.getCurrentSession().update(toUpdate);
            }

            public ObjectAdapter onObject() {
                return object;
            }

            @Override
            public String toString() {
                return "SaveObjectCommand [object=" + object + "]";
            }
        };
    }

    ///////////////////////////////////////////////////////////
    // execute
    ///////////////////////////////////////////////////////////

    public void execute(final List<PersistenceCommand> commands) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("execute " + commands.size() + " commands");
        }
        if (commands.size() <= 0) {
            return;
        }
        try {
            final boolean started = startHibernateTransaction();
            executeCommands(commands);
            commitHibernateTransaction(started);
        } catch (final RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Protected so can be overridden
     */
    protected void executeCommands(final List<PersistenceCommand> commands) {
        try {
            for (final PersistenceCommand command: commands) {
                command.execute(null);
            }
        } catch (final RuntimeException e) {
            LOG.warn("Failure during execution", e);
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }


    ///////////////////////////////////////////////////////////
    // resolve, getObject
    ///////////////////////////////////////////////////////////

    public ObjectAdapter getObject(final Oid oid, final ObjectSpecification hint) throws ObjectNotFoundException,
    ObjectPersistenceException {

        LOG.debug("getObject id=" + oid);
        Object result = null;
        final HibernateOid hoid = (HibernateOid) oid;
        try {
            final boolean started = startHibernateTransaction();
            result = HibernateUtil.getCurrentSession().get(hoid.getClassName(), hoid.getPrimaryKey());
            commitHibernateTransaction(started);
        } catch (final RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
        
        if (result == null) {
            throw new ObjectNotFoundException(oid);
        }
        return hydrator.recreateAdapter(hoid, result);
    }
        
    public void resolveImmediately(final ObjectAdapter object) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resolveImmediately id=" + object.getOid());
            LOG.debug("loadObject id=" + object.getOid());
        }
        final HibernateOid hoid = (HibernateOid) object.getOid();
        try {
            final boolean started = startHibernateTransaction();
            HibernateUtil.getCurrentSession().load(hoid.getClassName(), hoid.getPrimaryKey());
            commitHibernateTransaction(started);
        } catch (final RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
        
        if (object.getObject() == null) {
            throw new ObjectNotFoundException(object.getOid());
        }
    }


    public void resolveField(final ObjectAdapter object, final ObjectAssociation field) {
        LOG.debug("resolveField id=" + object.getOid() + ", field=" + field.getId());
        if (field.isOneToOneAssociation()) {
            final ObjectAdapter adapter = field.get(object);
            initializeField(object.getObject(), adapter.getObject());
        }
        if (field.isOneToManyAssociation()) {
            final ObjectAdapter collectionAdapter = field.get(object);
            // can sometimes be called if collection is already resolved! e.g.
            // BasicPerpsective.services
            if (!collectionAdapter.getResolveState().isResolved()) {
                PersistorUtil.start(collectionAdapter, ResolveState.RESOLVING);
                final Object collection = collectionAdapter.getObject();
                initializeField(object.getObject(), collection);
                PersistorUtil.end(collectionAdapter);
            }
        }
    }

    private void initializeField(final Object parent, final Object field) {
        if (!Hibernate.isInitialized(field)) {
            
            // TODO push this functionality up into
            // OneTo(One|Many)Association.get...() which should
            // wrap a transaction around all method calls which could hit the
            // db.

            try {
                // This method may not be running within the scope of a transaction,
                // so make sure one is active
                final boolean started = startHibernateTransaction();
                if (field instanceof PersistentCollection) {
                    HibernateUtil.getCurrentSession().lock(parent, org.hibernate.LockMode.NONE);
                    Hibernate.initialize(field);
                } else {
                    final LazyInitializer lazy = ((HibernateProxy) field).getHibernateLazyInitializer();
                    lazy.setSession((SessionImplementor) HibernateUtil.getCurrentSession());
                    lazy.initialize();
                }
                commitHibernateTransaction(started);
                LOG.debug("initializeField of type " + field.getClass() + " for parent " + parent);
            } catch (final RuntimeException e) {
                HibernateUtil.rollbackTransaction();
                throw e;
            }

        }
    }

    ///////////////////////////////////////////////////////////
    // getInstances, hasInstances
    ///////////////////////////////////////////////////////////

    public ObjectAdapter[] getInstances(final PersistenceQuery persistenceQuery) {
        if (persistenceQuery instanceof PersistenceQueryFindByTitle) {
            return getInstancesByTitleCriteria((PersistenceQueryFindByTitle) persistenceQuery);
        } else if (persistenceQuery instanceof PersistenceQueryFindByPattern) {
            return getInstancesByPatternCriteria((PersistenceQueryFindByPattern) persistenceQuery);
        } else if (persistenceQuery instanceof HibernateInstancesCriteria) {
            return getInstancesByHibernateCriteria((HibernateInstancesCriteria) persistenceQuery);
        } else if (persistenceQuery instanceof PersistenceQueryFindAllInstances) {
            return getInstances(persistenceQuery.getSpecification());
        } else {
            throw new UnsupportedFindException("Can't use criteria for search: " + persistenceQuery);
        }
    }

    public boolean hasInstances(final ObjectSpecification specification) {
        if (!specification.persistability().isPersistable()) {
            LOG.warn("trying to run hasInstances for non-persistent class " + specification);
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("hasInstances - class=" + specification.getFullName());
        }

        try {
            final boolean started = startHibernateTransaction();
            final Query query = createQuery("select o.id", specification);
            query.setMaxResults(1);
            boolean result = !query.list().isEmpty();
            commitHibernateTransaction(started);
            return result;
        } catch (final RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    ///////////////////////////////////////////////////////////
    // getInstancesBy (hibernate specific, not part of API)
    ///////////////////////////////////////////////////////////

    public ObjectAdapter[] getInstancesByTitleCriteria(final PersistenceQueryFindByTitle criteria) {
        try {
            final boolean started = startHibernateTransaction();
            final String classFullName = criteria.getSpecification().getFullName();
            final Criteria hibernateCriteria = HibernateUtil.getCurrentSession().createCriteria(classFullName).add(
                    Restrictions.ilike("title", criteria.getTitle(), MatchMode.ANYWHERE));
            final List<?> results = hibernateCriteria.list();
            final ObjectAdapter[] loadedObjects = loadObjects(HibernateUtil.getCurrentSession(), criteria.getSpecification(),
                    results);
            commitHibernateTransaction(started);
            return loadedObjects;
        } catch (final RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    public ObjectAdapter[] getInstances(final ObjectSpecification specification) {

        LOG.debug("getInstances - class=" + specification.getFullName());
        try {
            final boolean started = startHibernateTransaction();
            final Query query = createQuery(null, specification);
            ObjectAdapter[] objects = loadObjects(HibernateUtil.getCurrentSession(), specification, query.list());
            commitHibernateTransaction(started);
            return objects;
        } catch (final RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    public ObjectAdapter[] getInstancesByHibernateCriteria(final HibernateInstancesCriteria criteria) {
        try {
            final boolean started = startHibernateTransaction();
            criteria.setSession(HibernateUtil.getCurrentSession());
            final List<?> results = criteria.getResults();
            final ObjectAdapter[] objects = loadObjects(HibernateUtil.getCurrentSession(), criteria.getSpecification(),
                    results);
            commitHibernateTransaction(started);
            return objects;
        } catch (final RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    public ObjectAdapter[] getInstancesByPatternCriteria(final PersistenceQueryFindByPattern criteria) {
        try {
            final boolean started = startHibernateTransaction();
            final Object pojoPattern = criteria.getPattern().getObject();
            // ignore title when pattern matching as it cannot be null (filled in by adapter)
            final Criteria hibernateCriteria = HibernateUtil.getCurrentSession().createCriteria(pojoPattern.getClass()).add(
                    Example.create(pojoPattern).excludeProperty("title"));
            final List<?> results = hibernateCriteria.list();
            final ObjectAdapter[] objects = loadObjects(HibernateUtil.getCurrentSession(), criteria.getSpecification(),
                    results);
            commitHibernateTransaction(started);
            return objects;
        } catch (final RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }


    ///////////////////////////////////////////////////////////
    // createQuery (hibernate specific)
    ///////////////////////////////////////////////////////////

    protected Query createQuery(final String select, final ObjectSpecification specification) {
        ensureInHibernateTransaction();
        final StringBuffer querySb = new StringBuffer(128);
        if (select != null) {
            querySb.append(select).append(" ");
        }
        querySb.append("from ").append(specification.getFullName()).append(" as o");
        final String queryString = querySb.toString();
        LOG.debug("Query - " + queryString);
        return HibernateUtil.getCurrentSession().createQuery(queryString);
    }


    ///////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////

    private ObjectAdapter[] loadObjects(final Session session, final ObjectSpecification specification, final List<?> list) {
        final ObjectAdapter[] objects = new ObjectAdapter[list.size()];
        int i = 0;
        for (final Object object : list) {
            // REVIEW: cannot just load adapter for object - if [[NAME]] has
            // already loaded the object
            // then object won't match it (e.g. if getInstances has been called
            // and an instance has
            // been loaded) - so need to use Hibernate session to get an Oid to
            // do a lookup in that case
            objects[i++] = getAdapterManager().getAdapterFor(object);
        }
        return objects;
    }


    private void ensureInHibernateTransaction() {
        Assert.assertTrue(HibernateUtil.inTransaction());
    }


    ///////////////////////////////////////////////////////////
    // Services
    ///////////////////////////////////////////////////////////

    public Oid getOidForService(final String name) {
        return HibernateOid.createPersistent(name, name, name);
    }

    public void registerService(final String service, final Oid oid) {}


    ///////////////////////////////////////////////////////////
    // debug
    ///////////////////////////////////////////////////////////

    public void debugData(final DebugString debug) {}

    public String debugTitle() {
        return null;
    }


    

    ///////////////////////////////////////////////////////////
    // Dependencies (injected)
    ///////////////////////////////////////////////////////////
    
    public void setHydrator(PersistenceSessionHydrator hydrator) {
        this.hydrator = hydrator;
    }

    
    ///////////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////////

    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }





}
