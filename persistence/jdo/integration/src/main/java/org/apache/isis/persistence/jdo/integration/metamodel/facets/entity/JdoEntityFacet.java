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

import org.datanucleus.enhancement.Persistable;
import org.datanucleus.store.rdbms.RDBMSPropertyNames;

import org.apache.isis.applib.query.AllInstancesQuery;
import org.apache.isis.applib.query.NamedQuery;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.persistence.jdo.datanucleus.entities.DnEntityStateProvider;
import org.apache.isis.persistence.jdo.integration.metamodel.JdoMetamodelUtil;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JdoEntityFacet extends _JdoEntityFacetAbstract {

    public JdoEntityFacet(
            final FacetHolder holder) {
        super(holder);
    }

    @Override
    public ManagedObject fetchByIdentifier(ObjectSpecification spec, String identifier) {
        
        if(!spec.isEntity()) {
            throw _Exceptions.unexpectedCodeReach();
        }
        return getJdoPersistenceSession().fetchByIdentifier(spec, identifier);
    }
    
    @Override
    public Can<ManagedObject> fetchByQuery(ObjectSpecification spec, Query<?> query) {
        if(!spec.isEntity()) {
            throw _Exceptions.unexpectedCodeReach();
        }
        
        if (log.isDebugEnabled()) {
            log.debug("about to execute Query: {}", query.getDescription());
        }
        
        val range = query.getRange();
        
        if(query instanceof AllInstancesQuery) {

            val queryFindAllInstances = (AllInstancesQuery<?>) query;
            val queryEntityType = queryFindAllInstances.getResultType();
            
            val persistenceManager = getJdoPersistenceSession().getPersistenceManager();
            
            val typedQuery = persistenceManager.newJDOQLTypedQuery(queryEntityType);
            typedQuery.extension(RDBMSPropertyNames.PROPERTY_RDBMS_QUERY_MULTIVALUED_FETCH, "none");
            
            if(!range.isUnconstrained()) {
                typedQuery.range(range.getStart(), range.getEnd());
            }
            
            val resultList = getJdoPersistenceSession()
                   .getTransactionalProcessor()
                   .executeWithinTransaction(typedQuery::executeList);
            
            if(range.hasLimit()) {
                _Assert.assertTrue(resultList.size()<=range.getLimit());
            }
            
            return resultList;
            
        } else if(query instanceof NamedQuery) {
            
            val applibNamedQuery = (NamedQuery<?>) query;
            val queryResultType = applibNamedQuery.getResultType();
            
            val persistenceManager = getJdoPersistenceSession().getPersistenceManager();
            
            val namedParams = _Maps.<String, Object>newHashMap();
            val namedQuery = persistenceManager.newNamedQuery(queryResultType, applibNamedQuery.getName())
                    .setNamedParameters(namedParams);
            namedQuery.extension(RDBMSPropertyNames.PROPERTY_RDBMS_QUERY_MULTIVALUED_FETCH, "none");
            
            if(!range.isUnconstrained()) {
                namedQuery.range(range.getStart(), range.getEnd());
            }
            
            // inject services into query params; not sure if required (might be redundant)
            {
                val injector = getServiceInjector();
                
                applibNamedQuery
                .getParametersByName()
                .values()
                .forEach(injector::injectServicesInto);
            }
            
            applibNamedQuery
                .getParametersByName()
                .forEach(namedParams::put);
            
            val resultList = getJdoPersistenceSession()
                    .getTransactionalProcessor()
                    .executeWithinTransaction(namedQuery::executeList);
            
            if(range.hasLimit()) {
                _Assert.assertTrue(resultList.size()<=range.getLimit());
            }
            
            return resultList;
        }
        
        throw _Exceptions.unsupportedOperation("query type %s (%s) not supported by this persistence implementation",
                query.getClass(),
                query.getDescription());
        
        
        //return getJdoPersistenceSession().allMatchingQuery(query);
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
        
        val persistenceSession = getJdoPersistenceSession();
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
        
        getJdoPersistenceSession().makePersistentInTransaction(ManagedObject.of(spec, pojo));
    }
    
    @Override
    public void delete(ObjectSpecification spec, Object pojo) {
        getJdoPersistenceSession().destroyObjectInTransaction(ManagedObject.of(spec, pojo));
    }
    
    @Override
    public void refresh(Object pojo) {
        getJdoPersistenceSession().refreshEntity(pojo);
    }
    
    @Override
    public EntityState getEntityState(Object pojo) {
        return DnEntityStateProvider.entityState(pojo);
    }

    @Override
    public <T> T detach(T pojo) {
        return getJdoPersistenceSession().getPersistenceManager().detachCopy(pojo);
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
