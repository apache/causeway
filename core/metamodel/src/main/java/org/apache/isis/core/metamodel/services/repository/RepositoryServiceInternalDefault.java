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

package org.apache.isis.core.metamodel.services.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.isis.applib.PersistFailedException;
import org.apache.isis.applib.RepositoryException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class RepositoryServiceInternalDefault implements RepositoryService {

    private boolean autoFlush;

    @Programmatic
    @PostConstruct
    public void init(Map<String, String> properties) {
        final boolean disableAutoFlush = Boolean.parseBoolean(properties.get(KEY_DISABLE_AUTOFLUSH));
        this.autoFlush = !disableAutoFlush;
    }


    // //////////////////////////////////////

    @Programmatic
    @Override
    public <T> T instantiate(final Class<T> domainClass) {
        return factoryService.instantiate(domainClass);
    }

    // //////////////////////////////////////

    @Programmatic
    @Override
    public boolean isPersistent(final Object domainObject) {
        final ObjectAdapter adapter = getObjectAdapterProvider().adapterFor(unwrapped(domainObject));
        return adapter.representsPersistent();
    }

    @Programmatic
    @Override
    public boolean isDeleted(final Object domainObject) {
        final ObjectAdapter adapter = getObjectAdapterProvider().adapterFor(unwrapped(domainObject));
        return adapter.isDestroyed();
    }

    @Programmatic
    @Override
    public <T> T persist(final T object) {
        if (isPersistent(object)) {
            return object;
        }
        final ObjectAdapter adapter = getObjectAdapterProvider().adapterFor(unwrapped(object));

        if(adapter == null) {
            throw new PersistFailedException("Object not known to framework (unable to create/obtain an adapter)");
        }
        if (adapter.isParentedCollection()) {
            // TODO check aggregation is supported
            return  object;
        }
        if (isPersistent(object)) {
            throw new PersistFailedException("Object already persistent; OID=" + adapter.getOid());
        }
        persistenceSessionServiceInternal.makePersistent(adapter);

        return object;
    }

    @Programmatic
    @Override
    public <T> T persistAndFlush(final T object) {
        persist(object);
        transactionService.flushTransaction();
        return object;
    }

    @Override
    @Programmatic
    public void remove(final Object domainObject) {
        removeIfNotAlready(domainObject);
    }

    private void removeIfNotAlready(final Object object) {
        if (!isPersistent(object)) {
            return;
        }
        if (object == null) {
            throw new IllegalArgumentException("Must specify a reference for disposing an object");
        }
        final ObjectAdapter adapter = getObjectAdapterProvider().adapterFor(unwrapped(object));
        if (!isPersistent(object)) {
            throw new RepositoryException("Object not persistent: " + adapter);
        }

        persistenceSessionServiceInternal.remove(adapter);
    }

    @Override
    @Programmatic
    public void removeAndFlush(final Object domainObject) {
        remove(domainObject);
        transactionService.flushTransaction();
    }


    // //////////////////////////////////////


    // -- allInstances, allMatches, uniqueMatch, firstMatch

    @Programmatic
    @Override
    public <T> List<T> allInstances(final Class<T> type, long... range) {
        return allMatches(new QueryFindAllInstances<T>(type, range));
    }

    // //////////////////////////////////////

    @Programmatic
    @Override
    public <T> List<T> allMatches(Class<T> ofType, final Predicate<? super T> predicate, long... range) {
        return _NullSafe.stream(allInstances(ofType, range))
                .filter(predicate)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Programmatic
    @Override
    public <T> List<T> allMatches(final Query<T> query) {
        if(autoFlush) {
            transactionService.flushTransaction();
        }
        return submitQuery(query);
    }

    <T> List<T> submitQuery(final Query<T> query) {
        final List<ObjectAdapter> allMatching = persistenceSessionServiceInternal.allMatchingQuery(query);
        return ObjectAdapter.Util.unwrapTypedPojoList(allMatching);
    }


    // //////////////////////////////////////

    @Programmatic
    @Override
    public <T> T uniqueMatch(final Class<T> type, final Predicate<T> predicate) {
        final List<T> instances = allMatches(type, predicate, 0, 2); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more than one instance of " + type + " matching filter " + predicate);
        }
        return firstInstanceElseNull(instances);
    }


    @Programmatic
    @Override
    public <T> T uniqueMatch(final Query<T> query) {
        final List<T> instances = allMatches(query); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more that one instance for query:" + query.getDescription());
        }
        return firstInstanceElseNull(instances);
    }

    // //////////////////////////////////////

    private static <T> T firstInstanceElseNull(final List<T> instances) {
        return instances.size() == 0 ? null : instances.get(0);
    }


    private Object unwrapped(Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }

    private ObjectAdapterProvider getObjectAdapterProvider() {
        return persistenceSessionServiceInternal;
    }

    @javax.inject.Inject
    FactoryService factoryService;

    @javax.inject.Inject
    WrapperFactory wrapperFactory;

    @javax.inject.Inject
    TransactionService transactionService;

    @javax.inject.Inject
    PersistenceSessionServiceInternal persistenceSessionServiceInternal;



}
