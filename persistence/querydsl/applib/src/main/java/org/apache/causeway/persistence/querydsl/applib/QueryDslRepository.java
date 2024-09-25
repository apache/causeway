package org.apache.causeway.persistence.querydsl.applib;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.util.ReflectionUtils;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.repository.RepositoryService;

/**
 * Provides default implementation and convinience methods for querying a specific entity (hierarchy).
 * Relies heavily on QueryDsl to do the heavy lifting.
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
     * Default ordering is by 'id' ASC.
     *
     * Override in repository implementation to provide a default ordering of methods with multiple results
     * based on the comparable implementation of 'T' class.
     *
     * @return a list of OrderSpecifiers
     */
    protected List<OrderSpecifier<? extends Comparable>> getDefaultOrders() {
        return QueryDslUtil.newList(QueryDslUtil.ID_ORDER_SPECIFIER);
    }

    protected OrderSpecifier<? extends Comparable>[] getDefaultOrder() {
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
            entityClass = QueryDslUtil.getTypeParameter(getClass(), 0);
        }
        return entityClass;
    }

    /**
     * @return the main entity Q instance for which this repository implementation operates
     */
    public Q getEntityPath() {
        if (entityPath == null) {
            entityPath = getEntityPathInstance();
        }
        return entityPath;
    }

    public Q entity() {
        return getEntityPath();
    }

    private Q getEntityPathInstance() {
        Class<Q> qClass = QueryDslUtil.getTypeParameter(getClass(), 1);
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
     * @return all instances of this entity in the default order
     */
    public List<T> findAll() {
        return queryDslSupport
                .selectFrom(getEntityPath())
                .orderBy(getDefaultOrder())
                .fetch();
    }

    /**
     * Based on the given predicates search for exactly zero or one entity instance.
     *
     * @param predicates the predicates to apply as a filter
     * @return exactly one result or null
     * @throws NonUniqueResultException if there is more than one matching result
     */
    public Optional<T> findUnique(Predicate... predicates) throws NonUniqueResultException {
        return Optional.ofNullable(queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicates)
                .orderBy(getDefaultOrder())
                .fetchOne());
    }

    /**
     * Based on the given predicateFunction search for exactly zero or one entity instance.
     *
     * @param predicateFunction the predicate function to apply as a filter
     * @return exactly one result or null
     * @throws NonUniqueResultException if there is more than one matching result
     */
    public Optional<T> findUnique(Function<Q, Predicate> predicateFunction) throws NonUniqueResultException {
        return findUnique(predicateFunction,getDefaultOrder());
    }

    /**
     * Based on the given predicates search for exactly zero or one entity instance.
     *
     * @param predicateFunction the predicate function to apply as a filter
     * @param orderSpecifier    the ordering to apply
     * @return exactly one result or null
     * @throws NonUniqueResultException if there is more than one matching result
     */
    public Optional<T> findUnique(Function<Q, Predicate> predicateFunction,
                                  OrderSpecifier<?>... orderSpecifier) throws NonUniqueResultException {
        return Optional.ofNullable(queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicateFunction.apply(getEntityPath()))
                .orderBy(orderSpecifier)
                .fetchOne());
    }

    /**
     * Based on the given predicates search for the first entity instance based on default ordering.
     *
     * @param predicates the predicates to apply as a filter
     * @return the first result or null
     */
    public Optional<T> findAny(Predicate... predicates) {
        return Optional.ofNullable(queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicates)
                .orderBy(getDefaultOrder())
                .fetchFirst());
    }

    /**
     * Based on the given predicate function search for the first entity instance based on default ordering.
     *
     * @param predicateFunction the predicate function to apply as a filter
     * @return the first result or null
     */
    public Optional<T> findAny(Function<Q, Predicate> predicateFunction) {
        return findAny(predicateFunction.apply(getEntityPath()));
    }

    /**
     * Based on the given predicates search for the first entity instance based on specified ordering.
     *
     * @param predicates the predicates to apply as a filter
     * @param orderSpecifier    the ordering to apply
     * @return the first result or null
     */
    public Optional<T> findFirst(Predicate[] predicates, OrderSpecifier<?>... orderSpecifier) {
        return Optional.ofNullable(queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicates)
                .orderBy(orderSpecifier)
                .fetchFirst());
    }

    /**
     * Based on the given predicate function search for the first entity instance based on specified ordering.
     *
     * @param predicateFunction the predicate function to apply as a filter
     * @param orderSpecifier    the ordering to apply
     * @return the first result or null
     */
    public Optional<T> findFirst(Function<Q, Predicate> predicateFunction, OrderSpecifier<?>... orderSpecifier) {
        return findFirst(new Predicate[]{predicateFunction.apply(getEntityPath())}, orderSpecifier);
    }

    /**
     * Based on the given predicates search for applicable entity instances and return the distinct projection.
     *
     * @param projection the projection that defines the field to return
     * @param predicates the predicates to apply as a filter
     * @return the ordered projection result
     */
    public <F> List<F> findField(Expression<F> projection, Predicate... predicates) {
        return queryDslSupport
                .select(projection)
                .distinct()
                .from(getEntityPath())
                .where(predicates)
                .orderBy(getDefaultOrder())
                .fetch();
    }

    /**
     * Based on the given predicate function search for applicable entity instances and return the distinct projection.
     * CAUTION when the ordering is not aligned to the projection one might get unexpected results! E.g. when the
     * default ordering is defined for another field then in the projection, the distinct is applied to the ordering
     * definition instead of to the projection!
     *
     * @param projection        the projection that defines the field to return
     * @param predicateFunction the predicate function to apply as a filter
     * @return the ordered projection result
     */
    public <F> List<F> findField(Expression<F> projection, Function<Q, Predicate> predicateFunction) {
        return findField(projection, predicateFunction, getDefaultOrder());
    }

    /**
     * Based on the given predicate function search for applicable entity instances and return the distinct projection.
     *
     * @param projection        the projection that defines the field to return
     * @param predicateFunction the predicate function to apply as a filter
     * @param orderSpecifier    the ordering to apply; CAUTION when the ordering is not aligned to the projection one
     *                          might get unexpected results! E.g. when the default ordering is defined for another
     *                          field then in the projection, the distinct is applied to the ordering definition
     *                          instead of to the projection!
     * @return the projection result ordered as specified
     */
    public <F> List<F> findField(Expression<F> projection,
                                 Function<Q, Predicate> predicateFunction,
                                 OrderSpecifier<?>... orderSpecifier) {
        return queryDslSupport
                .select(projection)
                .distinct()
                .from(getEntityPath())
                .where(predicateFunction == null ? new BooleanBuilder() : predicateFunction.apply(getEntityPath()))
                .orderBy(orderSpecifier)
                .fetch();
    }

    /**
     * Based on the given predicate function search for applicable entity instances and return the first projection
     * based on default ordering.
     *
     * @param projection        the projection that defines the field to return
     * @param predicateFunction the predicate function to apply as a filter
     * @return the first projection result based on the default order
     */
    public <F> Optional<F> findFieldAny(Expression<F> projection,
                                        Function<Q, Predicate> predicateFunction) {
        return findFirstField(projection, predicateFunction, getDefaultOrder());
    }

    /**
     * Based on the given predicate function search for applicable entity instances and return the first projection
     * based on specified ordering.
     *
     * @param projection        the projection that defines the field to return
     * @param predicateFunction the predicate function to apply as a filter
     * @param orderSpecifier    the ordering to apply
     * @return the first projection result based on the given order
     */
    public <F> Optional<F> findFirstField(Expression<F> projection,
                                          Function<Q, Predicate> predicateFunction,
                                          OrderSpecifier<?>... orderSpecifier) {
        return Optional.ofNullable(queryDslSupport
                .select(projection)
                .distinct()
                .from(getEntityPath())
                .where(predicateFunction == null ? new BooleanBuilder() : predicateFunction.apply(getEntityPath()))
                .orderBy(orderSpecifier)
                .fetchFirst());
    }

    /**
     * Based on the given predicate search for applicable entity instances and return the first projection
     * based on specified ordering.
     *
     * @param projection     the projection that defines the field to return
     * @param predicate      the predicate to apply as a filter
     * @param orderSpecifier the ordering to apply
     * @return the first projection result based on the given order
     */
    public <F> Optional<F> findFirstField(Expression<F> projection,
                                          Predicate predicate,
                                          OrderSpecifier<?>... orderSpecifier) {
        return Optional.ofNullable(queryDslSupport
                .select(projection)
                .distinct()
                .from(getEntityPath())
                .where(predicate)
                .orderBy(orderSpecifier)
                .fetchFirst());
    }

    /**
     * Based on the given predicates search for exactly one or zero entity instance and return the projection.
     *
     * @param projection the projection that defines the field to return
     * @param predicates the predicates to apply as a filter
     * @return the projection result
     * @throws NonUniqueResultException if there is more than one matching result
     */
    public <F> Optional<F> findUniqueField(Expression<F> projection, Predicate... predicates) throws NonUniqueResultException {
        return Optional.ofNullable(queryDslSupport
                .select(projection)
                .from(getEntityPath())
                .where(predicates)
                .orderBy(getDefaultOrder())
                .fetchOne());
    }

    /**
     * Based on the given predicate function search for exactly one or zero entity instance and return the projection.
     *
     * @param projection        the projection that defines the field to return
     * @param predicateFunction the predicate function to apply as a filter
     * @return the projection result
     * @throws NonUniqueResultException if there is more than one matching result
     */
    public <F> Optional<F> findUniqueField(Expression<F> projection, Function<Q, Predicate> predicateFunction) throws NonUniqueResultException {
        return Optional.ofNullable(queryDslSupport
                .select(projection)
                .from(getEntityPath())
                .where(predicateFunction.apply(getEntityPath()))
                .orderBy(getDefaultOrder())
                .fetchOne());
    }

    /**
     * Based on the given predicates search for applicable entity instances and apply the default ordering.
     *
     * @param predicates the predicates to apply as a filter
     * @return the ordered instances
     */
    public List<T> find(Predicate... predicates) {
        return queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicates)
                .orderBy(getDefaultOrder())
                .fetch();
    }

    /**
     * Based on the given predicate function search for applicable entity instances and apply the default ordering.
     *
     * @param predicateFunction the predicate function to apply as a filter
     * @return the ordered instances
     */
    public List<T> find(Function<Q, Predicate> predicateFunction) {
        return queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicateFunction.apply(getEntityPath()))
                .orderBy(getDefaultOrder())
                .fetch();
    }

    /**
     * Based on the given predicate search for applicable entity instances and apply the given ordering.
     *
     * @param predicate      the predicate to apply as a filter
     * @param orderSpecifier the ordering to apply
     * @return the ordered instances
     */
    public List<T> findOrdered(Predicate predicate, OrderSpecifier<?>... orderSpecifier) {
        return queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicate)
                .orderBy(orderSpecifier)
                .fetch();
    }

    /**
     * Based on the given predicate function search for applicable entity instances and apply the given ordering.
     *
     * @param predicateFunction the predicate function to apply as a filter
     * @param orderSpecifier    the ordering to apply
     * @return the ordered instances
     */
    public List<T> findOrdered(Function<Q, Predicate> predicateFunction, OrderSpecifier<?>... orderSpecifier) {
        DslQuery<T> tDslQuery = queryDslSupport
                .selectFrom(getEntityPath())
                .where(predicateFunction.apply(getEntityPath()))
                .orderBy(orderSpecifier);
        return tDslQuery.fetch();
    }

    /**
     * Based on the given predicate search for applicable entity instances,
     * then transform the results to the given bean using the given projections and apply the given ordering.
     *
     * @param predicate      the predicate to apply as a filter
     * @param orderSpecifier the ordering to apply
     * @param bean           the bean class to use as a destination for the projection results
     * @param projections    the projections which define which information to deliver to the bean creation
     * @param <B>            the bean type
     * @return the information defined by the projections as ordered beans
     */
    public <B> List<B> findAsBean(Predicate predicate, OrderSpecifier<?> orderSpecifier, Class<? extends B> bean, Expression<?>... projections) {
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
     * @param predicateFunction the predicate function to apply as a filter
     * @param orderSpecifier    the ordering to apply
     * @param bean              the bean class to use as a destination for the projection results
     * @param projections       the projections which define which information to deliver to the bean creation
     * @param <B>               the bean type
     * @return the information defined by the projections as ordered beans
     */
    public <B> List<B> findAsBean(Function<Q, Predicate> predicateFunction, OrderSpecifier<?> orderSpecifier, Class<? extends B> bean, Expression<?>... projections) {
        return (List<B>) queryDslSupport
                .from(getEntityPath())
                .projection(Projections.bean(bean, projections))
                .where(predicateFunction.apply(getEntityPath()))
                .orderBy(orderSpecifier)
                .fetch();
    }
}