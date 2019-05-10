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
package org.apache.isis.core.runtime.system.transaction;

import static java.util.Objects.requireNonNull;
import static org.apache.isis.commons.internal.base._With.acceptIfPresent;
import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import javax.annotation.Priority;
import javax.inject.Singleton;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.xactn.Transaction;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.ioc.PriorityConstants;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.Oid.Factory;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

@Singleton @Priority(PriorityConstants.PRIORITY_BELOW_DEFAULT)
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
        return getPersistenceSession().newTransientInstance(spec);
    }

    @Override
    public ObjectAdapter createViewModelInstance(ObjectSpecification spec, String memento) {
        return getPersistenceSession().recreateViewModelInstance(spec, memento);
    }

    @Override
    public Object lookup(
            final Bookmark bookmark,
            final BookmarkService.FieldResetPolicy fieldResetPolicy) {
        
        final RootOid rootOid = Factory.ofBookmark(bookmark);
        final PersistenceSession ps = getPersistenceSession();
        final boolean denyRefresh = fieldResetPolicy == BookmarkService.FieldResetPolicy.DONT_REFRESH; 
                        
        if(rootOid.isViewModel()) {
            final ObjectAdapter adapter = ps.adapterFor(rootOid, ConcurrencyChecking.NO_CHECK);
            final Object pojo = mapIfPresentElse(adapter, ObjectAdapter::getPojo, null);
            
            return pojo;
            
        } else if(denyRefresh) {
            
            final Object pojo = ps.fetchPersistentPojoInTransaction(rootOid);
            return pojo;            
            
        } else {
            final ObjectAdapter adapter = ps.adapterFor(rootOid, ConcurrencyChecking.NO_CHECK);
            
            final Object pojo = mapIfPresentElse(adapter, ObjectAdapter::getPojo, null);
            acceptIfPresent(pojo, ps::refreshRootInTransaction);
            return pojo;
        }
        
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
        getPersistenceSession().refreshRootInTransaction(parent);
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
        return IsisContext.getPersistenceSession()
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
