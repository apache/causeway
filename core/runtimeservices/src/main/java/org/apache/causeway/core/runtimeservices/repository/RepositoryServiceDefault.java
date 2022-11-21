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
package org.apache.causeway.core.runtimeservices.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.exceptions.unrecoverable.PersistFailedException;
import org.apache.causeway.applib.exceptions.unrecoverable.RepositoryException;
import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.query.QueryRange;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEntityUtil;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;
import org.apache.causeway.core.metamodel.objectmanager.ObjectBulkLoader;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".RepositoryServiceDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
@RequiredArgsConstructor
//@Log4j2
public class RepositoryServiceDefault
implements RepositoryService, HasMetaModelContext {

    final FactoryService factoryService;
    final WrapperFactory wrapperFactory;
    final TransactionService transactionService;
    final CausewayConfiguration causewayConfiguration;

    @Getter(onMethod_ = {@Override})
    final MetaModelContext metaModelContext;

    private boolean autoFlush;

    @PostConstruct
    public void init() {
        val disableAutoFlush = causewayConfiguration.getCore().getRuntimeServices().getRepositoryService().isDisableAutoFlush();
        this.autoFlush = !disableAutoFlush;
    }

    @Override
    public EntityState getEntityState(final @Nullable Object object) {
        val adapter = getObjectManager().adapt(unwrapped(object));
        return MmEntityUtil.getEntityState(adapter);
    }

    @Override
    public <T> T detachedEntity(final @NonNull T entity) {
        return factoryService.detachedEntity(entity);
    }

    @Override
    public <T> T persist(final T domainObject) {

        val adapter = getObjectManager().adapt(unwrapped(domainObject));
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            throw new PersistFailedException("Object not known to framework (unable to create/obtain an adapter)");
        }
        // only persist detached or new entities, otherwise skip
        val entityState = MmEntityUtil.getEntityState(adapter);
        if(!entityState.isPersistable()
                || entityState.isAttached()) {
            return domainObject;
        }
        MmEntityUtil.persistInCurrentTransaction(adapter);
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
        val adapter = getObjectManager().adapt(unwrapped(domainObject));
        if(MmEntityUtil.hasOid(adapter)) {
            MmEntityUtil.destroyInCurrentTransaction(adapter);
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
        return allMatches(Query.<T>allInstances(type));
    }

    @Override
    public <T> List<T> allInstances(final Class<T> type, final long start, final long count) {
        return allMatches(Query.<T>allInstances(type)
                .withRange(QueryRange.of(start, count)));
    }

    @Override
    public <T> List<T> allMatches(final Class<T> ofType, final Predicate<? super T> predicate) {
        return allMatches(ofType, predicate, 0L, Long.MAX_VALUE);
    }


    @Override
    public <T> List<T> allMatches(final Class<T> ofType, final Predicate<? super T> predicate, final long start, final long count) {
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
        val resultTypeSpec = getSpecificationLoader()
                .specForType(query.getResultType())
                .orElse(null);

        if(resultTypeSpec==null) {
            return Collections.emptyList();
        }

        val queryRequest = ObjectBulkLoader.Request.of(resultTypeSpec, query);
        val allMatching = getObjectManager().queryObjects(queryRequest);
        final List<T> resultList = _Casts.uncheckedCast(MmUnwrapUtil.multipleAsList(allMatching));
        return resultList;
    }

    @Override
    public <T> Optional<T> uniqueMatch(final Class<T> type, final Predicate<T> predicate) {
        final List<T> instances = allMatches(type, predicate);
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
        final List<T> instances = allMatches(type, predicate);
        return firstInstanceElseEmpty(instances);
    }


    @Override
    public <T> Optional<T> firstMatch(final Query<T> query) {
        final List<T> instances = allMatches(query);
        return firstInstanceElseEmpty(instances);
    }

    @Override
    public <T> T refresh(final T entity) {
        if(entity==null) { return null; }

        getSpecificationLoader()
        .specForType(entity.getClass())
        .flatMap(ObjectSpecification::entityFacet)
        .ifPresent(entityFacet->entityFacet.refresh(entity));

        return entity;
    }

    @Override
    public <T> T detach(final T entity) {
        if(entity==null) { return null; }

        return getSpecificationLoader()
        .specForType(entity.getClass())
        .flatMap(ObjectSpecification::entityFacet)
        .map(entityFacet->entityFacet.detach(entity))
        .map(detachedEntity->
            detachedEntity==entity
            ? detachedEntity
            : getServiceInjector().injectServicesInto(detachedEntity))
        .orElse(entity);
    }

    @Override
    public <T> void removeAll(final Class<T> cls) {
        allInstances(cls).forEach(this::remove);

    }

    // -- HELPER

    private static <T> Optional<T> firstInstanceElseEmpty(final List<T> instances) {
        return instances.size() == 0
                ? Optional.empty()
                : Optional.of(instances.get(0));
    }

    private Object unwrapped(final Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }


}
