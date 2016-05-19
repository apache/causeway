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
package org.apache.isis.core.runtime.services.persistsession;

import java.util.List;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.persistence.container.DomainObjectContainerResolve;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionInternal;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + (Integer.MAX_VALUE - 1)  // ie before the Noop impl in metamodel
)
public class PersistenceSessionServiceInternalDefault implements PersistenceSessionServiceInternal {

    @Override
    public ObjectAdapter getAdapterFor(Oid oid) {
        return getPersistenceSession().getAdapterFor(oid);
    }

    @Override
    public ObjectAdapter getAdapterFor(final Object pojo) {
        return getPersistenceSession().getAdapterFor(pojo);
    }

    @Override
    public ObjectAdapter adapterFor(final Object pojo) {
        return getPersistenceSession().adapterFor(pojo);
    }

    @Override
    public ObjectAdapter adapterFor(
            final Object pojo,
            final ObjectAdapter ownerAdapter,
            final OneToManyAssociation collection) {
        return getPersistenceSession().adapterFor(pojo, ownerAdapter, collection);
    }

    @Override
    public ObjectAdapter mapRecreatedPojo(Oid oid, Object recreatedPojo) {
        return getPersistenceSession().mapRecreatedPojo(oid, recreatedPojo);
    }

    @Override
    public void removeAdapter(ObjectAdapter adapter) {
        getPersistenceSession().removeAdapter(adapter);
    }

    @Override
    public void makePersistent(final ObjectAdapter adapter) {
        getPersistenceSession().makePersistentInTransaction(adapter);
    }

    @Override
    public void remove(final ObjectAdapter adapter) {
        getPersistenceSession().destroyObjectInTransaction(adapter);
    }

    @Override
    public ObjectAdapter createTransientInstance(final ObjectSpecification spec) {
        return getPersistenceSession().createTransientInstance(spec);
    }

    @Override
    public ObjectAdapter createViewModelInstance(ObjectSpecification spec, String memento) {
        return getPersistenceSession().createViewModelInstance(spec, memento);
    }

    @Override
    public Object lookup(
            final Bookmark bookmark,
            final BookmarkService2.FieldResetPolicy fieldResetPolicy) {
        return new DomainObjectContainerResolve().lookup(bookmark, fieldResetPolicy);
    }

    @Override
    public Bookmark bookmarkFor(Object domainObject) {
        return new DomainObjectContainerResolve().bookmarkFor(domainObject);
    }

    @Override
    public Bookmark bookmarkFor(Class<?> cls, String identifier) {
        return new DomainObjectContainerResolve().bookmarkFor(cls, identifier);
    }

    @Override
    public void resolve(final Object parent) {
        new DomainObjectContainerResolve().resolve(parent);
    }

    @Override
    public void resolve(final Object parent, final Object field) {
        new DomainObjectContainerResolve().resolve(parent, field);
    }

    @Override
    public void beginTran() {
        getTransactionManager().startTransaction();
    }

    @Override
    public boolean flush() {
        return getTransactionManager().flushTransaction();
    }

    @Override
    public void commit() {
        getTransactionManager().endTransaction();
    }

    @Override
    public <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query) {
        return getPersistenceSession().allMatchingQuery(query);
    }

    @Override
    public <T> ObjectAdapter firstMatchingQuery(final Query<T> query) {
        return getPersistenceSession().firstMatchingQuery(query);
    }

    public static PersistenceSessionInternal getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    static IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

}
