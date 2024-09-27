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
package org.apache.causeway.persistence.querydsl.applib.services.repo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.EntityPathBase;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.persistence.querydsl.applib.services.support.QueryDslSupport;
import org.apache.causeway.persistence.querydsl.applib.util.QueryDslUtil;

import org.springframework.lang.Nullable;

/**
 * Provides default implementation and convenience methods for querying a specific entity (hierarchy), using
 * QueryDSL to construct the queries.
 *
 * <p>
 *     Internally, delegates to {@link QueryDslSupport} to actually submit the queries.
 * </p>
 *
 * @param <T> is the entity type
 * @param <Q> is the Q type of the entity
 */
@SuperBuilder
@NoArgsConstructor
public abstract class QueryDslRepository<T extends Comparable, Q extends EntityPathBase<T>> {

    @Inject protected QueryDslSupport queryDslSupport;
    @Inject protected RepositoryService repositoryService;

    /**
     * For testing
     */
    protected QueryDslRepository(
            final QueryDslSupport queryDslSupport,
            final RepositoryService repositoryService) {
        this.queryDslSupport = queryDslSupport;
        this.repositoryService = repositoryService;
    }

    private Class<T> entityClass;
    private Q entityPath;

    /**
     * Default ordering is by <code>id</code>, ascending.
     *
     * <p>
     *     Used as a default by certain methods that require an ordering, eg
     *     {@link #findUniqueUsingDefaultOrder(Function)}.  All such methods have the suffix
     *     &quot;UsingDefaultOrder&quot; in their name.
     * </p>
     *
     * <p>
     *     Repository subclasses should override if they prefer an alternative default ordering of methods.
     * </p>
     *
     * <p>
     *     <B>NOTE</B>: Repository subclasses <i>must</i> override if the entity does not have an <code>id</code> field,
     *     otherwise calls to &quot;findXxxUsingDefaultOrder&quot; methods will fail.
     * </p>
     *
     * @return a list of OrderSpecifiers
     */
    protected List<OrderSpecifier<? extends Comparable>> getDefaultOrders() {
        return newList(QueryDslUtil.ID_ORDER_SPECIFIER);
    }

    private OrderSpecifier<? extends Comparable>[] getDefaultOrder() {
        try {
            return getDefaultOrders() == null ? null : getDefaultOrders().toArray(new OrderSpecifier[0]);
        } catch (Exception e) {
            throw new RuntimeException("Invalid default order!", e);
        }
    }

    /**
     * @return the main entity class for which this repository implementation operates
     */
    public Class<T> getEntityClass() {
        if (entityClass == null) {
            entityClass = getTypeParameter(getClass(), 0);
        }
        return entityClass;
    }

    /**
     * @return the main entity Q instance for which this repository implementation operates
     *
     * @see #entity()
     * @see #q()
     */
    public Q getEntityPath() {
        if (entityPath == null) {
            entityPath = getEntityPathInstance();
        }
        return entityPath;
    }

    /**
     * @return the main entity Q instance for which this repository implementation operates
     *
     * @see #entity()
     * @see #getEntityPath()
     */
    public Q q() {
        return getEntityPath();
    }
    /**
     * @return the main entity Q instance for which this repository implementation operates
     *
     * @see #getEntityPath()
     * @see #q()
     */
    public Q entity() {
        return getEntityPath();
    }

    private Q getEntityPathInstance() {
        Class<Q> qClass = getTypeParameter(getClass(), 1);
        if (qClass == null) {
            throw new RecoverableException("Could not find Q type for this entity");
        }
        Q instance = null;
        try {
            String alias = "e";
            if (getEntityClass() != null) {
                alias = getEntityClass().getSimpleName();
                alias = alias.substring(0, 1).toLowerCase() + alias.substring(1);
            }
            instance = qClass.getConstructor(String.class).newInstance(alias);
        } catch (Exception e) {
            throw new RecoverableException("Could not instantiate Q type " + qClass.getName(), e);
        }
        return instance;
    }


