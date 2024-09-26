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
package org.apache.causeway.persistence.querydsl.jpa.services.support;

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
import com.querydsl.jpa.impl.JPAQuery;

import org.apache.causeway.persistence.querydsl.applib.query.DslQuery;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class DslQueryJpa<T> implements DslQuery<T> {

    final JPAQuery<T> jpaQuery;

    @Override
    public <U> DslQuery<U> projection(Expression<U> expr) {
        return DslQueryJpa.of(jpaQuery.select(expr));
    }

    @Override
    public DslQuery<T> from(EntityPath<?>... sources) {
        return DslQueryJpa.of(jpaQuery.from(sources));
    }

    @Override
    public <U> DslQuery<T> from(CollectionExpression<?, U> path, Path<U> alias) {
        return DslQueryJpa.of(jpaQuery.from(path,alias));
    }

    @Override
    public <U> FetchableQuery<U, ?> select(Expression<U> expression) {
        return jpaQuery.select(expression);
    }

    @Override
    public FetchableQuery<Tuple, ?> select(Expression<?>... expressions) {
        return jpaQuery.select(expressions);
    }

    @Override
    public <S> S transform(ResultTransformer<S> resultTransformer) {
        return jpaQuery.transform(resultTransformer);
    }

    @Override
    public List<T> fetch() {
        return jpaQuery.fetch();
    }

    @Override
    public T fetchFirst() {
        return jpaQuery.fetchFirst();
    }

    @Override
    public T fetchOne() throws NonUniqueResultException {
        return jpaQuery.fetchOne();
    }

    @Override
    public CloseableIterator<T> iterate() {
        return jpaQuery.iterate();
    }

    @Override
    public QueryResults<T> fetchResults() {
        return jpaQuery.fetchResults();
    }

    @Override
    public long fetchCount() {
        return jpaQuery.fetchCount();
    }

    @Override
    public DslQuery<T> groupBy(Expression<?>... expressions) {
        return DslQueryJpa.of(jpaQuery.groupBy(expressions));
    }

    @Override
    public DslQuery<T> having(Predicate... predicates) {
        return DslQueryJpa.of(jpaQuery.having(predicates));
    }

    @Override
    public DslQuery<T> limit(long limit) {
        return DslQueryJpa.of(jpaQuery.limit(limit));
    }

    @Override
    public DslQuery<T> offset(long offset) {
        return DslQueryJpa.of(jpaQuery.offset(offset));
    }

    @Override
    public DslQuery<T> restrict(QueryModifiers queryModifiers) {
        return DslQueryJpa.of(jpaQuery.restrict(queryModifiers));
    }

    @Override
    public DslQuery<T> orderBy(OrderSpecifier<?>... orderSpecifiers) {
        return DslQueryJpa.of(jpaQuery.orderBy(orderSpecifiers));
    }

    @Override
    public <P> DslQuery<T> set(ParamExpression<P> paramExpression, P t) {
        return DslQueryJpa.of(jpaQuery.set(paramExpression,t));
    }

    @Override
    public DslQuery<T> distinct() {
        return DslQueryJpa.of(jpaQuery.distinct());
    }

    @Override
    public DslQuery<T> where(Predicate... predicates) {
        return DslQueryJpa.of(jpaQuery.where(predicates));
    }

    @Override
    public BooleanExpression eq(Expression<? extends T> expression) {
        return jpaQuery.eq(expression);
    }

    @Override
    public BooleanExpression eq(T t) {
        return jpaQuery.eq(t);
    }

    @Override
    public BooleanExpression ne(Expression<? extends T> expression) {
        return jpaQuery.ne(expression);
    }

    @Override
    public BooleanExpression ne(T t) {
        return jpaQuery.ne(t);
    }

    @Override
    public BooleanExpression contains(Expression<? extends T> expression) {
        return jpaQuery.contains(expression);
    }

    @Override
    public BooleanExpression contains(T t) {
        return jpaQuery.contains(t);
    }

    @Override
    public BooleanExpression exists() {
        return jpaQuery.exists();
    }

    @Override
    public BooleanExpression notExists() {
        return jpaQuery.exists();
    }

    @Override
    public BooleanExpression lt(Expression<? extends T> expression) {
        return jpaQuery.lt(expression);
    }

    @Override
    public BooleanExpression lt(T t) {
        return jpaQuery.lt(t);
    }

    @Override
    public BooleanExpression gt(Expression<? extends T> expression) {
        return jpaQuery.gt(expression);
    }

    @Override
    public BooleanExpression gt(T t) {
        return jpaQuery.gt(t);
    }

    @Override
    public BooleanExpression loe(Expression<? extends T> expression) {
        return jpaQuery.loe(expression);
    }

    @Override
    public BooleanExpression loe(T t) {
        return jpaQuery.loe(t);
    }

    @Override
    public BooleanExpression goe(Expression<? extends T> expression) {
        return jpaQuery.goe(expression);
    }

    @Override
    public BooleanExpression goe(T t) {
        return jpaQuery.goe(t);
    }

    @Override
    public BooleanOperation isNull() {
        return jpaQuery.isNull();
    }

    @Override
    public BooleanOperation isNotNull() {
        return jpaQuery.isNotNull();
    }

    @Override
    public BooleanExpression in(Collection<? extends T> collection) {
        return jpaQuery.in(collection);
    }

    @Override
    public BooleanExpression in(T... ts) {
        return jpaQuery.in(ts);
    }

    @Override
    public QueryMetadata getMetadata() {
        return jpaQuery.getMetadata();
    }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C c) {
        return jpaQuery.accept(visitor,c);
    }

    @Override
    public Class<? extends T> getType() {
        return jpaQuery.getType();
    }
}
