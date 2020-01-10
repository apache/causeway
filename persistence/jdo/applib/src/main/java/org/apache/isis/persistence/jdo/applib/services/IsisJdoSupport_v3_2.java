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

import java.util.List;

import javax.annotation.Nullable;
import javax.jdo.JDOQLTypedQuery;
import javax.jdo.Query;
import javax.jdo.query.BooleanExpression;

import org.datanucleus.store.rdbms.RDBMSPropertyNames;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * Service that provide a number of workarounds when using JDO/DataNucleus.
 */
public interface IsisJdoSupport_v3_2 extends IsisJdoSupport
{

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
    @Programmatic
    <T> List<T> executeQuery(final Class<T> cls, @Nullable final BooleanExpression filter);

    @Programmatic
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
    @Programmatic
    <T> T executeQueryUnique(final Class<T> cls, @Nullable final BooleanExpression filter);

    @Programmatic
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
    @Programmatic
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
    @Programmatic
    default void disableMultivaluedFetch(JDOQLTypedQuery<?> query) {
        query.extension(RDBMSPropertyNames.PROPERTY_RDBMS_QUERY_MULTIVALUED_FETCH, "none");
    }

    /**
     * Fetch Optimization
     * @see {@link IsisJdoSupport_v3_2#disableMultivaluedFetch(JDOQLTypedQuery)}
     * @param query
     */
    @Programmatic
    default void disableMultivaluedFetch(Query<?> query) {
        query.addExtension(RDBMSPropertyNames.PROPERTY_RDBMS_QUERY_MULTIVALUED_FETCH, "none");
    }

}