    /**
     * Returns all the instances of this entity, in the preferred {@link OrderSpecifier ordering}.
     *
     * @return all instances of this entity in the preferred {@link OrderSpecifier ordering}.
     *
     * @param orderSpecifiers    the ordering to apply
     *
     * @see #findAll(Function[])
     * @see #findAllUsingDefaultOrder()
     */
    public List<T> findAll(final OrderSpecifier<?>... orderSpecifiers) {
        return queryDslSupport
                .selectFrom(getEntityPath())
                .orderBy(orderSpecifiers)
                .fetch();
    }

    /**
     * Returns all the instances of this entity, in the preferred {@link OrderSpecifier ordering}.
     *
     * <p>
     * As {@link #findAll(OrderSpecifier[])}, but allowing the {@link OrderSpecifier order} to be passed in as a
     * function (just syntax sugar).
     * </p>
     *
     * @param orderSpecifiers    the ordering to apply
     *
     * @return all instances of this entity in the preferred order
     *
     * @see #findAll(OrderSpecifier[])
     * @see #findAllUsingDefaultOrder()
     */
    public List<T> findAll(final Function<Q, OrderSpecifier<?>>... orderSpecifiers) {
        return findAll(apply(orderSpecifiers));
    }

    /**
     * Returns all the instances of this entity, in the {@link #getDefaultOrders() default order}.
     *
     * <p>
     *     NOTE: the default implementation of {@link #getDefaultOrders()} requires that the <code>id</code> field
     *     exists.  If this is not the case, then the method must be overridden.
     * </p>
     *
     * @return all instances of this entity in the {@link #getDefaultOrders() default order}.
     *
     * @see #findAll(OrderSpecifier[])
     * @see #findAll(Function[])
     * @see #getDefaultOrders()
     */
    public List<T> findAllUsingDefaultOrder() {
        return findAll(getDefaultOrder());
    }


