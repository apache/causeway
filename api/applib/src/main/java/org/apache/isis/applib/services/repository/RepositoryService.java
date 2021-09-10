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
package org.apache.isis.applib.services.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryRange;

import lombok.NonNull;
import lombok.val;

/**
 * Collects together methods for creating, persisting and searching for
 * entities from the underlying persistence store.
 *
 * <p>
 *     Typically it's good practice to define a domain-specific service
 *     (eg <code>CustomerRepository</code>) which then delegates to this
 *     service. This domain-specific service can use some
 *     {@link RepositoryService}'s "naive" methods that use client-side
 *     predicates for filtering; these can then be replaced by more
 *     sophisticated implementations that use proper server-side filtering later
 *     on without impacting the rest of the application.
 * </p>
 *
 * @since 1.x revised for 2.0 {@index}
 */
public interface RepositoryService {

    /**
     * Returns the {@link EntityState} of given {@code object}.
     *
     * @apiNote  Returns {@link EntityState#NOT_PERSISTABLE} if {@code object==null}.
     *
     * @param object
     * @return (non-null)
     * @since 2.0
     */
    EntityState getEntityState(@Nullable Object object);

    /**
     * Usually called as a precursor to persisting a domain entity, this method
     * verifies that the object is an entity and injects domain services into
     * it.
     *
     * <p>
     *     This approach allows the domain entity to have regular constructor
     *     (with parameters) to set up the initial state of the domain object.
     *     This is preferred over {@link #detachedEntity(Class)}, which
     *     also instantiates the class and then injects into it - but requires
     *     that the domain object has a no-arg constructor to do so.
     * </p>
     *
     *
     * <p>
     * This is the same functionality as exposed by
     * {@link org.apache.isis.applib.services.factory.FactoryService#detachedEntity(Object)}.
     * It is provided in this service as a convenience because instantiating and
     * {@link #persist(Object) persisting} an object are often done together.
     * </p>
     *
     * @since 2.0
     */
    <T> T detachedEntity(@NonNull T entity);

    /**
     * Persist the specified object (or do nothing if already persistent).
     *
     * <p>
     *     The persist isn't necessarily performed immediately; by default
     *     all pending changes are flushed to the database when the transaction completes.
     * </p>
     *
     */
    <T> T persist(T domainObject);

    /**
     * Persist the specified object (or do nothing if already persistent) and
     * flushes changes to the database.
     *
     * <p>
     *     Flushing will also result in ORM-maintained bidirectional
     *     relationships being updated.
     * </p>
     *
     * @see #persist(Object)
     * @see #persistAndFlush(Object[])
     */
    <T> T persistAndFlush(T domainObject);

    /**
     * Persist the specified objects (or do nothing if already persistent) and
     * flushes changes to the database.
     *
     * <p>
     *     Flushing will also result in ORM-maintained bidirectional
     *     relationships being updated.
     * </p>
     *
     * @see #persist(Object)
     * @see #persistAndFlush(Object)
     */
    default void persistAndFlush(final Object... domainObjects) {
        final int length = domainObjects.length;
        for (int i = 0; i < length; i++) {
            val domainObject = domainObjects[i];
            if (i < length - 1) {
                // not at the end
                persist(domainObject);
            } else {
                persistAndFlush(domainObject);
            }
        }
    }

    /**
     * Remove (ie delete) an object from the persistent object store
     * (or do nothing if it has already been deleted).
     *
     * <p>
     *     The delete isn't necessarily performed immediately; by default
     *     all pending changes are flushed to the database when the transaction
     *     completes.
     * </p>
     *
     * <p>
     *     Note that this method is also a no-op if the domain object is not attached.
     * </p>
     *
     * @param domainObject
     */
    void remove(Object domainObject);

    /**
     * Deletes the domain object but only if is persistent, and flushes changes
     * to the database (meaning the object is deleted immediately).
     *
     * <p>
     *     Flushing will also result in ORM-maintained bidirectional
     *     relationships being updated.
     * </p>
     *
     * @param domainObject
     */
    void removeAndFlush(Object domainObject);

