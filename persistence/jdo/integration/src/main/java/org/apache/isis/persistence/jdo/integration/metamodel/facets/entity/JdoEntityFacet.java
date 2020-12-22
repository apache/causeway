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
package org.apache.isis.persistence.jdo.integration.metamodel.facets.entity;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.datanucleus.enhancement.Persistable;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.iactn.InteractionTracker;
import org.apache.isis.persistence.jdo.integration.metamodel.JdoMetamodelUtil;

import lombok.val;

public class JdoEntityFacet extends JdoEntityFacetAbstract {

    public JdoEntityFacet(
            final FacetHolder holder, 
            final Supplier<InteractionTracker> isisInteractionTracker) {
        super(holder, isisInteractionTracker);
    }

    @Override
    public ManagedObject fetchByIdentifier(ObjectSpecification spec, String identifier) {
        
        if(!spec.isEntity()) {
            throw _Exceptions.unexpectedCodeReach();
        }
        
        val persistenceSession = super.getPersistenceSessionJdo();
        val rootOid = Oid.Factory.root(spec.getSpecId(), identifier);
        val pojo = persistenceSession.fetchPersistentPojo(rootOid);
        
        return ManagedObject.identified(spec, pojo, rootOid);
    }
    
    @Override
    public Can<ManagedObject> fetchByQuery(ObjectSpecification spec, Query<?> query) {
        if(!spec.isEntity()) {
            throw _Exceptions.unexpectedCodeReach();
        }
        val persistenceSession = super.getPersistenceSessionJdo();
        return persistenceSession.allMatchingQuery(query);
    }
    
    @Override
    public String identifierFor(ObjectSpecification spec, Object pojo) {

        if(pojo==null) {
            throw _Exceptions.illegalArgument(
                    "The persistence layer cannot identify a pojo that is null (given type %s)",
                    spec.getCorrespondingClass().getName());
        }
        
        if(!isPersistableType(pojo.getClass())) {
            throw _Exceptions.illegalArgument(
                    "The persistence layer does not recognize given type %s",
                    pojo.getClass().getName());
        }
        
        val persistenceSession = super.getPersistenceSessionJdo();
        val isRecognized = persistenceSession.isRecognized(pojo);
        if(!isRecognized) {
            throw _Exceptions.illegalArgument(
                    "The persistence layer does not recognize given object of type %s, "
                    + "meaning the object has no identifier that associates it with the persistence layer. "
                    + "(most likely, because the object is detached, eg. was not persisted after being new-ed up)", 
                    pojo.getClass().getName());
        }
        
        final String identifier = persistenceSession.identifierFor(pojo);
        return identifier;
    }

    @Override
    public void persist(ObjectSpecification spec, Object pojo) {

        if(pojo==null || !isPersistableType(pojo.getClass())) {
            return; //noop
        }
        
        val persistenceSession = super.getPersistenceSessionJdo();
        persistenceSession.makePersistentInTransaction(ManagedObject.of(spec, pojo));
    }
    
    @Override
    public void delete(ObjectSpecification spec, Object pojo) {
        val persistenceSession = super.getPersistenceSessionJdo();
        persistenceSession.destroyObjectInTransaction(ManagedObject.of(spec, pojo));
    }
    
    @Override
    public void refresh(Object pojo) {
        val persistenceSession = super.getPersistenceSessionJdo();
        persistenceSession.refreshRoot(pojo);
    }
    
    @Override
    public EntityState getEntityState(Object pojo) {
        val persistenceSession = super.getPersistenceSessionJdo();
        return persistenceSession.getEntityState(pojo);
    }

    @Override
    public <T> T detach(T pojo) {
        return super.getPersistenceSessionJdo().getJdoPersistenceManager().detachCopy(pojo);
    }

    // -- HELPER
    
    private static boolean isPersistableType(Class<?> type) {
        return Persistable.class.isAssignableFrom(type);
    }

    @Override
    public boolean isProxyEnhancement(Method method) {
        return JdoMetamodelUtil.isMethodProvidedByEnhancement(method);
    }


}
