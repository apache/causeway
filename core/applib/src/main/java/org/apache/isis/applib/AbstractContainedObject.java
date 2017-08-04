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

package org.apache.isis.applib;

import java.util.List;

import com.google.common.base.Predicate;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.applib.services.user.UserService;

/**
 * Convenience super class for all classes that wish to interact with the
 * container.
 * 
 * @see org.apache.isis.applib.DomainObjectContainer
 */
public abstract class AbstractContainedObject {

    /**
     * Create a new instance of the specified class, but do not persist it.
     *
     * <p>
     * It is recommended that the object be initially instantiated using
     * this method, though the framework will also handle the case when 
     * the object is simply <i>new()</i>ed up.  The benefits of using
     * {@link #newTransientInstance(Class)} are:
     * </p>
     *
     * <ul>
     * <li>any services will be injected into the object immediately
     *     (otherwise they will not be injected until the framework
     *     becomes aware of the object, typically when it is 
     *     {@link #persist(Object) persist}ed</li>
     * <li>the default value for any properties (usually as specified by 
     *     <tt>default<i>Xxx</i>()</tt> supporting methods) will not be
     *     used</li>
     * <li>the <tt>created()</tt> callback will not be called.
     * </ul>
     * 
     * <p>
     * The corollary is: if your code never uses <tt>default<i>Xxx</i>()</tt> 
     * supporting methods or the <tt>created()</tt> callback, then you can
     * alternatively just <i>new()</i> up the object rather than call this
     * method.
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#newTransientInstance(Class)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.factory.FactoryService#instantiate(Class)} or simply instantiate object directly and inject services using {@link ServiceRegistry2#injectServicesInto(Object)}.
     */
    @Deprecated
    protected <T> T newTransientInstance(final Class<T> ofType) {
        return getContainer().newTransientInstance(ofType);
    }

    /**
     * Create a new {@link ViewModel} instance of the specified type, initializing with the specified memento.
     *
     * <p>
     *     Rather than use this constructor it is generally preferable to simply instantiate a
     *     class annotated with {@link org.apache.isis.applib.annotation.ViewModel annotation}.
     *     If services need injecting into it, use {@link DomainObjectContainer#injectServicesInto(Object)}.
     * </p>
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#newViewModelInstance(Class, String)
     *
     * @deprecated - use JAXB view models, instantiated either using {@link org.apache.isis.applib.services.factory.FactoryService#instantiate(Class)} or simply instantiated directly with services injected using {@link ServiceRegistry2#injectServicesInto(Object)}.
     */
    @Deprecated
    protected <T extends ViewModel> T newViewModelInstance(final Class<T> ofType, final String memento) {
        return getContainer().newViewModelInstance(ofType, memento);
    }

    /**
     * @deprecated - not supported, will throw a RuntimeException
     */
    @Deprecated
    protected <T> T newAggregatedInstance(final Class<T> ofType) {
        return newAggregatedInstance(this, ofType);
    }

    /**
     * @deprecated - not supported, will throw a RuntimeException
     */
    @Deprecated
    protected <T> T newAggregatedInstance(final Object parent, final Class<T> ofType) {
        return getContainer().newAggregatedInstance(parent, ofType);
    }
    
    // //////////////////////////////////////

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
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#allInstances(Class, long...)
     *
     * @param range 2 longs, specifying 0-based start and count.
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#allInstances(Class, long...)}.
     */
    @Deprecated
    protected <T> List<T> allInstances(final Class<T> ofType, long... range) {
        return getContainer().allInstances(ofType, range);
    }

    // //////////////////////////////////////

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
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#allMatches(Class, Predicate, long...)
     * @see #allMatches(Query)
     *
     * @param range 2 longs, specifying 0-based start and count.
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#allMatches(Class, Predicate, long...)}.
     */
    @Deprecated
    @Programmatic
    protected <T> List<T> allMatches(final Class<T> ofType, final Predicate<? super T> predicate, long... range) {
        return getContainer().allMatches(ofType, predicate, range);
    }
    
    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#allMatches(Class, Predicate, long...)}.
     */
    @Deprecated
    protected <T> List<T> allMatches(final Class<T> ofType, final Filter<? super T> filter, long... range) {
        return getContainer().allMatches(ofType, filter, range);
    }