    /**
     * Removes all instances of the domain object.
     *
     * <p>
     *     Intended primarily for testing purposes.
     * </p>
     */
    <T> void removeAll(Class<T> cls);

    /**
     * Returns all persisted instances of specified type (including subtypes).
     *
     * <p>
     *     Intended primarily for prototyping purposes, though is safe to use
     *     in production applications to obtain all instances of domain entities
     *     if the number is known to be small (for example, reference/lookup
     *     data).
     * </p>
     *
     * <p>
     * If there are no instances the list will be empty.
     * </p>
     *
     * @apiNote This method creates a new {@link List} object each time it is
     *          called so the caller is free to use or modify the returned
     *          {@link List}. Changes will <i>not</i> be reflected back to the
     *          repository.
     */
    <T> List<T> allInstances(Class<T> ofType);

    /**
     * Overload of {@link #allInstances(Class)}, returns a <i>page</i> of
     * persisted instances of specified type (including subtypes).
     *
     * <p>
     * If the optional range parameters are used, the dataset returned starts
     * from (0 based) index, and consists of only up to count items.
     * </p>
     *
     * @param ofType
     * @param start
     * @param count
     * @param <T>
     */
    <T> List<T> allInstances(
            Class<T> ofType,
            long start, long count);

    /**
     * Returns all the instances of the specified type (including subtypes) that
     * the predicate object accepts.
     *
     * <p>
     * If there are no instances the list will be empty. This method creates a
     * new {@link List} object each time it is called so the caller is free to
     * use or modify the returned {@link List}, but the changes will not be
     * reflected back to the repository.
     * </p>
     *
     * <p>
     * This method is useful during exploration/prototyping, but - because the
     * filtering is performed client-side - this method is only really suitable
     * for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #allMatches(Query)} for production code.
     * </p>
     *
     * @apiNote This method creates a new {@link List} object each time it is
     *          called so the caller is free to use or modify the returned
     *          {@link List}. Changes will <i>not</i> be reflected back to the
     *          repository.
     */
    <T> List<T> allMatches(
            Class<T> ofType,
            Predicate<? super T> predicate);

    /**
     * Overload of {@link #allMatches(Class, Predicate)}, returns a <i>page</i>
     * of persisted instances of specified type (including subtypes).
     *
     * <p>
     * If the optional range parameters are used, the dataset returned starts
     * from (0 based) index, and consists of only up to count items.
     * </p>
     *
     * @param ofType
     * @param predicate
     * @param start
     * @param count
     * @param <T>
     */
    <T> List<T> allMatches(
            Class<T> ofType,
            Predicate<? super T> predicate,
            long start, long count);

    /**
     * Returns all the instances that match the given {@link Query}.
     *
     * <p>
     *     This is the main API for server-side (performant) queries returning
     *     multiple instances, where a
     *     {@link org.apache.isis.applib.query.NamedQuery} can be passed in
     *     that ultimately describes a SELECT query with WHERE predicates.
     *     The mechanism by which this is defined depends on the ORM (JDO or
     *     JPA).  A {@link org.apache.isis.applib.query.NamedQuery} can
     *     optionally specify a
     *     {@link org.apache.isis.applib.query.NamedQuery#withRange(QueryRange) range} of instances to be returned.
     * </p>
     *
     * <p>
     *     It is also possible to specify an
     *     {@link org.apache.isis.applib.query.AllInstancesQuery}.  This is
     *     equivalent to using {@link #allInstances(Class, long, long)}; a range
     *     can also be specified.
     * </p>
     *
     * @apiNote This method creates a new {@link List} object each time it is
     *          called so the caller is free to use or modify the returned
     *          {@link List}. Changes will <i>not</i> be reflected back to the
     *          repository.
     *
     * @see #allMatches(Class, Predicate, long, long)
     * @see #firstMatch(Query)
     * @see #uniqueMatch(Query)
     */
    <T> List<T> allMatches(Query<T> query);

