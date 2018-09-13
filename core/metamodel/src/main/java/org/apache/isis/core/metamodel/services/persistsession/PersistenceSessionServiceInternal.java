/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.services.persistsession;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.xactn.Transaction;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public interface PersistenceSessionServiceInternal extends ObjectAdapterProvider.Delegating {
    
    // -- instantiate

    /**
     * Provided by the <tt>PersistenceSession</tt> when used by framework.
     *
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    @Programmatic
    ObjectAdapter createTransientInstance(ObjectSpecification spec);

    @Programmatic
    ObjectAdapter createViewModelInstance(ObjectSpecification spec, String memento);

    // -- retrieve

    /**
     * Provided by <tt>PersistenceSession</tt> when used by framework.
     *
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    @Programmatic
    void resolve(Object parent);

    /**
     * Provided by <tt>PersistenceSession</tt> when used by framework.
     *
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     *
     * @deprecated - left over from manual object resolving.
     */
    @Deprecated
    @Programmatic
    void resolve(Object parent, Object field);

    /**
     * Provided by <tt>PersistenceSession</tt> when used by framework.
     *
     * <p>
     * Called by <tt>BookmarkServicesDefault</tt>.
     * @return
     */
    @Programmatic
    Object lookup(Bookmark bookmark, final BookmarkService.FieldResetPolicy fieldResetPolicy);

    @Programmatic
    Bookmark bookmarkFor(Object domainObject);

    @Programmatic
    Bookmark bookmarkFor(Class<?> cls, String identifier);



    // -- beginTran, flush, commit, currentTransaction

    @Programmatic
    void beginTran();

    @Programmatic
    void beginTran(final Command commandIfAny);

    /**
     * Provided by <tt>TransactionManager</tt> when used by framework.
     *
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    @Programmatic
    boolean flush();

    /**
     * Provided by <tt>TransactionManager</tt> when used by framework.
     *
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    @Programmatic
    void commit();

    @Programmatic
    void abortTransaction();

    @Programmatic
    Transaction currentTransaction();

    @Programmatic
    CountDownLatch currentTransactionLatch();

    @Programmatic
    TransactionState getTransactionState();

    // -- makePersistent, remove

    /**
     * Provided by the <tt>PersistenceSession</tt> when used by framework.
     *
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt> and also by
     * <tt>DomainObjectInvocationHandler#handleSaveMethod()</tt>.
     */
    @Programmatic
    void makePersistent(ObjectAdapter adapter);

    /**
     * Provided by <tt>UpdateNotifier</tt> and <tt>PersistenceSession</tt> when
     * used by framework.
     *
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    @Programmatic
    void remove(ObjectAdapter adapter);

    // -- allMatchingQuery, firstMatchingQuery
    /**
     * Provided by <tt>PersistenceSession</tt> when used by framework.
     *
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt> and also by the choices
     * facets.
     */
    @Programmatic
    <T> List<ObjectAdapter> allMatchingQuery(Query<T> query);

    /**
     * Provided by <tt>PersistenceSession</tt> when used by framework.
     *
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    @Programmatic
    <T> ObjectAdapter firstMatchingQuery(Query<T> query);

    @Programmatic
    void executeWithinTransaction(Runnable task);
    
    @Programmatic
    <T> T executeWithinTransaction(Supplier<T> task);

}
