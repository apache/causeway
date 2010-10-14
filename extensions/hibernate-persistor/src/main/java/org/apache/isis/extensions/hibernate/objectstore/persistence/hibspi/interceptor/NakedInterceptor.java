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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.interceptor;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.PropertyHelper;
import org.apache.isis.extensions.hibernate.objectstore.persistence.oidgenerator.HibernateOid;
import org.apache.isis.extensions.hibernate.objectstore.util.HibernateUtil;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.PersistorUtil;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;


/**
 * A Hibernate Interceptor to ensure process [[NAME]] correctly.
 * 
 * <p>
 * It does several things including:
 * <ul>
 * <li>Objects cached within NOF are used instead of loading from the database</li>
 * <li>Objects are created via NOF, so container and services are injected</li>
 * <li><tt>adapter_modified_by</tt> and <tt>adapter_modified_on</tt> properties are updated when an object is saved or updated</li>
 * </ul>
 */
public class AdapterInterceptor extends EmptyInterceptor {
    
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(AdapterInterceptor.class);


    //////////////////////////////////////////////////////////////////
    // isTransient
    //////////////////////////////////////////////////////////////////

    @Override
    public Boolean isTransient(final Object entity) {
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(entity);
        return adapter.getOid().isTransient();
    }


    //////////////////////////////////////////////////////////////////
    // Instantiate
    //////////////////////////////////////////////////////////////////

    @Override
    public Object instantiate(final String entityName, final EntityMode entityMode, final Serializable id)
            throws CallbackException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("instantiate entityName=" + entityName + ", id=" + id + ", mode=" + entityMode);
        }
        
        final HibernateOid oid = HibernateOid.createPersistent(entityName, id);
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(entityName);
        final ObjectAdapter adapter = getPersistenceSession().recreateAdapter(oid, spec);
        final ResolveState nextState = adapter.getResolveState().isResolved() ? ResolveState.UPDATING : ResolveState.RESOLVING;

        // TODO better sort resolve state transitions
        if (!adapter.getResolveState().isResolving()) {
            PersistorUtil.start(adapter, nextState);
        }

        final Object object = adapter.getObject();
        // need to set the id in case the object has an id property 
        // (if not id is held in the oid, and that's taken care of above)
        try {
            ClassMetadata classMetadata = HibernateUtil.getSessionFactory().getClassMetadata(entityName);
            classMetadata.setIdentifier(object, id, entityMode);
        } catch (final HibernateException e) {
            throw new CallbackException("Error getting identifier property for class " + entityName, e);
        }
        return object;
    }


    //////////////////////////////////////////////////////////////////
    // getEntity
    //////////////////////////////////////////////////////////////////
    
    @Override
    public Object getEntity(final String entityName, final Serializable id) throws CallbackException {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEntity entityName=" + entityName + ", id=" + id);
        }
        
        final HibernateOid oid = HibernateOid.createPersistent(entityName, id);
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
        if (adapter == null || adapter.getResolveState().isGhost() || adapter.getResolveState().isPartlyResolved()
                || adapter.getResolveState().isResolving()) {
            // change for lazy loading objects - this prevents hibernate
            // picking up the GHOST object as an already loaded entity by only returning
            // the object if it is resolved
            return null;
        }
        return adapter.getObject();
    }

    
    //////////////////////////////////////////////////////////////////
    // onFlushDirty
    //////////////////////////////////////////////////////////////////

    /**
     * Updates the user name and timestamp, if possible.
     */
    @Override
    public boolean onFlushDirty(
            final Object entity,
            final Serializable id,
            final Object[] currentState,
            final Object[] previousState,
            final String[] propertyNames,
            final Type[] types) {
        return setModified(currentState, propertyNames);
    }

    
    //////////////////////////////////////////////////////////////////
    // onSave
    //////////////////////////////////////////////////////////////////

    /**
     * Updates the user name and timestamp, if possible.
     */
    @Override
    public boolean onSave(
            final Object entity,
            final Serializable id,
            final Object[] state,
            final String[] propertyNames,
            final Type[] types) {
        return setModified(state, propertyNames);
    }

    
    //////////////////////////////////////////////////////////////////
    // Helpers
    //////////////////////////////////////////////////////////////////

    /**
     * Updates the currentState array with the user name and timestamp,
     * if available.
     * 
     * @return <tt>true</tt> if either user name and/or timestamp was updated.
     */
    private boolean setModified(final Object[] currentState, final String[] propertyNames) {
        boolean updatedModifiedBy = false;
        boolean updatedModifiedOn = false;
        for (int i = 0; i < propertyNames.length; i++) {
            if (!updatedModifiedBy && 
                 propertyNames[i].equals(PropertyHelper.MODIFIED_BY)) {
                currentState[i] = getSession().getUserName();
                updatedModifiedBy = true;
            }
            if (!updatedModifiedOn && 
                 propertyNames[i].equals(PropertyHelper.MODIFIED_ON)) {
                currentState[i] = new Date();
                updatedModifiedOn = true;
            }
        }
        return updatedModifiedBy || updatedModifiedOn;
    }

    
    //////////////////////////////////////////////////////////////////
    // Dependencies (from singleton)
    //////////////////////////////////////////////////////////////////

    private SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession().getAdapterManager();
    }

    private AuthenticationSession getSession() {
        return IsisContext.getAuthenticationSession();
    }


}