    /**
     * Returns all the instances of the specified type (including subtypes) that
     * match the given object: where any property that is set will be tested and
     * properties that are not set will be ignored.
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
     * This method is useful during exploration/prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #allMatches(Query)} for production code.
     * </p>
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#allMatches(Class, Object, long...)
     *
     * @param range 2 longs, specifying 0-based start and count.
     *
     * @deprecated - convert to use {@link org.apache.isis.applib.services.repository.RepositoryService#allMatches(Query)}.
     */
    @Deprecated
    protected <T> List<T> allMatches(final Class<T> ofType, final T pattern, long... range) {
        return getContainer().allMatches(ofType, pattern, range);
    }

    /**
     * Returns all the instances of the specified type (including subtypes) that
     * have the given title.
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
     * This method is useful during exploration/prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #allMatches(Query)} for production code.
     * </p>
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#allMatches(Class, String, long...)
     *
     * @param range 2 longs, specifying 0-based start and count.
     *
     * @deprecated - convert to use {@link org.apache.isis.applib.services.repository.RepositoryService#allMatches(Query)} instead
     */
    @Deprecated
    protected <T> List<T> allMatches(final Class<T> ofType, final String title, long... range) {
        return getContainer().allMatches(ofType, title, range);
    }

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
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#allMatches(Query)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#allMatches(Query)}
     */
    @Deprecated
    protected <T> List<T> allMatches(final Query<T> query) {
        return getContainer().allMatches(query);
    }

