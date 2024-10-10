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
 *
 */
package org.apache.causeway.persistence.querydsl.applib.query;

import org.apache.causeway.persistence.querydsl.applib.services.support.DetachedQueryFactory;
import org.apache.causeway.persistence.querydsl.applib.services.support.QueryDslSupport;

import com.querydsl.core.FetchableQuery;
import com.querydsl.core.Query;
import com.querydsl.core.support.ExtendedSubQuery;
import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;

/**
 * Representation of a query (conceptually, a SQL <code>SELECT</code> statement more or less).  If attached to
 * a persistent context, then can be used {@link DslQuery#fetch() fetch}'ed.  If not attached, can be used to build
 * other queries (because it also implements the QueryDSL {@link Expression} interface.
 *
 * <p>
 *  Typically is built up using {@link QueryDslSupport} (in which case will be attached to a persistence context and
 *  can be fetched immediately if desired), or else using {@link DetachedQueryFactory} (in which case will <i>not</i> be
 *  attached, but can be used to build up other queries, eg as a subquery).
 * </p>
 *
 * @since 2.1 {@index}
 *
 * @see QueryDslSupport
 * @see DetachedQueryFactory
 *
 * @param <T>
 */
public interface DslQuery<T> extends FetchableQuery<T, DslQuery<T>>, Query<DslQuery<T>>, ExtendedSubQuery<T> {

    /**
     * Change the projection of this query
     *
     * @param <U> the new subtype
     * @param expr new projection
     *
     * @return the current object
     */
    <U> DslQuery<U> projection(Expression<U> expr);

    /**
     * Add query sources
     *
     * @param sources sources
     * @return the current object
     */
    DslQuery<T> from(EntityPath<?>... sources);

    /**
     * Add query sources
     *
     * @param path source
     * @param alias alias
     * @param <U>
     * @return the current object
     */
    <U> DslQuery<T> from(CollectionExpression<?, U> path, Path<U> alias);

}
