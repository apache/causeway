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

import com.google.common.base.Predicate;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.Query;

public interface RepositoryService {

    /**
     * Normally any queries are automatically preceded by flushing pending executions.
     *
     * <p>
     * This key allows this behaviour to be disabled.
     *
     * <p>
     *     Originally introduced as part of ISIS-1134 (fixing memory leaks in the objectstore)
     *     where it was found that the autoflush behaviour was causing a (now unrepeatable)
     *     data integrity error (see <a href="https://issues.apache.org/jira/browse/ISIS-1134?focusedCommentId=14500638&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-14500638">ISIS-1134 comment</a>, in the isis-module-security.
     *     However, that this could be circumvented by removing the call to flush().
     *     We don't want to break existing apps that might rely on this behaviour, on the
     *     other hand we want to fix the memory leak.  Adding this configuration property
     *     seems the most prudent way forward.
     * </p>
     */
    String KEY_DISABLE_AUTOFLUSH = "isis.services.container.disableAutoFlush";

    /**
     * Same as {@link org.apache.isis.applib.services.factory.FactoryService#instantiate(Class)}; provided as a
     * convenience because instantiating and {@link #persist(Object) persisting} are often done together.
     */
    @Programmatic
    @Deprecated
    <T> T instantiate(final Class<T> ofType);

    /**
     * Determines if the specified object is persistent (that it is stored permanently outside of the virtual machine
     * in the object store).
     */
    @Programmatic
    @Deprecated
    boolean isPersistent(Object domainObject);

    /**
     * Persist the specified object (or do nothing if already persistent).
     *
     * <p>
     * It is recommended that the object be initially instantiated using
     * {@link org.apache.isis.applib.DomainObjectContainer#newTransientInstance(Class)}.  However, the framework will also
     * handle the case when the object is simply <i>new()</i>ed up.
     *
     * @see org.apache.isis.applib.DomainObjectContainer#newTransientInstance(Class)
     * @see #isPersistent(Object)
     */
    @Programmatic
    <T> T persist(T domainObject);
    
    /**
     * Persist the specified object (or do nothing if already persistent) and flushes changes to the database.
     *
     * @see #persist(Object)
     */
    @Programmatic
    <T> T persistAndFlush(T domainObject);

    /**
     * Deletes the domain object but only if is persistent.
     *
     * @param domainObject
     */
    @Programmatic
    void remove(Object domainObject);

    /**
     * Removes all instances of the domain object.
     *
     * <p>
     *     Intended primarily for testing purposes.
     * </p>
     */
    <T> void removeAll(Class<T> cls);

    /**
     * Deletes the domain object but only if is persistent, and flushes changes to the database.
     *
     * @param domainObject
     */
    @Programmatic
    void removeAndFlush(Object domainObject);

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
     * @param range 2 longs, specifying 0-based start and count.
     */
    @Programmatic
    <T> List<T> allInstances(Class<T> ofType, long... range);

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
     * @see #allMatches(Class, Predicate, long...)
     *
     * @param range 2 longs, specifying 0-based start and count.
     */
    @Programmatic
    <T> List<T> allMatches(final Class<T> ofType, final Predicate<? super T> predicate, long... range);

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
    @Programmatic
    <T> List<T> allMatches(Query<T> query);

    /**
     * Returns the first instance of the specified type (including subtypes)
     * that matches the supplied {@link Predicate}, or <tt>null</tt> if none.
     *
     * <p>
     * This method is useful during exploration/prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #firstMatch(Query)} for production code.
     * </p>
     *
     */
    @Programmatic
    <T> Optional<T> firstMatch(final Class<T> ofType, final Predicate<T> predicate);

    /**
     * Returns the first instance that matches the supplied query, or <tt>null</tt> if none.
     *
     * <p>
     *     This method is the recommended way of querying for an instance when one or more may match.  See also
     *     {@link #uniqueMatch(Query)}.
     * </p>
     *
     * @see #uniqueMatch(Query)
     *
     */
    @Programmatic
    <T> Optional<T> firstMatch(Query<T> query);

    /**
     * Find the only instance of the specified type (including subtypes) that
     * has the specified title.
     *
     * <p>
     * If no instance is found then <tt>null</tt> will be return, while if there
     * is more that one instances a run-time exception will be thrown.
     *
     * <p>
     * This method is useful during exploration/prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #uniqueMatch(Query)} for production code.
     * </p>
     */
    @Programmatic
    <T> Optional<T> uniqueMatch(final Class<T> ofType, final Predicate<T> predicate);

    /**
     * Find the only instance that matches the provided query.
     *
     * <p>
     * If no instance is found then null will be return, while if there is more
     * that one instances a run-time exception will be thrown.
     * </p>
     *
     * <p>
     *     This method is the recommended way of querying for (precisely) one instance.  See also {@link #allMatches(Query)}
     * </p>
     *
     * @see #firstMatch(Query)
     */
    @Programmatic
    <T> Optional<T> uniqueMatch(Query<T> query);


}
