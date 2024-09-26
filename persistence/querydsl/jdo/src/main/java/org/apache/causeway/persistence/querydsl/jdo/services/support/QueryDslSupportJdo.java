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
package org.apache.causeway.persistence.querydsl.jdo.services.support;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.jdo.PersistenceManager;

import com.querydsl.core.Tuple;
import com.querydsl.core.dml.DeleteClause;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.jdo.JDOQuery;
import com.querydsl.jdo.JDOQueryFactory;

import org.apache.causeway.persistence.querydsl.applib.query.DslQuery;
import org.apache.causeway.persistence.querydsl.applib.services.support.QueryDslSupport;

import org.apache.causeway.persistence.querydsl.jdo.query.DslQueryJdo;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import org.apache.causeway.persistence.jdo.applib.services.JdoSupportService;

@Component
@Primary
public class QueryDslSupportJdo implements QueryDslSupport {

    @Inject protected JdoSupportService jdoSupportService;
    protected CustomizedJdoQueryFactory queryFactory;

    protected CustomizedJdoQueryFactory getQueryFactory(){
        if(queryFactory==null){
            queryFactory = new CustomizedJdoQueryFactory(() -> jdoSupportService.getPersistenceManager());
        }
        return queryFactory;
    }

    class CustomizedJdoQueryFactory extends JDOQueryFactory{
        public CustomizedJdoQueryFactory(Supplier<PersistenceManager> persistenceManager) {
            super(persistenceManager);
        }
        public JDOQuery<?> from(EntityPath<?>... from){
            return this.query().from(from);
        }
    }

    @Override
    public DeleteClause<?> delete(EntityPath<?> path) {
        return getQueryFactory().delete(path);
    }

    @Override
    public <T> DslQuery<T> select(Expression<T> expr) {
        return DslQueryJdo.of(getQueryFactory().select(expr));
    }

    @Override
    public DslQuery<Tuple> select(Expression<?>... exprs) {
        return DslQueryJdo.of(getQueryFactory().select(exprs));
    }

    @Override
    public <T> DslQuery<T> selectDistinct(Expression<T> expr) {
        return DslQueryJdo.of(getQueryFactory().selectDistinct(expr));
    }

    @Override
    public DslQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return DslQueryJdo.of(getQueryFactory().selectDistinct(exprs));
    }

    @Override
    public DslQuery<Integer> selectZero() {
        return DslQueryJdo.of(getQueryFactory().selectZero());
    }

    @Override
    public DslQuery<Integer> selectOne() {
        return DslQueryJdo.of(getQueryFactory().selectOne());
    }

    @Override
    public <T> DslQuery<T> selectFrom(EntityPath<T> expr) {
        return DslQueryJdo.of(getQueryFactory().selectFrom(expr));
    }

    @Override
    public <T> DslQuery<T> from(EntityPath<T> from) {
        return (DslQuery<T>) DslQueryJdo.of(getQueryFactory().from(from));
    }

    @Override
    public <T> DslQuery<T> from(EntityPath<T>... from) {
        return (DslQuery<T>) DslQueryJdo.of(getQueryFactory().from(from));
    }

    @Override
    public DslQuery<?> query() {
        return DslQueryJdo.of(getQueryFactory().query());
    }

}
