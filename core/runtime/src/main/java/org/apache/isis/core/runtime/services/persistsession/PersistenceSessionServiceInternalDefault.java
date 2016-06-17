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
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.applib.services.xactn.Transaction;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosure;

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
        return getPersistenceSession().lookup(bookmark, fieldResetPolicy);
    }

    @Override
    public Bookmark bookmarkFor(Object domainObject) {
        final ObjectAdapter adapter = getPersistenceSession().adapterFor(domainObject);
        final Oid oid = adapter.getOid();
        if(oid == null) {
            // values cannot be bookmarked
            return null;
        }
        if(!(oid instanceof RootOid)) {
            // must be root
            return null;
        }
        final RootOid rootOid = (RootOid) oid;
        return rootOid.asBookmark();
    }

    @Override
    public Bookmark bookmarkFor(Class<?> cls, String identifier) {
        final ObjectSpecification objectSpec = getSpecificationLoader().loadSpecification(cls);
        String objectType = objectSpec.getSpecId().asString();
        return new Bookmark(objectType, identifier);
    }

    @Override
    public void resolve(final Object parent) {
        getPersistenceSession().resolve(parent);
    }

    /**
     * @deprecated - left over from manual object resolving.
     */
    @Deprecated
    @Override
    public void resolve(final Object parent, final Object field) {
        if (field == null) {
            resolve(parent);
        }
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
    public Transaction currentTransaction() {
        return getTransactionManager().getCurrentTransaction();
    }

    @Override
    public <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query) {
        return getPersistenceSession().allMatchingQuery(query);
    }

    @Override
    public <T> ObjectAdapter firstMatchingQuery(final Query<T> query) {
        return getPersistenceSession().firstMatchingQuery(query);
    }

    @Override
    public void executeWithinTransaction(TransactionalClosure transactionalClosure) {
        getTransactionManager().executeWithinTransaction(transactionalClosure);
    }

    protected PersistenceSession getPersistenceSession() {
        return getIsisSessionFactory().getCurrentSession().getPersistenceSession();
    }

    private IsisSessionFactory getIsisSessionFactory() {
        return isisSessionFactory;
    }

    @Programmatic
    public IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    protected SpecificationLoader getSpecificationLoader() {
        return getIsisSessionFactory().getSpecificationLoader();
    }


    @javax.inject.Inject
    IsisSessionFactory isisSessionFactory;


}
