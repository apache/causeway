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
package org.apache.causeway.persistence.querydsl.jpa.services.support;

import java.util.function.Supplier;

import jakarta.persistence.EntityManager;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAInsertClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;

/**
 * Jakarta edition of {@link JPAQueryFactory}
 * @see com.querydsl.jpa.impl.JPAQueryFactory
 */
class JPAQueryFactoryJakartaEdition implements JPQLQueryFactory {

    private final JPQLTemplates templates;

    private final Supplier<EntityManager> entityManager;

    public JPAQueryFactoryJakartaEdition(final EntityManager entityManager) {
        this.entityManager = () -> entityManager;
        this.templates = null;
    }

//not used
//    public JPAQueryFactoryJakartaEdition(final JPQLTemplates templates, final EntityManager entityManager) {
//        this.entityManager = () -> entityManager;
//        this.templates = templates;
//    }
//
//    public JPAQueryFactoryJakartaEdition(final Supplier<EntityManager> entityManager) {
//        this.entityManager = entityManager;
//        this.templates = null;
//    }
//
//    public JPAQueryFactoryJakartaEdition(final JPQLTemplates templates, final Supplier<EntityManager> entityManager) {
//        this.entityManager = entityManager;
//        this.templates = templates;
//    }

    @Override
    public JPADeleteClause delete(final EntityPath<?> path) {
        //FIXME[CAUSEWAY-3800] delete not yet implemented for jakarta edition
        throw new UnsupportedOperationException("not yet available for jakarta edition");
//        if (templates != null) {
//            return new JPADeleteClause(entityManager.get(), path, templates);
//        } else {
//            return new JPADeleteClause(entityManager.get(), path);
//        }
    }

    @Override
    public <T> JPAQuery<T> select(final Expression<T> expr) {
        return query().select(expr);
    }

    @Override
    public JPAQuery<Tuple> select(final Expression<?>... exprs) {
        return query().select(exprs);
    }

    @Override
    public <T> JPAQuery<T> selectDistinct(final Expression<T> expr) {
        return select(expr).distinct();
    }

    @Override
    public JPAQuery<Tuple> selectDistinct(final Expression<?>... exprs) {
        return select(exprs).distinct();
    }

    @Override
    public JPAQuery<Integer> selectOne() {
        return select(Expressions.ONE);
    }

    @Override
    public JPAQuery<Integer> selectZero() {
        return select(Expressions.ZERO);
    }

    @Override
    public <T> JPAQuery<T> selectFrom(final EntityPath<T> from) {
        return select(from).from(from);
    }

    @Override
    public JPAQuery<?> from(final EntityPath<?> from) {
        return query().from(from);
    }

    @Override
    public JPAQuery<?> from(final EntityPath<?>... from) {
        return query().from(from);
    }

    @Override
    public JPAUpdateClause update(final EntityPath<?> path) {
        //FIXME[CAUSEWAY-3800] delete not yet implemented for jakarta edition
        throw new UnsupportedOperationException("not yet available for jakarta edition");
//        if (templates != null) {
//            return new JPAUpdateClause(entityManager.get(), path, templates);
//        } else {
//            return new JPAUpdateClause(entityManager.get(), path);
//        }
    }

    @Override
    public JPAInsertClause insert(final EntityPath<?> path) {
        //FIXME[CAUSEWAY-3800] delete not yet implemented for jakarta edition
        throw new UnsupportedOperationException("not yet available for jakarta edition");
//        if (templates != null) {
//            return new JPAInsertClause(entityManager.get(), path, templates);
//        } else {
//            return new JPAInsertClause(entityManager.get(), path);
//        }
    }

    @Override
    public JPAQuery<?> query() {
        //FIXME[CAUSEWAY-3800] delete not yet implemented for jakarta edition
        throw new UnsupportedOperationException("not yet available for jakarta edition");
//        if (templates != null) {
//            return new JPAQuery<Void>(entityManager.get(), templates);
//        } else {
//            return new JPAQuery<Void>(entityManager.get());
//        }
    }
}
