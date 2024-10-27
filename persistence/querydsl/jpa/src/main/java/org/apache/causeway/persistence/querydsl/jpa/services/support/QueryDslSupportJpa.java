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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.core.Tuple;
import com.querydsl.core.dml.DeleteClause;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import org.apache.causeway.persistence.querydsl.applib.query.DslQuery;
import org.apache.causeway.persistence.querydsl.applib.services.support.QueryDslSupport;

@Component
@ConditionalOnMissingBean(QueryDslSupport.class)
public class QueryDslSupportJpa implements QueryDslSupport {

    @PersistenceContext EntityManager entityManager;

    protected JPAQueryFactory queryFactory;

    protected JPAQueryFactory getQueryFactory(){
        if(queryFactory==null){
            queryFactory = new JPAQueryFactory(() -> entityManager);
        }
        return queryFactory;
    }

    @Override
    public DeleteClause<?> delete(EntityPath<?> path) {
        return getQueryFactory().delete(path);
    }

    @Override
    public <T> DslQuery<T> select(Expression<T> expr) {
        return DslQueryJpa.of(getQueryFactory().select(expr));
    }

    @Override
    public DslQuery<Tuple> select(Expression<?>... exprs) {
        return DslQueryJpa.of(getQueryFactory().select(exprs));
    }

    @Override
    public <T> DslQuery<T> selectDistinct(Expression<T> expr) {
        return DslQueryJpa.of(getQueryFactory().selectDistinct(expr));
    }

    @Override
    public DslQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return DslQueryJpa.of(getQueryFactory().selectDistinct(exprs));
    }

    @Override
    public DslQuery<Integer> selectZero() {
        return DslQueryJpa.of(getQueryFactory().selectZero());
    }

    @Override
    public DslQuery<Integer> selectOne() {
        return DslQueryJpa.of(getQueryFactory().selectOne());
    }

    @Override
    public <T> DslQuery<T> selectFrom(EntityPath<T> expr) {
        return DslQueryJpa.of(getQueryFactory().selectFrom(expr));
    }

    @Override
    public <T> DslQuery<T> from(EntityPath<T> from) {
        return (DslQuery<T>) DslQueryJpa.of(getQueryFactory().from(from));
    }

    @Override
    public <T> DslQuery<T> from(EntityPath<T>... from) {
        return (DslQuery<T>) DslQueryJpa.of(getQueryFactory().from(from));
    }

    @Override
    public DslQuery<?> query() {
        return DslQueryJpa.of(getQueryFactory().query());
    }
}
