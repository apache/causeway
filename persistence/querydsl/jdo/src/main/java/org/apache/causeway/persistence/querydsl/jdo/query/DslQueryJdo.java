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
package org.apache.causeway.persistence.querydsl.jdo.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.FetchableQuery;
import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.QueryResults;
import com.querydsl.core.ResultTransformer;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jdo.JDOQuery;

import org.apache.causeway.persistence.querydsl.applib.query.DslQuery;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class DslQueryJdo<T> implements DslQuery<T> {
    private static final long serialVersionUID = 1L;
    
    final JDOQuery<T> jdoQuery;

    @Override
    public <U> DslQuery<U> projection(Expression<U> expr) {
        return DslQueryJdo.of(jdoQuery.select(expr));
    }

    @Override
    public DslQuery<T> from(EntityPath<?>... sources) {
        return DslQueryJdo.of(jdoQuery.from(sources));
    }

    @Override
    public <U> DslQuery<T> from(CollectionExpression<?, U> path, Path<U> alias) {
        return DslQueryJdo.of(jdoQuery.from(path,alias));
    }

    @Override
    public <U> FetchableQuery<U, ?> select(Expression<U> expression) {
        return jdoQuery.select(expression);
    }

    @Override
    public FetchableQuery<Tuple, ?> select(Expression<?>... expressions) {
        return jdoQuery.select(expressions);
    }

    @Override
    public <S> S transform(ResultTransformer<S> resultTransformer) {
        return jdoQuery.transform(resultTransformer);
    }

    @Override
    public List<T> fetch() {
        List<T> result= newList(jdoQuery.fetch());
        jdoQuery.close();
        return result;
    }

    @Override
    public T fetchFirst() {
        T result = null;
        try {
            // When using offset/limit it is mandatory to apply an ordering!!
            if (jdoQuery.getMetadata().getOrderBy().isEmpty()) {
                jdoQuery.orderBy(new OrderSpecifier<>(com.querydsl.core.types.Order.ASC, Expressions.constant(1)));
            }
            List<T> results = jdoQuery
                    // instead of fetchFirst; workaround for SqlServer and the v1 datanuclues implementation which
                    // doesn't handle offset and limit 1 correctly.
                    .offset(0)
                    .limit(2)
                    .fetch();
            if (results != null && !results.isEmpty()) {
                result = results.get(0);
            }
        } finally {
            jdoQuery.close();
        }
        return result;
    }

    @Override
    public T fetchOne() throws NonUniqueResultException {
        T result = null;
        try {
            // When using offset/limit it is mandatory to apply an ordering!!
            if (jdoQuery.getMetadata().getOrderBy().isEmpty()) {
                jdoQuery.orderBy(new OrderSpecifier<>(com.querydsl.core.types.Order.ASC, Expressions.constant(1)));
            }
            List<T> results = jdoQuery
                    // instead of fetchOne; workaround for SqlServer and the v1 datanuclues implementation which
                    // doesn't handle offset and limit 1 correctly.
                    .offset(0)
                    .limit(2)
                    .fetch();
            if (results != null) {
                if (results.size() > 1)
                    throw new NonUniqueResultException(jdoQuery.toString());
                if (results.size() == 1)
                    result = results.get(0);
            }
        } finally {
            jdoQuery.close();
        }
        return result;
    }

    @Override
    public CloseableIterator<T> iterate() {
        return jdoQuery.iterate();
    }

    @Override
    public QueryResults<T> fetchResults() {
        return jdoQuery.fetchResults();
    }

    @Override
    public long fetchCount() {
        return jdoQuery.fetchCount();
    }

    @Override
    public DslQuery<T> groupBy(Expression<?>... expressions) {
        return DslQueryJdo.of(jdoQuery.groupBy(expressions));
    }

    @Override
    public DslQuery<T> having(Predicate... predicates) {
        return DslQueryJdo.of(jdoQuery.having(predicates));
    }

    @Override
    public DslQuery<T> limit(long limit) {
        return DslQueryJdo.of(jdoQuery.limit(limit));
    }

    @Override
    public DslQuery<T> offset(long offset) {
        return DslQueryJdo.of(jdoQuery.offset(offset));
    }

    @Override
    public DslQuery<T> restrict(QueryModifiers queryModifiers) {
        return DslQueryJdo.of(jdoQuery.restrict(queryModifiers));
    }

    @Override
    public DslQuery<T> orderBy(OrderSpecifier<?>... orderSpecifiers) {
        return DslQueryJdo.of(jdoQuery.orderBy(orderSpecifiers));
    }

    @Override
    public <P> DslQuery<T> set(ParamExpression<P> paramExpression, P t) {
        return DslQueryJdo.of(jdoQuery.set(paramExpression,t));
    }

    @Override
    public DslQuery<T> distinct() {
        return DslQueryJdo.of(jdoQuery.distinct());
    }

    @Override
    public DslQuery<T> where(Predicate... predicates) {
        return DslQueryJdo.of(jdoQuery.where(predicates));
    }

    @Override
    public BooleanExpression eq(Expression<? extends T> expression) {
        return jdoQuery.eq(expression);
    }

    @Override
    public BooleanExpression eq(T t) {
        return jdoQuery.eq(t);
    }

    @Override
    public BooleanExpression ne(Expression<? extends T> expression) {
        return jdoQuery.ne(expression);
    }

    @Override
    public BooleanExpression ne(T t) {
        return jdoQuery.ne(t);
    }

    @Override
    public BooleanExpression contains(Expression<? extends T> expression) {
        return jdoQuery.contains(expression);
    }

    @Override
    public BooleanExpression contains(T t) {
        return jdoQuery.contains(t);
    }

    @Override
    public BooleanExpression exists() {
        return jdoQuery.exists();
    }

    @Override
    public BooleanExpression notExists() {
        return jdoQuery.exists();
    }

    @Override
    public BooleanExpression lt(Expression<? extends T> expression) {
        return jdoQuery.lt(expression);
    }

    @Override
    public BooleanExpression lt(T t) {
        return jdoQuery.lt(t);
    }

    @Override
    public BooleanExpression gt(Expression<? extends T> expression) {
        return jdoQuery.gt(expression);
    }

    @Override
    public BooleanExpression gt(T t) {
        return jdoQuery.gt(t);
    }

    @Override
    public BooleanExpression loe(Expression<? extends T> expression) {
        return jdoQuery.loe(expression);
    }

    @Override
    public BooleanExpression loe(T t) {
        return jdoQuery.loe(t);
    }

    @Override
    public BooleanExpression goe(Expression<? extends T> expression) {
        return jdoQuery.goe(expression);
    }

    @Override
    public BooleanExpression goe(T t) {
        return jdoQuery.goe(t);
    }

    @Override
    public BooleanOperation isNull() {
        return jdoQuery.isNull();
    }

    @Override
    public BooleanOperation isNotNull() {
        return jdoQuery.isNotNull();
    }

    @Override
    public BooleanExpression in(Collection<? extends T> collection) {
        return jdoQuery.in(collection);
    }

    @Override
    public BooleanExpression in(T... ts) {
        return jdoQuery.in(ts);
    }

    @Override
    public QueryMetadata getMetadata() {
        return jdoQuery.getMetadata();
    }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C c) {
        return jdoQuery.accept(visitor,c);
    }

    @Override
    public Class<? extends T> getType() {
        return jdoQuery.getType();
    }

    private static <T> List<T> newList(Collection<T> objs) {
        return newArrayList(objs);
    }

    private static <T> ArrayList<T> newArrayList(Collection<T> objs) {
        ArrayList<T> result = new ArrayList<>();
        result.addAll(objs);
        return result;
    }

}