    /**
     * Based on the given predicates search for exactly zero or one entity instance.
     *
     * @param predicates the predicates to apply as a filter
     * @return exactly one result or null
     * @throws NonUniqueResultException if there is more than one matching result
     *
     * @see #findUnique(Function)
     */
    public Optional<T> findUnique(
            Predicate... predicates
    ) throws NonUniqueResultException {
        return Optional.ofNullable(queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicates)
                .fetchOne());
    }

    /**
     * Based on the given predicates search for exactly zero or one entity instance.
     *
     * <p>
     *     This is equivalent to {@link #findUnique(Function)}, but bundles up the
     *     predicates as a function (just syntax sugar).
     * </p>
     *
     * @param predicateFunction the predicates to apply as a filter
     *
     * @return exactly one result or null
     * @throws NonUniqueResultException if there is more than one matching result
     *
     * @see #findUnique(Function)
     */
    public Optional<T> findUnique(
            final Function<Q, Predicate> predicateFunction
    ) throws NonUniqueResultException {
        return findUnique(predicateFunction.apply(getEntityPath()));
    }




    /**
     * Based on the given predicates search for the first entity instance based on the
     * {@link #getDefaultOrders() default ordering}.
     *
     * <p>
     *     NOTE: the default implementation of {@link #getDefaultOrders()} requires that the <code>id</code> field
     *     exists.  If this is not the case, then the method must be overridden.
     * </p>
     *
     * @param predicates the predicates to apply as a filter
     * @return the first result or null
     *
     * @see #findAnyUsingDefaultOrder(Function)
     * @see #getDefaultOrder()
     */
    public Optional<T> findAnyUsingDefaultOrder(final Predicate... predicates) {
        return Optional.ofNullable(queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicates)
                .orderBy(getDefaultOrder())
                .fetchFirst());
    }

    /**
     * Based on the given predicate function search for the first entity instance based on the
     * {@link #getDefaultOrders() default ordering}.
     *
     * <p>
     *     This is equivalent to {@link #findAnyUsingDefaultOrder(Predicate...)}, but with the predicates supplied
     *     as a function (syntax sugar).
     * </p>
     *
     * <p>
     *     NOTE: the default implementation of {@link #getDefaultOrders()} requires that the <code>id</code> field
     *     exists.  If this is not the case, then that method must be overridden.
     * </p>
     *
     * @param predicateFunction the predicate function to apply as a filter
     * @return the first result or null
     *
     * @see #findAnyUsingDefaultOrder(Predicate...)
     * @see #getDefaultOrder()
     */
    public Optional<T> findAnyUsingDefaultOrder(Function<Q, Predicate> predicateFunction) {
        return findAnyUsingDefaultOrder(predicateFunction.apply(getEntityPath()));
    }


    /**
     * Based on the given predicates search for the first entity instance based on specified
     * {@link OrderSpecifier ordering}.
     *
     * @param predicates the predicates to apply as a filter
     * @param orderSpecifier    the ordering to apply
     *
     * @return the first result or null
     *
     * @see #findFirst(Function, OrderSpecifier[])
     * @see #findFirst(Function, Function[])
     */
    public Optional<T> findFirst(
            final Predicate[] predicates,
            final OrderSpecifier<?>... orderSpecifier) {
        return Optional.ofNullable(queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicates)
                .orderBy(orderSpecifier)
                .fetchFirst());
    }

    /**
     * Based on the given predicate function search for the first entity instance based on specified
     * {@link OrderSpecifier ordering}.
     *
     * <p>
     *     This is equivalent to {@link #findFirst(Predicate[], OrderSpecifier[])}, but with the predicates supplied
     *     as a function (syntax sugar).
     * </p>
     *
     * @param predicateFunction the predicate function to apply as a filter
     * @param orderSpecifier    the ordering to apply
     *
     * @return the first result or null
     *
     * @see #findFirst(Predicate[], OrderSpecifier[])
     * @see #findFirst(Function, Function[])
     */
    public Optional<T> findFirst(
            final Function<Q, Predicate> predicateFunction,
            final OrderSpecifier<?>... orderSpecifier) {
        return findFirst(new Predicate[]{predicateFunction.apply(getEntityPath())}, orderSpecifier);
    }

    /**
     * Based on the given predicate function search for the first entity instance based on specified
     * {@link OrderSpecifier ordering}.
     *
     * <p>
     *     This is equivalent to {@link #findFirst(Predicate[], OrderSpecifier[])}, but with the predicate and
     *     ordering supplied as a function (syntax sugar).
     * </p>
     *
     * @param predicateFunction the predicate function to apply as a filter
     * @param orderSpecifier    the ordering to apply
     *
     * @return the first result or null
     *
     * @see #findFirst(Function, Function[])
     * @see #findFirst(Function, OrderSpecifier[])
     */
    public Optional<T> findFirst(
            final Function<Q, Predicate> predicateFunction,
            final Function<Q, OrderSpecifier<?>>... orderSpecifier) {
        return findFirst(new Predicate[]{predicateFunction.apply(getEntityPath())}, apply(orderSpecifier));
    }


    /**
     * Based on the given predicate function search for applicable entity instances and return the distinct projection
     * (a subset of fields).
     *
     * <p>
     * <b>CAUTION</b>: when the supplied {@link OrderSpecifier ordering} is not aligned to the projection one might get
     * unexpected results, because the elimination of duplicates is based on the ordering, not the projection!
     * </p>
     *
     * @param projection        the projection that defines the field to return
     * @param predicateFunction the predicate function to apply as a filter (can be null).
     * @param orderSpecifier    the ordering to apply; make sure aligns with the projection.
     *
     * @return the projection result ordered as specified
     *
     * @see #findFieldsDistinct(Expression, Function, OrderSpecifier[])
     * @see #findFieldsDistinctUsingDefaultOrder(Expression, Predicate...)
     */
    public <F> List<F> findFieldsDistinct(
            final Expression<F> projection,
            @Nullable final Function<Q, Predicate> predicateFunction,
            final OrderSpecifier<?>... orderSpecifier) {
        return queryDslSupport
                .select(projection)
                .distinct()
                .from(getEntityPath())
                .where(predicateFunction == null ? new BooleanBuilder() : predicateFunction.apply(getEntityPath()))
                .orderBy(orderSpecifier)
                .fetch();
    }

    /**
     * Based on the given predicate function search for applicable entity instances and return the distinct projection
     * (a subset of fields).
     *
     * <p>
     *     Same as {@link #findFieldsDistinct(Expression, Function, OrderSpecifier[])}, but with the
     *     {@link OrderSpecifier ordering} bundled up as a function (syntax sugar).
     * </p>
     *
     * <p>
     * <b>CAUTION</b>: when the supplied {@link OrderSpecifier ordering} is not aligned to the projection one might get
     * unexpected results, because the elimination of duplicates is based on the ordering, not the projection!
     * </p>
     *
     * @param projection        the projection that defines the field to return
     * @param predicateFunction the predicate function to apply as a filter (can be null).
     * @param orderSpecifier    the ordering to apply; make sure aligns with the projection.
     *
     * @return the projection result ordered as specified
     *
     * @see #findFieldsDistinct(Expression, Function, OrderSpecifier[])
     * @see #findFieldsDistinctUsingDefaultOrder(Expression, Predicate...)
     */
    public <F> List<F> findFieldsDistinct(
            final Expression<F> projection,
            @Nullable final Function<Q, Predicate> predicateFunction,
            final Function<Q, OrderSpecifier<?>>... orderSpecifier) {
        return findFieldsDistinct(projection, predicateFunction, apply(orderSpecifier));
    }

    /**
     * Based on the given predicates search for applicable entity instances and return the distinct projection,
     * using the {@link #getDefaultOrders() default order}.
     *
     * <p>
     * <b>CAUTION</b>: when the {@link #getDefaultOrders() ordering} is not aligned to the projection one might get
     * unexpected results, because the elimination of duplicates is based on the ordering, not the projection!
     * </p>
     *
     * <p>
     *     NOTE: the default implementation of {@link #getDefaultOrders()} requires that the <code>id</code> field
     *     exists.  If this is not the case, then that method must be overridden.
     * </p>
     *
     * @param projection the projection that defines the field to return
     * @param predicates the predicates to apply as a filter
     * @return the ordered projection result
     *
     * @see #findFieldsDistinct(Expression, Function, OrderSpecifier[])
     * @see #findFieldsDistinct(Expression, Function, Function[])
     * @see #findFieldsDistinctUsingDefaultOrder(Expression, Function)
     */
    public <F> List<F> findFieldsDistinctUsingDefaultOrder(
            final Expression<F> projection,
            final Predicate... predicates
    ) {
        return queryDslSupport
                .select(projection)
                .distinct()
                .from(getEntityPath())
                .where(predicates)
                .orderBy(getDefaultOrder())
                .fetch();
    }

    /**
     * Based on the given predicate function search for applicable entity instances and return the distinct projection,
     * using the {@link #getDefaultOrders() default ordering}.
     *
     * <p>
     *     This is the same as {@link #findFieldsDistinctUsingDefaultOrder(Expression, Predicate...)}, but allowing the
     *     predicates to be provided as a function (syntax sugar).
     * </p>
     *
     * <p>
     * <b>CAUTION</b>: when the {@link #getDefaultOrders() ordering} is not aligned to the projection one might get
     * unexpected results, because the elimination of duplicates is based on the ordering, not the projection!
     * </p>
     *
     * <p>
     *     NOTE: the default implementation of {@link #getDefaultOrders()} requires that the <code>id</code> field
     *     exists.  If this is not the case, then that method must be overridden.
     * </p>
     *
     * @param projection        the projection that defines the field to return
     * @param predicateFunction the predicate function to apply as a filter
     * @return the ordered projection result
     *
     * @see #findFieldsDistinct(Expression, Function, OrderSpecifier[])
     * @see #findFieldsDistinctUsingDefaultOrder(Expression, Predicate...)
     */
    public <F> List<F> findFieldsDistinctUsingDefaultOrder(
            final Expression<F> projection,
            final Function<Q, Predicate> predicateFunction
    ) {
        return findFieldsDistinct(projection, predicateFunction, getDefaultOrder());
    }


    /**
     * Based on the given predicate search for applicable entity instances and return the first projection
     * based on specified {@link OrderSpecifier ordering}.
     *
     * <p>
     * <b>CAUTION</b>: when the supplied {@link OrderSpecifier ordering} is not aligned to the projection one might get
     * unexpected results, because the elimination of duplicates is based on the ordering, not the projection!
     * </p>
     *
     * @param projection     the projection that defines the field to return
     * @param predicate      the predicate to apply as a filter
     * @param orderSpecifiers the ordering to apply
     * @return the first projection result based on the given order
     *
     * @see #findFirstFields(Expression, Function, OrderSpecifier[])
     */
    public <F> Optional<F> findFirstFields(
            final Expression<F> projection,
            @Nullable final Predicate predicate,
            final OrderSpecifier<?>... orderSpecifiers
    ) {
        return Optional.ofNullable(queryDslSupport
                .select(projection)
                .distinct()
                .from(getEntityPath())
                .where(predicate == null ? new BooleanBuilder() : predicate)
                .orderBy(orderSpecifiers)
                .fetchFirst());
    }


    /**
     * Based on the given predicate function search for applicable entity instances and return the first projection
     * based on specified {@link OrderSpecifier ordering}.
     *
     * <p>
     *     Same as {@link #findFirstFields(Expression, Predicate, OrderSpecifier[])}, but with the predicates
     *     bundled up as a function (syntax sugar).
     * </p>
     *
     * <p>
     * <b>CAUTION</b>: when the supplied {@link OrderSpecifier ordering} is not aligned to the projection one might get
     * unexpected results, because the elimination of duplicates is based on the ordering, not the projection!
     * </p>
     *
     * @param projection        the projection that defines the field to return
     * @param predicateFunction the predicate function to apply as a filter
     * @param orderSpecifier    the ordering to apply
     * @return the first projection result based on the given order
     *
     * @see #findFirstFields(Expression, Function, OrderSpecifier[])
     * @see #findFirstFields(Expression, Function, Function[])
     */
    public <F> Optional<F> findFirstFields(
            final Expression<F> projection,
            @Nullable final Function<Q, Predicate> predicateFunction,
            final OrderSpecifier<?>... orderSpecifier
    ) {
        return findFirstFields(
                projection,
                predicateFunction == null ? null : predicateFunction.apply(getEntityPath()),
                orderSpecifier
        );
    }

    /**
     * Based on the given predicate function search for applicable entity instances and return the first projection
     * based on specified {@link OrderSpecifier ordering}.
     *
     * <p>
     *     Same as {@link #findFirstFields(Expression, Predicate, OrderSpecifier[])}, but with the predicates
     *     and order specifiers both bundled up as a function (syntax sugar).
     * </p>
     *
     * <p>
     * <b>CAUTION</b>: when the supplied {@link OrderSpecifier ordering} is not aligned to the projection one might get
     * unexpected results, because the elimination of duplicates is based on the ordering, not the projection!
     * </p>
     *
     * @param projection        the projection that defines the field to return
     * @param predicateFunction the predicate function to apply as a filter
     * @param orderSpecifiers    the ordering to apply
     * @return the first projection result based on the given order
     *
     * @see #findFirstFields(Expression, Predicate, OrderSpecifier[])
     * @see #findFirstFields(Expression, Function, OrderSpecifier[])
     * @see #findFirstFieldUsingDefaultOrder(Expression, Function)
     */
    public <F> Optional<F> findFirstFields(
            final Expression<F> projection,
            @Nullable final Function<Q, Predicate> predicateFunction,
            final Function<Q, OrderSpecifier<?>>... orderSpecifiers
    ) {
        return findFirstFields(
                projection,
                predicateFunction,
                apply(orderSpecifiers)
        );
    }

    /**
     * Based on the given predicate function search for applicable entity instances and return the first projection
     * based on the {@link #getDefaultOrders() default ordering}.
     *
     * <p>
     * <b>CAUTION</b>: when the {@link #getDefaultOrders() ordering} is not aligned to the projection one might get
     * unexpected results, because the elimination of duplicates is based on the ordering, not the projection!
     * </p>
     *
     * <p>
     *     NOTE: the default implementation of {@link #getDefaultOrders()} requires that the <code>id</code> field
     *     exists.  If this is not the case, then that method must be overridden.
     * </p>
     *
     * @param projection        the projection that defines the field to return
     * @param predicateFunction the predicate function to apply as a filter
     * @return the first projection result based on the default order
     *
     * @see #findFirstFields(Expression, Predicate, OrderSpecifier[])
     * @see #findFirstFields(Expression, Function, OrderSpecifier[])
     * @see #findFirstFields(Expression, Function, Function[])
     * @see #getDefaultOrders()
     */
    public <F> Optional<F> findFirstFieldUsingDefaultOrder(
            final Expression<F> projection,
            final Function<Q, Predicate> predicateFunction
    ) {
        return findFirstFields(projection, predicateFunction, getDefaultOrder());
    }



    /**
     * Based on the given predicates search for exactly one or zero entity instance and return the projection.
     *
     * @param projection the projection that defines the field to return
     * @param predicates the predicates to apply as a filter
     * @return the projection result
     * @throws NonUniqueResultException if there is more than one matching result
     *
     * @see #findUniqueFields(Expression, Function)
     */
    public <F> Optional<F> findUniqueFields(
            final Expression<F> projection,
            final Predicate... predicates) throws NonUniqueResultException {
        return Optional.ofNullable(queryDslSupport
                .select(projection)
                .from(getEntityPath())
                .where(predicates)
                .fetchOne());
    }

    /**
     * Based on the given predicate function search for exactly one or zero entity instance and return the projection.
     *
     * @param projection        the projection that defines the field to return
     * @param predicateFunction the predicate function to apply as a filter
     * @return the projection result
     * @throws NonUniqueResultException if there is more than one matching result
     *
     * @see #findUniqueFields(Expression, Predicate...)
     */
    public <F> Optional<F> findUniqueFields(
            final Expression<F> projection,
            final Function<Q, Predicate> predicateFunction) throws NonUniqueResultException {
        return Optional.ofNullable(queryDslSupport
                .select(projection)
                .from(getEntityPath())
                .where(predicateFunction.apply(getEntityPath()))
                .fetchOne());
    }


    /**
     * Based on the given predicate search for applicable entity instances and apply the given
     * {@link OrderSpecifier ordering}.
     *
     * @param predicate      the predicate to apply as a filter
     * @param orderSpecifier the ordering to apply
     * @return the ordered instances
     *
     * @see #find(Function, OrderSpecifier[])
     * @see #find(Function, Function[])
     * @see #findUsingDefaultOrder(Predicate...)
     * @see #findUsingDefaultOrder(Function)
     */
    public List<T> find(
            final Predicate predicate,
            final OrderSpecifier<?>... orderSpecifier
    ) {
        return queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicate)
                .orderBy(orderSpecifier)
                .fetch();
    }

    /**
     * Based on the given predicate function search for applicable entity instances and apply the given
     * {@link OrderSpecifier ordering}.
     *
     * <p>
     *     Same as {@link #find(Predicate, OrderSpecifier[])}, but with the predicate bundled up as
     *     a function (syntax sugar).
     * </p>
     *
     * @param predicateFunction the predicate function to apply as a filter
     * @param orderSpecifier    the ordering to apply
     * @return the ordered instances
     *
     * @see #find(Predicate, OrderSpecifier[])
     * @see #find(Function, Function[])
     * @see #findUsingDefaultOrder(Predicate...)
     * @see #findUsingDefaultOrder(Function)
     */
    public List<T> find(
            final Function<Q, Predicate> predicateFunction,
            final OrderSpecifier<?>... orderSpecifier
    ) {
        return queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicateFunction.apply(getEntityPath()))
                .orderBy(orderSpecifier)
                .fetch();
    }

    /**
     * Based on the given predicate function search for applicable entity instances and apply the given
     * {@link OrderSpecifier ordering}.
     *
     * <p>
     *     Same as {@link #find(Predicate, OrderSpecifier[])}, but with the predicate and ordering bundled up as
     *     a function (syntax sugar).
     * </p>
     *
     * @param predicateFunction the predicate function to apply as a filter
     * @param orderSpecifier    the ordering to apply
     * @return the ordered instances
     *
     * @see #find(Predicate, OrderSpecifier[])
     * @see #find(Function, OrderSpecifier[])
     * @see #findUsingDefaultOrder(Predicate...)
     * @see #findUsingDefaultOrder(Function)
     */
    public List<T> find(
            final Function<Q, Predicate> predicateFunction,
            final Function<Q, OrderSpecifier<?>>... orderSpecifier
    ) {
        return find(predicateFunction, apply(orderSpecifier));
    }


    /**
     * Based on the given predicates search for applicable entity instances and apply the
     * {@link #getDefaultOrders() default ordering}.
     *
     * <p>
     *     NOTE: the default implementation of {@link #getDefaultOrders()} requires that the <code>id</code> field
     *     exists.  If this is not the case, then that method must be overridden.
     * </p>
     *
     * @param predicates the predicates to apply as a filter
     * @return the ordered instances
     *
     * @see #find(Predicate, OrderSpecifier[])
     * @see #find(Function, OrderSpecifier[])
     * @see #find(Function, Function[])
     * @see #findUsingDefaultOrder(Function)
     */
    public List<T> findUsingDefaultOrder(Predicate... predicates) {
        return queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicates)
                .orderBy(getDefaultOrder())
                .fetch();
    }

    /**
     * Based on the given predicate function search for applicable entity instances and apply the
     * {@link #getDefaultOrders() default ordering}.
     *
     * <p>
     *     Same as {@link #findUsingDefaultOrder(Predicate...)}, but with the predicate bundled up as a function
     *     (syntax sugar).
     * </p>
     *
     * <p>
     *     NOTE: the default implementation of {@link #getDefaultOrders()} requires that the <code>id</code> field
     *     exists.  If this is not the case, then that method must be overridden.
     * </p>
     *
     * @param predicateFunction the predicate function to apply as a filter
     * @return the ordered instances
     *
     * @see #find(Predicate, OrderSpecifier[])
     * @see #find(Function, OrderSpecifier[])
     * @see #find(Function, Function[])
     * @see #findUsingDefaultOrder(Predicate...)
     */
    public List<T> findUsingDefaultOrder(Function<Q, Predicate> predicateFunction) {
        return find(predicateFunction, getDefaultOrder());
    }



    /**
     * Based on the given predicate search for applicable entity instances,
     * then transform the results to the given bean using the given projections and apply the given
     * {@link OrderSpecifier ordering}.
     *
     * @param predicate      the predicate to apply as a filter
     * @param orderSpecifier the ordering to apply
     * @param bean           the bean class to use as a destination for the projection results
     * @param projections    the projections which define which information to deliver to the bean creation
     * @param <B>            the bean type
     * @return the information defined by the projections as ordered beans
     */
    public <B> List<B> findAsBean(
            final Predicate predicate,
            final OrderSpecifier<?> orderSpecifier,
            final Class<? extends B> bean,
            final Expression<?>... projections) {
        return (List<B>) queryDslSupport
                .from(getEntityPath())
                .projection(Projections.bean(bean, projections))
                .where(predicate)
                .orderBy(orderSpecifier)
                .fetch();
    }

    /**
     * Based on the given predicate function search for applicable entity instances,
     * then transform the results to the given bean using the given projections and apply the given ordering.
     *
     * <p>
     *     Same as {@link #findAsBean(Predicate, OrderSpecifier, Class, Expression[])}, but with the predicate
     * </p>
     *
     * @param predicateFunction the predicate function to apply as a filter
     * @param orderSpecifier    the ordering to apply
     * @param bean              the bean class to use as a destination for the projection results
     * @param projections       the projections which define which information to deliver to the bean creation
     * @param <B>               the bean type
     * @return the information defined by the projections as ordered beans
     */
    public <B> List<B> findAsBean(
            final Function<Q, Predicate> predicateFunction,
            final OrderSpecifier<?> orderSpecifier,
            final Class<? extends B> bean,
            final Expression<?>... projections) {
        return (List<B>) queryDslSupport
                .from(getEntityPath())
                .projection(Projections.bean(bean, projections))
                .where(predicateFunction.apply(getEntityPath()))
                .orderBy(orderSpecifier)
                .fetch();
    }


    private OrderSpecifier<?>[] apply(Function<Q, OrderSpecifier<?>>... orderSpecifiers) {
        return Arrays.stream(orderSpecifiers)
                .map(x -> x.apply(getEntityPath()))
                .collect(Collectors.toUnmodifiableList())
                .toArray(new OrderSpecifier[0]);
    }

    private static <T> Class<T> getTypeParameter(Class<?> parameterizedType, int index){
        if(parameterizedType==null) return null;

        ParameterizedType pType= (ParameterizedType) parameterizedType.getGenericSuperclass();
        if(pType==null) return null;

        Type[] types= pType.getActualTypeArguments();
        if(types==null || types.length<=index || types[index] instanceof ParameterizedType) return null;

        return (Class<T>) types[index];
    }

    static <T> List<T> newList(T... objs) {
        return newArrayList(objs);
    }

    static <T> ArrayList<T> newArrayList(T... objs) {
        ArrayList<T> result = new ArrayList();
        Collections.addAll(result, objs);
        return result;
    }


}