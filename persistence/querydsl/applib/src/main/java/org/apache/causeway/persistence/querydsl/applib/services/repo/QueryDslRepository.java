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
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.EntityPathBase;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.val;

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.persistence.querydsl.applib.services.support.QueryDslSupport;
import org.apache.causeway.persistence.querydsl.applib.util.DslExpressions;

import org.springframework.lang.Nullable;

/**
 * Provides default implementation and convenience methods for querying a specific entity (hierarchy), using
 * QueryDSL to construct the queries.
 *
 * <p>
 *     Internally, delegates to {@link QueryDslSupport} to actually submit the queries.
 * </p>
 *
 * @since 2.1 {@index}
 *
 * @param <T> is the entity type
 * @param <Q> is the Q type of the entity
 */
@SuperBuilder
@NoArgsConstructor
public abstract class QueryDslRepository<T extends Comparable, Q extends EntityPathBase<T>> {

    public static final OrderSpecifier<Comparable> ID_ORDER_SPECIFIER = new OrderSpecifier<>(Order.ASC, DslExpressions.constant("id"));

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
     *     {@link #findAnyUsingDefaultOrder(Function...)} or
     *     {@link #findFieldsDistinctUsingDefaultOrder(Function, Function...)}.  All such methods have the suffix
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
     * @return a list of {@link OrderSpecifier}s
     *
     * @see #findUsingDefaultOrder(Function[])
     * @see #findAllUsingDefaultOrder()
     * @see #findAnyUsingDefaultOrder(Function...)
     * @see #findFieldsDistinctUsingDefaultOrder(Function, Function...)
     */
    protected Function<Q, List<OrderSpecifier<? extends Comparable>>> getDefaultOrders() {
        return entity -> newList(ID_ORDER_SPECIFIER);
    }

    private Function<Q, OrderSpecifier<?>>[] getDefaultOrdersAsArray() {
        return getDefaultOrders().apply(getEntityPath())
                .stream()
                .map(orderSpecifier -> (Function<Q, OrderSpecifier<?>>) q -> orderSpecifier)
                .collect(Collectors.toList())
                .toArray(new Function[]{});
    }

    private OrderSpecifier<? extends Comparable>[] getDefaultOrdersUnwrapped() {
        try {
            val defaultOrdersFunc = getDefaultOrders();
            val defaultOrders = defaultOrdersFunc.apply(entity());
            return defaultOrders == null ? null : defaultOrders.toArray(new OrderSpecifier[0]);
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
     * The main entity Q instance for which this repository implementation operates.
     *
     * <p>
     *     The default implementation instantiates this reflectively, based on the conventions of the query-dsl
     *     annotation processor; eg <code>QCustomer</code> has a 1-arg string constructor.  In most cases there is
     *     no reason to override this.
     * </p>
     *
     * <p>
     *     However, for some complicated orderings, we have found it necessary to instead use the Q instance created
     *     by the annotation processor, called eg <code>QCustomer.customer</code>.  It is perfectly acceptable to
     *     override this method and just return the appropriate for the entity in question.
     * </p>
     *
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
        try {
            String alias = "e";
            if (getEntityClass() != null) {
                alias = getEntityClass().getSimpleName();
                alias = alias.substring(0, 1).toLowerCase() + alias.substring(1);
            }
            return qClass.getConstructor(String.class).newInstance(alias);
        } catch (Exception e) {
            throw new RecoverableException("Could not instantiate Q type " + qClass.getName(), e);
        }
    }

    /**
     * Returns all the instances of this entity, in the preferred {@link OrderSpecifier ordering}.
     *
     * @param orderSpecifiers    the ordering to apply
     *
     * @return all instances of this entity in the preferred order
     *
     * @see #findAllUsingDefaultOrder()
     */
    public List<T> findAll(final Function<Q, OrderSpecifier<?>>... orderSpecifiers) {
        return queryDslSupport
                .selectFrom(getEntityPath())
                .orderBy(unwrapOrderSpecifiers(orderSpecifiers))
                .fetch();
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
     * @see #findAll(Function[])
     * @see #getDefaultOrders()
     */
    public List<T> findAllUsingDefaultOrder() {
        return findAll(getDefaultOrdersAsArray());
    }

    /**
     * Based on the given predicate(s), search for exactly zero or one entity instance.
     *
     * @param predicates the predicates to apply as a filter
     *
     * @return exactly one result or null
     * @throws NonUniqueResultException if there is more than one matching result
     */
    public Optional<T> findUnique(
            final Function<Q, Predicate>... predicates
    ) throws NonUniqueResultException {
        return Optional.ofNullable(queryDslSupport
                .selectFrom(getEntityPath())
                .where(unwrapPredicates(predicates))
                .fetchOne());
    }

    /**
     * Based on the given predicates search for the first entity instance based on the provided ordering.
     *
     * @param predicates the predicate(s) to apply as a filter (can be null)
     * @return the first result or null
     *
     * @see #findAny(Function, Function[])
     * @see #findAnyUsingDefaultOrder(Function...)
     */
    public Optional<T> findAny(
            @Nullable final Function<Q, Predicate>[] predicates,
            final Function<Q, OrderSpecifier<?>>... orderSpecifiers
    ) {
        return Optional.ofNullable(queryDslSupport
                .selectFrom(getEntityPath())
                .where(unwrapPredicates(predicates))
                .orderBy(unwrapOrderSpecifiers(orderSpecifiers))
                .fetchFirst());
    }

    /**
     * Based on the given predicate, search for the first entity instance based on the provided ordering.
     *
     * @param predicate      the predicate to apply as a filter (can be null)
     *
     * @return the first result or null
     *
     * @see #findAny(Function[], Function[])
     * @see #findAnyUsingDefaultOrder(Function...)
     */
    public Optional<T> findAny(
            final Function<Q, Predicate> predicate,
            final Function<Q, OrderSpecifier<?>>... orderSpecifiers
    ) {
        return findAny(asArray(predicate), orderSpecifiers);
    }

    /**
     * Based on the given predicate function search for the first entity instance based on the
     * {@link #getDefaultOrders() default ordering}.
     *
     * <p>
     *     NOTE: the default implementation of {@link #getDefaultOrders()} requires that the <code>id</code> field
     *     exists.  If this is not the case, then that method must be overridden.
     * </p>
     *
     * @param predicates the predicate(s) to apply as a filter
     * @return the first result or null
     *
     * @see #findAny(Function[], Function[])
     * @see #findAny(Function, Function[])
     * @see #getDefaultOrdersUnwrapped()
     */
    public Optional<T> findAnyUsingDefaultOrder(final Function<Q, Predicate>... predicates) {
        return findAny(predicates, getDefaultOrdersAsArray());
    }

    /**
     * Based on the given predicate(s), search for the first entity instance based on specified
     * {@link OrderSpecifier ordering}.
     *
     * @param predicates        the predicate(s) to apply as a filter (can be null)
     * @param orderSpecifiers   the ordering to apply
     *
     * @return the first result or null
     *
     * @see #findFirstUsingDefaultOrder(Function[]) (Predicate[], OrderSpecifier[])
     * @see #findFirst(Function, Function[])
     */
    public Optional<T> findFirst(
            @Nullable final Function<Q, Predicate>[] predicates,
            final Function<Q, OrderSpecifier<?>>... orderSpecifiers
    ) {
        return Optional.ofNullable(queryDslSupport
                .selectFrom(getEntityPath())
                .where(unwrapPredicates(predicates))
                .orderBy(unwrapOrderSpecifiers(orderSpecifiers))
                .fetchFirst());
    }

    /**
     * Based on the given predicate, search for the first entity instance based on specified
     * {@link OrderSpecifier ordering}.
     *
     * @param predicate        the predicate(s) to apply as a filter
     * @param orderSpecifiers   the ordering to apply
     *
     * @return the first result or null
     *
     * @see #findFirstUsingDefaultOrder(Function[]) (Predicate[], OrderSpecifier[])
     * @see #findFirst(Function[], Function[])
     */
    public Optional<T> findFirst(
            final Function<Q, Predicate> predicate,
            final Function<Q, OrderSpecifier<?>>... orderSpecifiers) {
        return findFirst(asArray(predicate), orderSpecifiers);
    }

    /**
     * Based on the given predicate(s) search for the first entity instance based on specified
     * {@link OrderSpecifier ordering}.
     *
     * @param predicates the predicate(s) to apply as a filter
     *
     * @return the first result or null
     *
s     * @see #findFirst(Function[], Function[])
     * @see #findFirst(Function, Function[])
     */
    public Optional<T> findFirstUsingDefaultOrder(
            final Function<Q, Predicate>... predicates) {
        return findFirst(predicates, getDefaultOrdersAsArray());
    }

    /**
     * Based on the given predicate(s), search for applicable entity instances and return the distinct projection
     * (a subset of fields).
     *
     * <p>
     * <b>CAUTION</b>: when the supplied {@link OrderSpecifier ordering} is not aligned to the projection one might get
     * unexpected results, because the elimination of duplicates is based on the ordering, not the projection!
     * </p>
     *
     * @param projection        the projection that defines the field to return
     * @param predicates        the predicate(s) to apply as a filter (can be null)
     * @param orderSpecifiers   the ordering to apply; make sure aligns with the projection.
     *
     * @return the projection result ordered as specified
     *
     * @see #findFieldsDistinct(Function, Function[], Function[])
     * @see #findFieldsDistinctUsingDefaultOrder(Function, Function...)
     */
    public <F> List<F> findFieldsDistinct(
            final Function<Q, Expression<F>> projection,
            @Nullable final Function<Q, Predicate>[] predicates,
            final Function<Q, OrderSpecifier<?>>... orderSpecifiers) {
        return queryDslSupport
                .select(projection.apply(getEntityPath()))
                .distinct()
                .from(getEntityPath())
                .where(unwrapPredicates(predicates))
                .orderBy(unwrapOrderSpecifiers(orderSpecifiers))
                .fetch();
    }

    /**
     * Based on the given predicate, search for applicable entity instances and return the distinct projection
     * (a subset of fields).
     *
     * <p>
     * <b>CAUTION</b>: when the supplied {@link OrderSpecifier ordering} is not aligned to the projection one might get
     * unexpected results, because the elimination of duplicates is based on the ordering, not the projection!
     * </p>
     *
     * @param projection        the projection that defines the field to return
     * @param predicate the predicate function to apply as a filter (can be null).
     * @param orderSpecifiers    the ordering to apply; make sure aligns with the projection.
     *
     * @return the projection result ordered as specified
     *
     * @see #findFieldsDistinct(Function, Function[], Function[])
     * @see #findFieldsDistinctUsingDefaultOrder(Function, Function...)
     */
    public <F> List<F> findFieldsDistinct(
            final Function<Q, Expression<F>> projection,
            @Nullable final Function<Q, Predicate> predicate,
            final Function<Q, OrderSpecifier<?>>... orderSpecifiers) {
        return findFieldsDistinct(projection, asArray(predicate), orderSpecifiers);
    }

    /**
     * Based on the given predicate function search for applicable entity instances and return the distinct projection,
     * using the {@link #getDefaultOrders() default ordering}.
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
     * @param predicates the predicate(s) to apply as a filter (can be null)
     * @return the ordered projection result
     *
     * @see #findFieldsDistinct(Function, Function[], Function[])
     * @see #findFieldsDistinct(Function, Function, Function[])
     */
    public <F> List<F> findFieldsDistinctUsingDefaultOrder(
            final Function<Q, Expression<F>> projection,
            @Nullable final Function<Q, Predicate>... predicates
    ) {
        return queryDslSupport
                .select(projection.apply(getEntityPath()))
                .distinct()
                .from(getEntityPath())
                .where(unwrapPredicates(predicates))
                .orderBy(getDefaultOrdersUnwrapped())
                .fetch();
    }

    /**
     * Based on the given predicate function search for applicable entity instances and return the first projection
     * based on specified {@link OrderSpecifier ordering}.
     *
     * <p>
     * <b>CAUTION</b>: when the supplied {@link OrderSpecifier ordering} is not aligned to the projection one might get
     * unexpected results, because the elimination of duplicates is based on the ordering, not the projection!
     * </p>
     *
     * @param projection        the projection that defines the field to return
     * @param predicates        the predicate(s) to apply as a filter (can be null)
     * @param orderSpecifiers    the ordering to apply
     * @return the first projection result based on the given order
     *
     * @see #findFirstFields(Function, Function, Function[])
     */
    public <F> Optional<F> findFirstFields(
            final Function<Q, Expression<F>> projection,
            @Nullable final Function<Q, Predicate>[] predicates,
            final Function<Q, OrderSpecifier<?>>... orderSpecifiers
    ) {
        return Optional.ofNullable(queryDslSupport
                .select(projection.apply(getEntityPath()))
                .distinct()
                .from(getEntityPath())
                .where(unwrapPredicates(predicates))
                .orderBy(unwrapOrderSpecifiers(orderSpecifiers))
                .fetchFirst());
    }

    /**
     * Based on the given predicate function search for applicable entity instances and return the first projection
     * based on specified {@link OrderSpecifier ordering}.
     *
     * <p>
     * <b>CAUTION</b>: when the supplied {@link OrderSpecifier ordering} is not aligned to the projection one might get
     * unexpected results, because the elimination of duplicates is based on the ordering, not the projection!
     * </p>
     *
     * @param projection        the projection that defines the field to return
     * @param predicate         the predicate to apply as a filter
     * @param orderSpecifiers    the ordering to apply
     * @return the first projection result based on the given order
     *
     * @see #findFirstFields(Function, Function[], Function[])
     */
    public <F> Optional<F> findFirstFields(
            final Function<Q, Expression<F>> projection,
            @Nullable final Function<Q, Predicate> predicate,
            final Function<Q, OrderSpecifier<?>>... orderSpecifiers
    ) {
        return findFirstFields(projection, asArray(predicate), orderSpecifiers);
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
     * @param projection    the projection that defines the field to return
     * @param predicates    the predicate(s) to apply as a filter
     * @return the first projection result based on the default order
     *
     * @see #getDefaultOrders()
     */
    public <F> Optional<F> findFirstFieldsUsingDefaultOrder(
            final Function<Q, Expression<F>> projection,
            @Nullable final Function<Q, Predicate>... predicates
    ) {
        return findFirstFields(projection, predicates, getDefaultOrdersAsArray());
    }

    /**
     * Based on the given predicates search for exactly one or zero entity instance and return the projection.
     *
     * @param projection the projection that defines the field to return
     * @param predicates the predicates to apply as a filter
     * @return the projection result
     * @throws NonUniqueResultException if there is more than one matching result
     */
    public <F> Optional<F> findUniqueFields(
            final Function<Q, Expression<F>> projection,
            final Function<Q, Predicate>... predicates
    ) throws NonUniqueResultException {
        return Optional.ofNullable(queryDslSupport
                .select(projection.apply(getEntityPath()))
                .from(getEntityPath())
                .where(unwrapPredicates(predicates))
                .fetchOne());
    }

    /**
     * Based on the given predicate(s), search for applicable entity instances and apply the given
     * {@link OrderSpecifier ordering}.
     *
     * @param predicates the predicate(s) to apply as a filter
     * @param orderSpecifiers    the ordering to apply
     * @return the ordered instances
     *
     * @see #find(Function, Function[])
     * @see #findUsingDefaultOrder(Function[])
     */
    public List<T> find(
            @Nullable final Function<Q, Predicate>[] predicates,
            final Function<Q, OrderSpecifier<?>>... orderSpecifiers
    ) {
        return queryDslSupport
                .selectFrom(getEntityPath())
                .where(unwrapPredicates(predicates))
                .orderBy(unwrapOrderSpecifiers(orderSpecifiers))
                .fetch();
    }

    /**
     * Based on the given predicate(s), search for applicable entity instances and apply the given
     * {@link OrderSpecifier ordering}.
     *
     * @param predicate the predicate to apply as a filter (can be null)
     * @param orderSpecifiers    the ordering to apply
     * @return the ordered instances
     *
     * @see #find(Function[], Function[])
     * @see #findUsingDefaultOrder(Function[])
     */
    public List<T> find(
            @Nullable final Function<Q, Predicate> predicate,
            final Function<Q, OrderSpecifier<?>>... orderSpecifiers
    ) {
        return find(asArray(predicate), orderSpecifiers);
    }

    /**
     * Based on the given predicate function search for applicable entity instances and apply the
     * {@link #getDefaultOrders() default ordering}.
     *
     * <p>
     *     NOTE: the default implementation of {@link #getDefaultOrders()} requires that the <code>id</code> field
     *     exists.  If this is not the case, then that method must be overridden.
     * </p>
     *
     * @param predicates the predicate function to apply as a filter
     * @return the ordered instances
     *
     * @see #find(Function[], Function[])
     * @see #find(Function, Function[])
     */
    public List<T> findUsingDefaultOrder(Function<Q, Predicate>... predicates) {
        return find(predicates, getDefaultOrdersAsArray());
    }

    /**
     * Based on the given predicate search for applicable entity instances,
     * then transform the results to the given bean using the given projections and apply the given ordering.
     *
     * @param projections       the projections which define which information to deliver to the bean creation
     * @param predicates        the predicate(s) to apply as a filter (can be null)
     * @param orderSpecifiers    the ordering to apply
     * @param bean              the bean class to use as a destination for the projection results
     * @param <B>               the bean type
     * @return the information defined by the projections as ordered beans
     *
     * @see #findAsBean(Function, Function, Class, Function[])
     */
    public <B> List<B> findAsBean(
            final Function<Q, Expression<?>>[] projections,
            final Class<? extends B> bean,
            @Nullable final Function<Q, Predicate>[] predicates,
            final Function<Q, OrderSpecifier<?>>... orderSpecifiers
    ) {
        return (List<B>) queryDslSupport
                .from(getEntityPath())
                .projection(Projections.bean(bean, unwrapProjections(projections)))
                .where(unwrapPredicates(predicates))
                .orderBy(unwrapOrderSpecifiers(orderSpecifiers))
                .fetch();
    }

    /**
     * Based on the given predicate search for applicable entity instances,
     * then transform the results to the given bean using the given projections and apply the given ordering.
     *
     * @param predicate         the predicate to apply as a filter (can be null)
     * @param orderSpecifier    the ordering to apply
     * @param bean              the bean class to use as a destination for the projection results
     * @param projections       the projections which define which information to deliver to the bean creation
     * @param <B>               the bean type
     * @return the information defined by the projections as ordered beans
     */
    public <B> List<B> findAsBean(
            @Nullable final Function<Q, Predicate> predicate,
            final Function<Q, OrderSpecifier<?>> orderSpecifier,
            final Class<? extends B> bean,
            final Function<Q, Expression<?>>... projections) {
        return (List<B>) queryDslSupport
                .from(getEntityPath())
                .projection(Projections.bean(bean, unwrapProjections(projections)))
                .where(unwrapPredicates(predicate))
                .orderBy(orderSpecifier.apply(getEntityPath()))
                .fetch();
    }

//not used    
//    private <F> Expression<F>[] unwrapExpressions(Function<Q, Expression<F>>... expressions) {
//        return _Casts.uncheckedCast(Arrays.stream(expressions)
//                .map(x -> x.apply(getEntityPath()))
//                .collect(Collectors.toUnmodifiableList())
//                .toArray(new Expression[0]));
//    }

    private Expression<?>[] unwrapProjections(Function<Q, Expression<?>>... projections) {
        return Arrays.stream(projections)
                .map(x -> x.apply(getEntityPath()))
                .collect(Collectors.toUnmodifiableList())
                .toArray(new Expression[0]);
    }

    private Predicate[] unwrapPredicates(final Function<Q, Predicate>... predicates) {
        if(predicates == null) {
            return new Predicate[]{new BooleanBuilder()};
        }
        if(predicates.length == 0) {
            return new Predicate[]{new BooleanBuilder()};
        }
        if(predicates.length == 1 && predicates[0] == null) {
            return new Predicate[]{new BooleanBuilder()};
        }
        return Arrays.stream(predicates)
                .map(x -> x.apply(getEntityPath()))
                .collect(Collectors.toUnmodifiableList())
                .toArray(new Predicate[0]);
    }

    private OrderSpecifier<?>[] unwrapOrderSpecifiers(final Function<Q, OrderSpecifier<?>>... orderSpecifiers) {
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

    private static <T extends Comparable, Q extends EntityPathBase<T>> Function<Q, Predicate>[] asArray(Function<Q, Predicate> predicate) {
        return predicate != null 
                ? (Function<Q, Predicate>[]) new Function[] {predicate} 
                : new Function[0];
    }

    static <T> List<T> newList(T... objs) {
        return newArrayList(objs);
    }

    static <T> ArrayList<T> newArrayList(T... objs) {
        ArrayList<T> result = new ArrayList<>();
        Collections.addAll(result, objs);
        return result;
    }

}
