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

import javax.annotation.Nullable;

import org.apache.isis.applib.query.Query;

import lombok.val;

// tag::refguide[]
public interface RepositoryService {

    // end::refguide[]
    /**
     * Returns the EntityState of given {@code object}. Returns {@link EntityState#NOT_PERSISTABLE} for {@code object==null}.
     * @param object
     * @return (non-null)
     * @since 2.0
     */
    // tag::refguide[]
    EntityState getEntityState(@Nullable Object object);    // <.>

    // end::refguide[]
    /**
     * Same as {@link org.apache.isis.applib.services.factory.FactoryService#detachedEntity(Class)}; provided as a
     * convenience because instantiating and {@link #persist(Object) persisting} are often done together.
     * @since 2.0
     */
    // tag::refguide[]
    <T> T detachedEntity(Class<T> ofType);                  // <.>

    // end::refguide[]
    /**
     * Persist the specified object (or do nothing if already persistent).
     *
     * @see #isPersistent(Object)
     */
    // tag::refguide[]
    <T> T persist(T domainObject);                          // <.>

    // end::refguide[]
    /**
     * Persist the specified object (or do nothing if already persistent) and flushes changes to the database.
     *
     * @see #persist(Object)
     */
    // tag::refguide[]
    <T> T persistAndFlush(T domainObject);                  // <.>

    // end::refguide[]
    /**
     * Deletes the domain object but only if is attached.
     *
     * @param domainObject
     */
    // tag::refguide[]
    void remove(Object domainObject);                       // <.>

    // end::refguide[]
    /**
     * Deletes the domain object but only if is persistent, and flushes changes to the database.
     *
     * @param domainObject
     */
    // tag::refguide[]
    void removeAndFlush(Object domainObject);               // <.>

    // end::refguide[]
    /**
     * As {@link #allInstances(Class, long, long)}, but but returning all instances rather than just those
     *      * within the specified range..
     */
    // tag::refguide[]
    <T> List<T> allInstances(Class<T> ofType);              // <.>

    // end::refguide[]
    /**
     * Returns all the instances of the specified type (including subtypes).
     * If the optional range parameters are used, the dataset returned starts
     * from (0 based) index, and consists of only up to count items.
     *
     * <p>
     * If there are no instances the list will be empty. This method creates a
     * new {@link List} object each time it is called so the caller is free to
     * use or modify the returned {@link List}, but the changes will not be
     * reflected back to the repository.
     * </p>
     *
     * <p>
     * This method should only be called where the number of instances is known
     * to be relatively low, unless the optional range parameters (2 longs) are
     * specified. The range parameters are "start" and "count".
     * </p>
     *
     * @param ofType
     * @param start
     * @param count
     * @param <T>
     * @return
     */
    // tag::refguide[]
    <T> List<T> allInstances(                               // <.>
            Class<T> ofType,
            long start, long count);

    // end::refguide[]
    /**
     * As {@link #allMatches(Class, Predicate, long, long)}, but returning all instances rather than just those
     * within the specified range.
     */
    // tag::refguide[]
    <T> List<T> allMatches(                                 // <.>
            Class<T> ofType,
            Predicate<? super T> predicate);

    // end::refguide[]
    /**
     * Returns all the instances of the specified type (including subtypes) that
     * the predicate object accepts. If the optional range parameters are used, the
     * dataset returned starts from (0 based) index, and consists of only up to
     * count items.
     *
     * <p>
     * If there are no instances the list will be empty. This method creates a
     * new {@link List} object each time it is called so the caller is free to
     * use or modify the returned {@link List}, but the changes will not be
     * reflected back to the repository.
     * </p>
     *
     * <p>
     * This method is useful during exploration/prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #allMatches(Query)} for production code.
     * </p>
     *
     * @param ofType
     * @param predicate
     * @param start
     * @param count
     * @param <T>
     * @return
     */
    // tag::refguide[]
    <T> List<T> allMatches(                                 // <.>
            Class<T> ofType,
            Predicate<? super T> predicate,
            long start, long count);