    /**
     * Finds the only instance of the specified type (including subtypes) that
     * satifies the (client-side) predicate.
     *
     * <p>
     * This method is useful during exploration/prototyping, but - because the
     * filtering is performed client-side - this method is only really suitable
     * for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #uniqueMatch(Query)} for production code.
     * </p>
     *
     * <p>
     * If no instance is found then {@link Optional#empty()} will be return,
     * while if there is more that one instances a run-time exception will be
     * thrown.
     * </p>
     *
     * @see #uniqueMatch(Query)
     * @see #firstMatch(Class, Predicate)
     * @see #allMatches(Class, Predicate)
     * @see #allMatches(Class, Predicate, long, long)
     */
    <T> Optional<T> uniqueMatch(
            Class<T> ofType,
            Predicate<T> predicate);

    /**
     * Find the only instance that matches the provided {@link Query}.
     *
     * <p>
     *     This is the main API for server-side (performant) queries returning
     *     no more than one instance, where a
     *     {@link org.apache.isis.applib.query.NamedQuery} can be passed in
     *     that ultimately describes a SELECT query with WHERE predicates.
     *     The mechanism by which this is defined depends on the ORM (JDO or
     *     JPA).  A {@link org.apache.isis.applib.query.NamedQuery} can
     *     optionally specify a
     *     {@link org.apache.isis.applib.query.NamedQuery#withRange(QueryRange) range} of instances to be returned.
     * </p>
     *
     * <p>
     * If no instance is found then {@link Optional#empty()} will be return,
     * while if there is more that one instances a run-time exception will be
     * thrown.
     * </p>
     *
     * @see #uniqueMatch(Class, Predicate)
     * @see #firstMatch(Query)
     * @see #allMatches(Query)
     */
    <T> Optional<T> uniqueMatch(Query<T> query);

    /**
     * Find the only instance of the specified type (including subtypes) that
     * satifies the provided (client-side) predicate.
     *
     * <p>
     * This method is useful during exploration/prototyping, but - because the
     * filtering is performed client-side - this method is only really suitable
     * for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #firstMatch(Query)} for production code.
     * </p>
     *
     * <p>
     * If no instance is found then {@link Optional#empty()} will be return, while if there
     * is more that one instances then the first will be returned.
     * <p>
     *
     * @see #firstMatch(Query)
     * @see #uniqueMatch(Class, Predicate)
     * @see #allMatches(Class, Predicate)
     * @see #allMatches(Class, Predicate, long, long)
     */
    <T> Optional<T> firstMatch(
            Class<T> ofType,
            Predicate<T> predicate);

    /**
     * Find the only instance that matches the provided {@link Query}, if any.
     *
     * <p>
     *     This is the main API for server-side (performant) queries returning
     *     the first matching instance, where a
     *     {@link org.apache.isis.applib.query.NamedQuery} can be passed in
     *     that ultimately describes a SELECT query with WHERE predicates.
     *     The mechanism by which this is defined depends on the ORM (JDO or
     *     JPA).  A {@link org.apache.isis.applib.query.NamedQuery} can
     *     optionally specify a
     *     {@link org.apache.isis.applib.query.NamedQuery#withRange(QueryRange) range} of instances to be returned.
     * </p>
     *
     * <p>
     * If no instance is found then {@link Optional#empty()} will be return, while if there
     * is more that one instances then the first will be returned.
     * <p>
     *
     * @see #firstMatch(Class, Predicate)
     * @see #uniqueMatch(Query)
     * @see #allMatches(Query)
     */
    <T> Optional<T> firstMatch(Query<T> query);

    /**
     * Reloads the domain entity from the database.
     */
    <T> T refresh(T pojo);

    /**
     * Explicitly detaches the entity from the current persistence session.
     *
     * <p>
     * This allows the entity to be read from even after the PersistenceSession
     * that obtained it has been closed.
     * </p>
     *
     * @param entity - to detach
     */
    <T> T detach(T entity);

}
