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
package org.apache.isis.persistence.jdo.applib.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.jdo.JDOQLTypedQuery;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.query.BooleanExpression;

import org.datanucleus.store.rdbms.RDBMSPropertyNames;

/**
 * Primarily provides access to the current thread's {@link PersistenceManagerFactory} and
 * hence also the current thread's {@link PersistenceManager}.
 *
 * @since 2.0 {@index}
 *
 * @apiNote While ideally this service should be independent of vendor
 *          specifics, currently it depends on DataNucleus RDBMS.
 */
public interface JdoSupportService {

    // -- INTERFACE

    PersistenceManagerFactory getPersistenceManagerFactory();

    /**
     * Force a reload (corresponding to the JDO <tt>PersistenceManager</tt>'s <tt>refresh()</tt> method)
     * of a domain objects.
     *
     * <p>
     * In fact, this may just reset the lazy-load state of the domain object, but the effect is the same:
     * to cause the object's state to be reloaded from the database.
     *
     * <p>
     * The particular example that led to this method being added was a 1:m bidirectional relationship,
     * analogous to <tt>Customer <-> * Order</tt>.  Persisting the child <tt>Order</tt> object did not cause
     * the parent <tt>Customer</tt>'s collection of orders to be updated.  In fact, JDO does not make any
     * such guarantee to do so.  Options are therefore either to maintain the collection in code, or to
     * refresh the parent.
     */
    <T> T refresh(T domainObject);

    void ensureLoaded(Collection<?> collectionOfDomainObjects);

    List<Map<String, Object>> executeSql(String sql);

    Integer executeUpdate(String sql);

    /**
     * Force the deletion of all instances of the specified class.
     *
     * <p>
     * Note: this is intended primarily for testing purposes, eg clearing existing data as part of
     * installing fixtures.  It will generate a <tt>SQL DELETE</tt> for each instance.  To perform
     * a bulk deletion with a single <tt>SQL DELETE</tt>, use {@link #executeUpdate(String)}.
     *
     * <p>
     * Implementation note: It can occasionally be the case that Isis' internal adapter for the domain object is
     * still in memory.  JDO/DataNucleus seems to bump up the version of the object prior to its deletion,
     * which under normal circumstances would cause Isis to throw a concurrency exception.  Therefore
     * To prevent this from happening (ie to <i>force</i> the deletion of all instances), concurrency checking
     * is temporarily disabled while this method is performed.
     */
    void deleteAll(Class<?>... pcClasses);

    // -- QUERIES

    /**
     * To perform the most common use-case of executing a (type-safe) query against the specified class,
     * filtering using the provided {@link BooleanExpression}, then automatically cloning the returned list
     * and closing the query.
     *
     * <p>
     *     Typical usage:
     *     <pre>
     *          final QToDoItem q = QToDoItem.candidate();
     *          return executeQuery(ToDoItem.class,
     *                              q.atPath.eq(atPath).and(
     *                              q.description.indexOf(description).gt(0))
     *                              );
     *     </pre>
     * </p>
     */
    <T> List<T> executeQuery(final Class<T> cls, @Nullable final BooleanExpression filter);

    default <T> List<T> executeQuery(final Class<T> cls) {
        return executeQuery(cls, null);
    }

    /**
     * To perform a common use-case of executing a (type-safe) query against the specified class,
     * filtering a unique match using the provided {@link BooleanExpression}, then returning
     * the result and closing the query.
     *
     * <p>
     *     Typical usage:
     *     <pre>
     *          final QToDoItem q = QToDoItem.candidate();
     *          return executeQueryUnique(ToDoItem.class,
     *                              q.atPath.eq(atPath).and(
     *                              q.description.eq(description))
     *                              );
     *     </pre>
     * </p>
     */
    <T> T executeQueryUnique(final Class<T> cls, @Nullable final BooleanExpression filter);

    default <T> T executeQueryUnique(final Class<T> cls) {
        return executeQueryUnique(cls, null);
    }

    /**
     * To support the execution of type-safe queries using DataNucleus' lower-level APIs
     * (eg for group by and so on).
     *
     * <p>
     *     Responsibility for cloning any result sets and closing the query is the responsibility
     *     of the caller.
     * </p>
     */
    <T> JDOQLTypedQuery<T> newTypesafeQuery(Class<T> cls);

    // -- UTILITY

    /**
     * Fetch Optimization
     * <p>
     * From <a href="http://www.datanucleus.org/products/accessplatform/jdo/query.html">DN-5.2</a> ...
     * <p>
     * For RDBMS any single-valued member will be fetched in the original SQL query, but with
     * multiple-valued members this is not supported. However what will happen is that any
     * collection/array field will be retrieved in a single SQL query for all candidate objects
     * (by default using an EXISTS subquery); this avoids the "N+1" problem, resulting in 1 original
     * SQL query plus 1 SQL query per collection member. Note that you can disable this by either
     * not putting multi-valued fields in the FetchPlan, or by setting the query extension
     * datanucleus.rdbms.query.multivaluedFetch to none (default is "exists" using the single SQL per field).
     */
    default void disableMultivaluedFetch(JDOQLTypedQuery<?> query) {
        query.extension(RDBMSPropertyNames.PROPERTY_RDBMS_QUERY_MULTIVALUED_FETCH, "none");
    }

    /**
     * Fetch Optimization
     * @param query
     */
    default void disableMultivaluedFetch(Query<?> query) {
        query.addExtension(RDBMSPropertyNames.PROPERTY_RDBMS_QUERY_MULTIVALUED_FETCH, "none");
    }


    // -- SHORTCUTS

    default PersistenceManager getPersistenceManager() {
        return getPersistenceManagerFactory().getPersistenceManager();
    }

}