    // //////////////////////////////////////

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
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#firstMatch(Class, Predicate)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#firstMatch(Class, Predicate)}
     */
    @Deprecated
    protected <T> T firstMatch(final Class<T> ofType, final Predicate<T> predicate) {
        return getContainer().firstMatch(ofType, predicate);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#firstMatch(Class, Predicate)}
     */
    @Deprecated
    protected <T> T firstMatch(final Class<T> ofType, final Filter<T> filter) {
        return getContainer().firstMatch(ofType, filter);
    }

    /**
     * Returns the first instance of the specified type (including subtypes)
     * that matches the supplied object as a pattern, or <tt>null</tt> if none.
     *
     * <p>
     * This method is useful during exploration/prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #firstMatch(Query)} for production code.
     * </p>
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#firstMatch(Class, Object)
     *
     * @deprecated - convert to use {@link org.apache.isis.applib.services.repository.RepositoryService#firstMatch(Class, Predicate)} instead
     */
    @Deprecated
    protected <T> T firstMatch(final Class<T> ofType, final T pattern) {
        return getContainer().firstMatch(ofType, pattern);
    }

    /**
     * Returns the first instance of the specified type (including subtypes)
     * that matches the supplied title, or <tt>null</tt> if none.
     *
     * <p>
     * This method is useful during exploration/prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #firstMatch(Query)} for production code.
     * </p>
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#firstMatch(Class, String)
     *
     * @deprecated - convert to use {@link org.apache.isis.applib.services.repository.RepositoryService#firstMatch(Class, Predicate)} instead
     */
    @Deprecated
    protected <T> T firstMatch(final Class<T> ofType, final String title) {
        return getContainer().firstMatch(ofType, title);
    }

    /**
     * Returns the first instance that matches the supplied query, or <tt>null</tt> if none.
     *
     * <p>
     *     This method is the recommended way of querying for an instance when one or more may match.  See also
     *     {@link #uniqueMatch(Query)}.
     * </p>
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#firstMatch(Query)
     * @see #uniqueMatch(Query)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#firstMatch(Class, Predicate)}
     */
    @Deprecated
    protected <T> T firstMatch(final Query<T> query) {
        return getContainer().firstMatch(query);
    }

    // //////////////////////////////////////


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
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#uniqueMatch(Class, Predicate)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#uniqueMatch(Class, Predicate)}
     */
    @Deprecated
    protected <T> T uniqueMatch(final Class<T> ofType, final Predicate<T> predicate) {
        return getContainer().uniqueMatch(ofType, predicate);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#uniqueMatch(Class, Predicate)}
     */
    @Deprecated
    protected <T> T uniqueMatch(final Class<T> ofType, final Filter<T> filter) {
        return getContainer().uniqueMatch(ofType, filter);
    }

    /**
     * Find the only instance of the specified type (including subtypes) that
     * has the specified title.
     *
     * <p>
     * If no instance is found then <tt>null</tt> will be returned, while if
     * there is more that one instances a run-time exception will be thrown.
     *
     * <p>
     * This method is useful during exploration/prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #uniqueMatch(Query)} for production code.
     * </p>
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#uniqueMatch(Class, String)
     *
     * @deprecated - convert to use {@link org.apache.isis.applib.services.repository.RepositoryService#uniqueMatch(Class, Predicate)} instead
     */
    @Deprecated
    protected <T> T uniqueMatch(final Class<T> ofType, final String title) {
        return getContainer().uniqueMatch(ofType, title);
    }

    /**
     * Find the only instance of the patterned object type (including subtypes)
     * that matches the set fields in the pattern object: where any property
     * that is set will be tested and properties that are not set will be
     * ignored.
     *
     * <p>
     * If no instance is found then null will be return, while if there is more
     * that one instances a run-time exception will be thrown.
     * </p>
     *
     * <p>
     * This method is useful during exploration/prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #uniqueMatch(Query)} for production code.
     * </p>
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#uniqueMatch(Class, Object)
     * @deprecated - convert to use {@link org.apache.isis.applib.services.repository.RepositoryService#uniqueMatch(Class, Predicate)} instead
     */
    @Deprecated
    protected <T> T uniqueMatch(final Class<T> ofType, final T pattern) {
        return getContainer().uniqueMatch(ofType, pattern);
    }

    /**
     * Find the only instance that matches the provided query.
     *
     * <p>
     * If no instance is found then null will be return, while if there is more
     * that one instances a run-time exception will be thrown.
     * </p>
     *
     * <p>
     *     This method is the recommended way of querying for (precisely) one instance.  See also {@link #firstMatch(Query)}
     *     for less strict querying.
     * </p>
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see #firstMatch(Query)
     * @see DomainObjectContainer#uniqueMatch(Query)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#uniqueMatch(Query)}
     */
    @Deprecated
    protected <T> T uniqueMatch(final Query<T> query) {
        return getContainer().uniqueMatch(query);
    }

    // //////////////////////////////////////

    /**
     * Whether the object is in a valid state, that is that none of the
     * validation of properties, collections and object-level is vetoing.
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see #validate(Object)
     * @see DomainObjectContainer#isValid(Object)
     */
    protected boolean isValid(final Object domainObject) {
        return getContainer().isValid(domainObject);
    }

    /**
     * The reason, if any why the object is in a invalid state
     *
     * <p>
     * Checks the validation of all of the properties, collections and
     * object-level.
     * </p>
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see #isValid(Object)
     * @see DomainObjectContainer#validate(Object)
     */
    protected String validate(final Object domainObject) {
        return getContainer().validate(domainObject);
    }

    // //////////////////////////////////////

    /**
     * Determines if the specified object is persistent (that it is stored permanently outside of the virtual machine
     * in the object store).
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#isPersistent(Object)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#isPersistent(Object)}
     */
    @Deprecated
    protected boolean isPersistent(final Object domainObject) {
        return getContainer().isPersistent(domainObject);
    }

    /**
     * Queues up a request to persist this object to the object store.  The object is persisted either
     * when the transaction is committed, or when it is flushed.  Flushing is performed either implicitly whenever a subsequent query is run,
     * or can be performed explicitly using {@link DomainObjectContainer#flush()}.
     *
     * <p>
     * It is recommended that the object be initially instantiated using
     * {@link #newTransientInstance(Class)}.  However, the framework will also
     * handle the case when the object is simply <i>new()</i>ed up.  See
     * {@link #newTransientInstance(Class)} for more information.
     * </p>
     *
     * <p>
     * This method will throw an exception if the object {@link #isPersistent(Object) is persistent} already.  For this reason
     * {@link #persistIfNotAlready(Object)} is generally preferred.
     * </p>
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see #newTransientInstance(Class)
     * @see #isPersistent(Object)
     * @see #persistIfNotAlready(Object)
     * @see DomainObjectContainer#persist(Object)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#persist(Object)} or {@link org.apache.isis.applib.services.repository.RepositoryService#persistAndFlush(Object)}
     */
    @Deprecated
    protected <T> T persist(final T transientDomainObject) {
        getContainer().persist(transientDomainObject);
        return transientDomainObject;
    }

    /**
     * Queues up a request to persist this object to the object store (as per {@link #persist(Object)}) (or do
     * nothing if the object is already {@link #isPersistent(Object) persistent}).
     *
     * <p>
     * It is recommended that the object be initially instantiated using
     * {@link #newTransientInstance(Class)}.  However, the framework will also
     * handle the case when the object is simply <i>new()</i>ed up.  See
     * {@link #newTransientInstance(Class)} for more information.
     * </p>
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see #newTransientInstance(Class)
     * @see #isPersistent(Object)
     * @see #persist(Object)
     * @see {@link DomainObjectContainer#persistIfNotAlready(Object)}
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#persist(Object)} or {@link org.apache.isis.applib.services.repository.RepositoryService#persistAndFlush(Object)}
     */
    @Deprecated
    protected <T> T persistIfNotAlready(final T domainObject) {
        getContainer().persistIfNotAlready(domainObject);
        return domainObject;
    }

    /**
     * Delete the provided object from the persistent object store.
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#remove(Object)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#remove(Object)} or {@link org.apache.isis.applib.services.repository.RepositoryService#removeAndFlush(Object)}
     */
    @Deprecated
    protected <T> T remove(final T persistentDomainObject) {
        getContainer().remove(persistentDomainObject);
        return persistentDomainObject;
    }

    /**
     * Delete the provided object from the persistent object store.
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#remove(Object)} or {@link org.apache.isis.applib.services.repository.RepositoryService#removeAndFlush(Object)}
     */
    @Deprecated
    protected <T> T removeIfNotAlready(final T persistentDomainObject) {
        getContainer().removeIfNotAlready(persistentDomainObject);
        return persistentDomainObject;
    }
    
    // //////////////////////////////////////

    /**
     * Display the specified message to the user, in a non-intrusive fashion.
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#informUser(String)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#informUser(String)}
     */
    @Deprecated
    protected void informUser(final String message) {
        getContainer().informUser(message);
    }

    /**
     * Display the specified i18n message to the user, in a non-intrusive fashion.
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#informUser(TranslatableString, Class, String)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#informUser(TranslatableString, Class, String)}
     */
    @Deprecated
    protected void informUser(TranslatableString message, final Class<?> contextClass, final String contextMethod) {
        getContainer().informUser(message, contextClass, contextMethod);
    }

    /**
     * Display the specified message as a warning to the user, in a more visible
     * fashion, but without requiring explicit acknowledgement.
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#warnUser(String)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#warnUser(String)}
     */
    @Deprecated
    protected void warnUser(final String message) {
        getContainer().warnUser(message);
    }

    /**
     * Display the specified i18n message as a warning to the user, in a more visible
     * fashion, but without requiring explicit acknowledgement.
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#warnUser(TranslatableString, Class, String)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#warnUser(TranslatableString, Class, String)}
     */
    @Deprecated
    protected void warnUser(TranslatableString message, final Class<?> contextClass, final String contextMethod) {
        getContainer().warnUser(message, contextClass, contextMethod);
    }

    /**
     * Display the specified message as an error to the user, ensuring that it
     * is acknowledged.
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#raiseError(String)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#raiseError(String)}
     */
    @Deprecated
    protected void raiseError(final String message) {
        getContainer().raiseError(message);
    }

    /**
     * Display the specified i18n message as an error to the user, ensuring that it
     * is acknowledged.
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#raiseError(TranslatableString, Class, String)
     *
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#raiseError(TranslatableString, Class, String)}
     */
    @Deprecated
    protected String raiseError(TranslatableString message, final Class<?> contextClass, final String contextMethod) {
        return getContainer().raiseError(message, contextClass, contextMethod);
    }

    // //////////////////////////////////////

    /**
     * Get the details about the current user.
     *
     * <p>
     * The method simply delegates to the {@link DomainObjectContainer}.
     * </p>
     *
     * @see DomainObjectContainer#getUser()
     *
     * @deprecated - use {@link UserService#getUser()}
     */
    @Deprecated
    protected UserMemento getUser() {
        return getContainer().getUser();
    }

    // //////////////////////////////////////


    @javax.inject.Inject
    private DomainObjectContainer container;

    /**
     * This field is not persisted, nor displayed to the user.
     *
     * @deprecated
     */
    @Deprecated
    protected DomainObjectContainer getContainer() {
        return this.container;
    }

    /**
     * @deprecated - will use the field to inject instead.
     */
    @Deprecated
    @Programmatic
    public void setContainer(final DomainObjectContainer container) {
        this.container = container;
    }

}
