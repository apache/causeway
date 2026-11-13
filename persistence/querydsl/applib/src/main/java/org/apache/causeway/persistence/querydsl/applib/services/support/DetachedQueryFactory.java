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
package org.apache.causeway.persistence.querydsl.applib.services.support;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;

import org.apache.causeway.persistence.querydsl.applib.query.DslQuery;

/**
 * Factory for detached query expressions.  These are most commonly used in subqueries; they cannot be fetched directly
 * because they are not attached to any persistence context.
 *
 * @since 2.1 {@index}
 *
 * @see QueryDslSupport
 * @see <a href="https://chatgpt.com/share/66f522fd-a9cc-8010-9659-731f9a6182da">ChatGPT transcript on attached vs detached queries.</a>
 */
public interface DetachedQueryFactory {

    <T> DslQuery<T> select(Expression<T> expr);

    DslQuery<Tuple> select(Expression<?>... exprs);

    /**
     * Create a new detached {@link DslQuery} instance with the given projection
     *
     * @param expr projection
     * @param <T>
     * @return select(distinct expr)
     */
    default <T> DslQuery<T> selectDistinct(Expression<T> expr) {
        return select(expr).distinct();
    }

    /**
     * Create a new detached {@link DslQuery} instance with the given projection
     *
     * @param exprs projection
     * @return select(distinct exprs)
     */
    default DslQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return select(exprs).distinct();
    }

    /**
     * Create a new detached {@link DslQuery} instance with the given projection 0
     *
     * @return select(0)
     */
    default DslQuery<Integer> selectZero() {
        return select(Expressions.ZERO);
    }

    /**
     * Create a new detached {@link DslQuery} instance with the projection 1
     *
     * @return select(1)
     */
    default DslQuery<Integer> selectOne() {
        return select(Expressions.ONE);
    }

    /**
     * Create a new detached {@link DslQuery} instance with the given projection
     *
     * @param expr projection and source
     * @param <T>
     * @return select(expr).from(expr)
     */
    default <T> DslQuery<T> selectFrom(EntityPath<T> expr) {
        return select(expr).from(expr);
    }
}
