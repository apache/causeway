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

package org.apache.isis.applib.services.jdosupport;

import java.util.List;

import org.apache.isis.applib.annotation.Programmatic;
import org.datanucleus.query.typesafe.BooleanExpression;
import org.datanucleus.query.typesafe.TypesafeQuery;


/**
 * Service that provide a number of workarounds when using JDO/DataNucleus.
 */
public interface IsisJdoSupport_v3_1 extends org.apache.isis.applib.services.jdosupport.IsisJdoSupport {

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
    <T> List<T> executeQuery(final Class<T> cls, final BooleanExpression booleanExpression);

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
    <T> T executeQueryUnique(final Class<T> cls, final BooleanExpression booleanExpression);

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
    <T> TypesafeQuery<T> newTypesafeQuery(Class<T> cls);

}