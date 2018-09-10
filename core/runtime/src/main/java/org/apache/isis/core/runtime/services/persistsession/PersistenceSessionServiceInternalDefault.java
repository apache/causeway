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

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.xactn.Transaction;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + (Integer.MAX_VALUE - 1)  // ie before the Noop impl in metamodel
        )
public class PersistenceSessionServiceInternalDefault implements PersistenceSessionServiceInternal {

    @Override
    public ObjectAdapterProvider getObjectAdapterProvider() {
        return getPersistenceSession();
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
            final BookmarkService.FieldResetPolicy fieldResetPolicy) {
        return getPersistenceSession().lookup(bookmark, fieldResetPolicy);
    }

    @Override
    public Bookmark bookmarkFor(Object domainObject) {
        final ObjectAdapter adapter = getPersistenceSession().adapterFor(domainObject);
        if(adapter.isValue()) {
            // values cannot be bookmarked
            return null;
        }
        final Oid oid = adapter.getOid();
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
        beginTran(null);
    }

    @Override
    public void beginTran(final Command commandIfAny) {
        getTransactionManager().startTransaction(commandIfAny);
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
    public void abortTransaction() {
        getTransactionManager().abortTransaction();
    }

    @Override
    public Transaction currentTransaction() {
        return getTransactionManager().getCurrentTransaction();
    }

    @Override
    public CountDownLatch currentTransactionLatch() {
        IsisTransaction transaction = getTransactionManager().getCurrentTransaction();
        return transaction==null ? new CountDownLatch(0) : transaction.countDownLatch();
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
    public void executeWithinTransaction(Runnable task) {
        getTransactionManager().executeWithinTransaction(task);
    }
    
    @Override
    public <T> T executeWithinTransaction(Supplier<T> task) {
        return getTransactionManager().executeWithinTransaction(task);
    }

    @Override
    public TransactionState getTransactionState() {
        final IsisTransaction transaction = getTransactionManager().getCurrentTransaction();
        if (transaction == null) {
            return TransactionState.NONE;
        }
        IsisTransaction.State state = transaction.getState();
        return state.getTransactionState();
    }

    protected PersistenceSession getPersistenceSession() {
        return ofNullable(getIsisSessionFactory().getCurrentSession())
                .map(IsisSession::getPersistenceSession)
                .orElseThrow(()->new NonRecoverableException("No IsisSession on current thread."));
    }

    private IsisSessionFactory getIsisSessionFactory() {
        return requireNonNull(isisSessionFactory, "IsisSessionFactory was not injected.");
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