    // end::refguide[]
    /**
     * Returns all the instances that match the given {@link Query}.
     *
     * <p>
     * If there are no instances the list will be empty. This method creates a
     * new {@link List} object each time it is called so the caller is free to
     * use or modify the returned {@link List}, but the changes will not be
     * reflected back to the repository.
     * </p>
     *
     * <p>
     *     This method is the recommended way of querying for multiple instances.
     * </p>
     */
    // tag::refguide[]
    <T> List<T> allMatches(Query<T> query);                 // <.>

    // end::refguide[]
    /**
     * Find the only instance of the specified type (including subtypes) that
     * has the specified title.
     *
     * <p>
     * If no instance is found then {@link Optional#empty()} will be return, while if there
     * is more that one instances a run-time exception will be thrown.
     *
     * <p>
     * This method is useful during exploration/prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #uniqueMatch(Query)} for production code.
     * </p>
     */
    // tag::refguide[]
    <T> Optional<T> uniqueMatch(                            // <.>
            Class<T> ofType,
            Predicate<T> predicate);

    // end::refguide[]
    /**
     * Find the only instance that matches the provided query.
     *
     * <p>
     * If no instance is found then {@link Optional#empty()} will be return, while if there is more
     * that one instances a run-time exception will be thrown.
     * </p>
     *
     * <p>
     *     This method is the recommended way of querying for (precisely) one instance.  See also {@link #allMatches(Query)}
     * </p>
     *
     * @see #firstMatch(Query)
     */
    // tag::refguide[]
    <T> Optional<T> uniqueMatch(Query<T> query);            // <.>

    // end::refguide[]
    /**
     * Find the only instance of the specified type (including subtypes) that
     * has the specified title.
     *
     * <p>
     * If no instance is found then {@link Optional#empty()} will be return, while if there
     * is more that one instances then the first will be returned.
     *
     * <p>
     * This method is useful during exploration/prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #firstMatch(Query)} for production code.
     * </p>
     */
    // tag::refguide[]
    <T> Optional<T> firstMatch(                             // <.>
            Class<T> ofType,
            Predicate<T> predicate);

    // end::refguide[]
    /**
     * Find the only instance that matches the provided query, if any..
     *
     * <p>
     * If no instance is found then {@link Optional#empty()} will be return, while if there is more
     * that one instances then the first will be returned.
     * </p>
     *
     * @see #firstMatch(Query)
     */
    // tag::refguide[]
    <T> Optional<T> firstMatch(Query<T> query);             // <.>
    // end::refguide[]
    /**
     * Reloads the pojo.
     */
    // tag::refguide[]
    <T> T refresh(T pojo);                                // <.>

    // end::refguide[]
    /**
     * Detach the entity from the current persistence session.
     *
     * <p>
     * This allows the entity to be read from even after the PersistenceSession that obtained it has been closed.
     *
     * @param entity - to detach
     */
    // tag::refguide[]
    <T> T detach(T entity);                            // <.>

    // end::refguide[]

    // -- DEPRECATIONS

    /**
     * @deprecated if applicable use {@link #detachedEntity(Class)} instead
     */
    @Deprecated
    default <T> T instantiate(Class<T> ofType) {
        return detachedEntity(ofType);
    }

    /**
     * Determines if the specified object is persistent (that it is stored permanently outside of the virtual machine
     * in the object store).
     *
     * <p>
     *     This method can also return <code>true</code> if the object has been {@link #isDeleted(Object) deleted}
     *     from the object store.
     * </p>
     * @deprecated due to ambiguous semantic, use {@link #getEntityState(Object)} instead
     */
    @Deprecated
    default boolean isPersistent(Object domainObject) {
        val entityState = getEntityState(domainObject);
        return entityState.isAttached() || entityState.isDestroyed();
    }

    /**
     * Determines if the specified object has been deleted from the object store.
     * @deprecated due to ambiguous semantic, use {@link #getEntityState(Object)} instead
     */
    @Deprecated
    default boolean isDeleted(Object domainObject) {
        val entityState = getEntityState(domainObject);
        return entityState.isDestroyed();
    }

    // tag::refguide[]

}
// end::refguide[]
