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
package org.apache.causeway.applib.query;

import java.io.Serializable;
import java.util.function.Predicate;

import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.collections.Can;

import lombok.NonNull;


/**
 * For use by repository implementations, representing the values of a query.
 *
 * <p>
 * The implementations of these objects are be provided by the underlying
 * persistor/object store; consult its documentation.
 * <p>
 * Implementations are expected to implement the {@link #getRange()}
 * method, which is used to support range / paging
 * the data. Returned result sets are expected to start from index "start",
 * and no more than "count" items are expected.
 * <p>
 * <b>Note:</b> not every object store will necessarily support this
 * interface. In particular, the in-memory object store does not. For this, you
 * can use the {@link Predicate} interface to similar effect, for example in
 * {@link RepositoryService#allMatches(Class, Predicate, long, long)}).
 *
 * <b>Note:</b> that the predicate is applied within the {@link RepositoryService}
 * (ie client-side) rather than being pushed back to the object store.
 *
 * @since 1.x {@index}
 */
public interface Query<T> extends Serializable {

    /**
     * The {@link Class} of the objects returned by this query.
     */
    Class<T> getResultType();

    /**
     * A human-readable representation of this query and its values.
     */
    String getDescription();

    /**
     * Returns a model with start index into the set table and maximal number of items to return.
     */
    QueryRange getRange();

    // -- WITHERS

    Query<T> withRange(@NonNull QueryRange range);

    default Query<T> withRange(final long ...range) {
        return withRange(QueryRange.of(range));
    }

    default Query<T> withStart(final long start) {
        return withRange(start);
    }

    default Query<T> withLimit(final long limit) {
        return withRange(0L, limit);
    }

    // -- FACTORIES

    static <T> Query<T> allInstances(
            final @NonNull Class<T> resultType) {
        return new _AllInstancesQueryDefault<>(resultType, QueryRange.unconstrained());
    }

    static <T> NamedQuery<T> named(
            final @NonNull Class<T> resultType,
            final @NonNull String queryName) {
        return new _NamedQueryDefault<>(resultType, queryName, QueryRange.unconstrained(), null);
    }

    static <T> SelectByIdQuery<T> selectByIdQuery(
            final @NonNull Class<T> resultType,
            final @NonNull String ... idsStringified) {
        return selectByIdQuery(resultType, Can.ofArray(idsStringified));
    }

    static <T> SelectByIdQuery<T> selectByIdQuery(
            final @NonNull Class<T> resultType,
            final @NonNull Can<String> idsStringified) {
        return new _SelectByIdQueryDefault<>(resultType, idsStringified, QueryRange.unconstrained());
    }

}
