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

package org.apache.isis.core.runtimeservices.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Repository;

import org.apache.isis.applib.PersistFailedException;
import org.apache.isis.applib.RepositoryException;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.query.ObjectBulkLoader;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;

import lombok.val;

@Repository
@Named("isisRuntimeServices.RepositoryServiceDefault")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("Default")
//@Log4j2
public class RepositoryServiceDefault implements RepositoryService {

    @Inject private FactoryService factoryService;
    @Inject private WrapperFactory wrapperFactory;
    @Inject private TransactionService transactionService;
    @Inject private IsisConfiguration isisConfiguration;
    @Inject private ObjectManager objectManager; 
    
    private boolean autoFlush;

    @PostConstruct
    public void init() {
        val disableAutoFlush = isisConfiguration.getCore().getRuntimeServices().getRepositoryService().isDisableAutoFlush();
        this.autoFlush = !disableAutoFlush;
    }

    @Override
    public EntityState getEntityState(@Nullable final Object object) {
        val adapter = objectManager.adapt(unwrapped(object));
        return EntityUtil.getEntityState(adapter);
    }
    
    @Override
    public <T> T detachedEntity(final Class<T> domainClass) {
        return factoryService.detachedEntity(domainClass);
    }

    @Override
    public <T> T persist(final T domainObject) {
        
        val adapter = objectManager.adapt(unwrapped(domainObject));
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            throw new PersistFailedException("Object not known to framework (unable to create/obtain an adapter)");
        }
        // only persist detached entities, otherwise skip
        if(!EntityUtil.isDetached(adapter)) {
            return domainObject;
        }
        EntityUtil.persistInTransaction(adapter);
        return domainObject;
    }


    @Override
    public <T> T persistAndFlush(final T object) {
        persist(object);
        transactionService.flushTransaction();
        return object;
    }

    @Override
    public void remove(final Object domainObject) {
        if (domainObject == null) {
            return; // noop
        }
        val adapter = objectManager.adapt(unwrapped(domainObject));
        if(EntityUtil.isAttached(adapter)) {
            EntityUtil.destroyInTransaction(adapter);   
        }
    }

    @Override
    public void removeAndFlush(final Object domainObject) {
        remove(domainObject);
        transactionService.flushTransaction();
    }


    // -- allInstances, allMatches, uniqueMatch, firstMatch

    @Override
    public <T> List<T> allInstances(final Class<T> type) {
        return allMatches(new QueryFindAllInstances<T>(type, 0L, Long.MAX_VALUE));
    }

    @Override
    public <T> List<T> allInstances(final Class<T> type, long start, long count) {
        return allMatches(new QueryFindAllInstances<T>(type, start, count));
    }


    @Override
    public <T> List<T> allMatches(Class<T> ofType, Predicate<? super T> predicate) {
        return allMatches(ofType, predicate, 0L, Long.MAX_VALUE);
    }


    @Override
    public <T> List<T> allMatches(Class<T> ofType, final Predicate<? super T> predicate, long start, long count) {
        return _NullSafe.stream(allInstances(ofType, start, count))
                .filter(predicate)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public <T> List<T> allMatches(final Query<T> query) {
        if(autoFlush) {
            transactionService.flushTransaction();
        }
        return submitQuery(query);
    }

    <T> List<T> submitQuery(final Query<T> query) {
        val resultTypeSpec = objectManager.getMetaModelContext()
                .getSpecificationLoader()
                .loadSpecification(query.getResultType());
        
        val queryRequest = ObjectBulkLoader.Request.of(resultTypeSpec, query);
        val allMatching = objectManager.queryObjects(queryRequest);
        return _Casts.uncheckedCast(UnwrapUtil.multipleAsList(allMatching));
    }

    @Override
    public <T> Optional<T> uniqueMatch(final Class<T> type, final Predicate<T> predicate) {
        final List<T> instances = allMatches(type, predicate, 0, 2); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more than one instance of " + type + " matching filter " + predicate);
        }
        return firstInstanceElseEmpty(instances);
    }


    @Override
    public <T> Optional<T> uniqueMatch(final Query<T> query) {
        final List<T> instances = allMatches(query); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more that one instance for query:" + query.getDescription());
        }
        return firstInstanceElseEmpty(instances);
    }

    @Override
    public <T> Optional<T> firstMatch(final Class<T> type, final Predicate<T> predicate) {
        final List<T> instances = allMatches(type, predicate, 0, 2); // No need to fetch more than 2.
        return firstInstanceElseEmpty(instances);
    }


    @Override
    public <T> Optional<T> firstMatch(final Query<T> query) {
        final List<T> instances = allMatches(query); // No need to fetch more than 2.
        return firstInstanceElseEmpty(instances);
    }

    @Override
    public <T> T refresh(T pojo) {
        val managedObject = objectManager.adapt(pojo);
        objectManager.getObjectRefresher().refreshObject(managedObject);
        return _Casts.uncheckedCast(managedObject.getPojo());
    }

    @Override
    public <T> T detach(T entity) {
        val managedObject = objectManager.adapt(entity);
        val managedDetachedObject = objectManager.getObjectDetacher().detachObject(managedObject);
        return _Casts.uncheckedCast(managedDetachedObject.getPojo());
    }

    @Override
    public <T> void removeAll(Class<T> cls) {
        allInstances(cls).forEach(this::remove);

    }

    // -- HELPER
    
    private static <T> Optional<T> firstInstanceElseEmpty(final List<T> instances) {
        return instances.size() == 0
                ? Optional.empty()
                : Optional.of(instances.get(0));
    }

    private Object unwrapped(Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }


}
