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

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.xactn.TransactionService;

/**
 * A domain service that acts as a framework's container for managing the 
 * domain objects, and which provides functionality to those domain objects
 * in order that they might interact or have knowledge of with the "outside world".
 *
 * @deprecated - use {@link MessageService}, {@link TitleService}, {@link RepositoryService}, {@link FactoryService}, {@link UserService}, {@link TransactionService}.
 */
@Deprecated
public interface DomainObjectContainer {

    //region > resolve, objectChanged (DEPRECATED)

    /**
     * Re-initialises the fields of an object, using the
     * JDO {@link javax.jdo.PersistenceManager#refresh(Object) refresh} API.
     *
     * <p>
     *     Previously this method was provided for manual control of lazy loading; with the JDO/DataNucleus objectstore
     *     that original functionality is performed automatically by the framework.
     * </p>
     *
     * @deprecated - equivalent to {@link org.apache.isis.applib.services.jdosupport.IsisJdoSupport#refresh(Object)}.
     */
    @Programmatic
    @Deprecated
    void resolve(Object domainObject);

    /**
     * Provided that the <tt>field</tt> parameter is <tt>null</tt>, re-initialises the fields of an object, using the
     * JDO {@link javax.jdo.PersistenceManager#refresh(Object) refresh} API.
     *
     * <p>
     *     Previously this method was provided for manual control of lazy loading; with the JDO/DataNucleus objectstore
     *     that original functionality is performed automatically by the framework.
     * </p>
     *
     * @deprecated - equivalent to {@link org.apache.isis.applib.services.jdosupport.IsisJdoSupport#refresh(Object)}.
     */
    @Programmatic
    @Deprecated
    void resolve(Object domainObject, Object field);

    /**
     * This method does nothing (is a no-op).
     *
     * <p>
     *     Previous this method was provided for manual control of object dirtyng; with the JDO/DataNucleus objectstore
     *     that original functionality is performed automatically by the framework.
     * </p>
     *
     * @deprecated - is a no-op
     */
    @Programmatic
    @Deprecated
    void objectChanged(Object domainObject);


    //endregion

    //region > flush, commit (DEPRECATED)

    /**
     * Flush all changes to the object store.
     * 
     * <p>
     * Occasionally useful to ensure that newly persisted domain objects
     * are flushed to the database prior to a subsequent repository query. 
     * 
     * @return  - is never used, always returns <tt>false</tt>.
     *
     * @deprecated - use {@link TransactionService#flushTransaction()}.
     */
    @Programmatic
    @Deprecated
    boolean flush();

    /**
     * Commit all changes to the object store.
     * 
     * <p>
     * This has been deprecated because the demarcation of transaction
     * boundaries is a framework responsibility rather than being a
     * responsibility of the domain object model.
     * 
     * @deprecated
     */
    @Programmatic
    @Deprecated
    void commit();

    //endregion

    //region > new{Transient/Persistent}Instance

    /**
    * @deprecated - use {@link org.apache.isis.applib.services.factory.FactoryService#instantiate(Class)} instead.
     */
    @Deprecated
    @Programmatic
    <T> T newTransientInstance(final Class<T> ofType);

    /**
     * Create a new {@link ViewModel} instance of the specified type, initializing with the specified memento.
     *
     * <p>
     *     Rather than use this constructor it is generally preferable to simply instantiate a
     *     class annotated with {@link org.apache.isis.applib.annotation.ViewModel annotation}.
     *     If services need injecting into it, use {@link #injectServicesInto(Object)}.
     * </p>
     */
    @Programmatic
    <T> T newViewModelInstance(final Class<T> ofType, final String memento);

    /**
     * @deprecated - not supported, will throw a RuntimeException
     */
    @Deprecated
    @Programmatic
    <T> T newAggregatedInstance(Object parent, Class<T> ofType);

    /**
     * (Deprecated) returns a new instance of the specified class that will have been
     * persisted.
     *
     * @deprecated - in almost all cases the workflow is to {@link #newTransientInstance(Class)}, populate the object
     * (eg with the arguments to an action) and then to
     * {@link #persist(Object) persist) the object.  It is exceptionally rare for
     * an object to be created, and with no further data required - be in a state
     * to be persisted immediately.
     */
    @Programmatic
    @Deprecated
    <T> T newPersistentInstance(final Class<T> ofType);

    /**
     * Returns a new instance of the specified class that has the same persisted
     * state (either transient or persisted) as the provided object.
     * 
     * <p>
     * This method has been deprecated because it is a rare use case, causing
     * unnecessary interface bloat for very little gain.
     * <p></p>
     * 
     * @deprecated
     */
    @Programmatic
    @Deprecated
    <T> T newInstance(final Class<T> ofType, final Object object);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.factory.FactoryService#mixin(Class, Object)} instead.
     */
    @Deprecated
    @Programmatic
    <T> T mixin( Class<T> mixinClass, Object mixedIn);

    //endregion

    //region > injectServicesInto, lookupService, lookupServices (DEPRECATED)

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.registry.ServiceRegistry#injectServicesInto(Object)} instead.
     */
    @Deprecated
    @Programmatic
    <T> T injectServicesInto(final T domainObject);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.registry.ServiceRegistry#lookupService(Class)} instead.
     */
    @Deprecated
    @Programmatic
    <T> T lookupService(Class<T> service);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.registry.ServiceRegistry#lookupServices(Class)} instead.
     */
    @Deprecated
    @Programmatic
    <T> Iterable<T> lookupServices(Class<T> service);


    //endregion

    //region > isValid, validate

    /**
     * Whether the object is in a valid state, that is that none of the
     * validation of properties, collections and object-level is vetoing.
     * 
     * @see #validate(Object)
     */
    @Programmatic
    boolean isValid(Object domainObject);

    /**
     * The reason, if any why the object is in a invalid state
     * 
     * <p>
     * Checks the validation of all of the properties, collections and
     * object-level.
     * </p>
     *
     * @see #isValid(Object)
     */
    @Programmatic
    String validate(Object domainObject);

    //endregion

    //region > isViewModel

    @Programmatic
    boolean isViewModel(Object domainObject);

    //endregion

    //region > isPersistent, persist, remove (DEPRECATED)

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#isPersistent(Object)} instead.
     */
    @Deprecated
    @Programmatic
    boolean isPersistent(Object domainObject);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#persist(Object)} instead. Please note that {@link org.apache.isis.applib.services.repository.RepositoryService#persist(Object)} will not throw an exception if the Domain Object is already persistent, so the implementation will be the same as that of {@link org.apache.isis.applib.services.repository.RepositoryService#persistIfNotAlready(Object)} (or the equivalent, deprecated {@link org.apache.isis.applib.DomainObjectContainer#persistIfNotAlready(Object)}) instead.
     */
    @Deprecated
    @Programmatic
    void persist(Object domainObject);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#persist(Object)} instead.
     */
    @Deprecated
    @Programmatic
    void persistIfNotAlready(Object domainObject);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#remove(Object)} instead.
     */
    @Deprecated
    @Programmatic
    void remove(Object persistentDomainObject);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#remove(Object)} instead.
     */
    @Deprecated
    @Programmatic
    void removeIfNotAlready(Object domainObject);

    //endregion

    //region > info, warn, error (DEPRECATED)

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#informUser(String)} instead.
     */
    @Deprecated
    @Programmatic
    void informUser(String message);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#informUser(TranslatableString, Class, String)} instead.
     */
    @Deprecated
    @Programmatic
    String informUser(TranslatableString message, final Class<?> contextClass, final String contextMethod);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#warnUser(String)} instead.
     */
    @Deprecated
    @Programmatic
    void warnUser(String message);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#warnUser(TranslatableString, Class, String)} instead.
     */
    @Deprecated
    @Programmatic
    String warnUser(TranslatableString message, final Class<?> contextClass, final String contextMethod);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#raiseError(String)} instead.
     */
    @Deprecated
    @Programmatic
    void raiseError(String message);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#raiseError(TranslatableString, Class, String)} instead.
     */
    @Deprecated
    @Programmatic
    String raiseError(TranslatableString message, final Class<?> contextClass, final String contextMethod);

    //endregion

    //region > properties (DEPRECATED)

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.config.ConfigurationService#getProperty(String)} instead.
     */
    @Deprecated
    @Programmatic
    String getProperty(String name);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.config.ConfigurationService#getProperty(String, String)} instead.
     */
    @Deprecated
    @Programmatic
    String getProperty(String name, String defaultValue);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.config.ConfigurationService#getPropertyNames()} instead.
     */
    @Deprecated
    @Programmatic
    List<String> getPropertyNames();

    //endregion

    //region > security

    /**
     * @deprecated - use {@link UserService#getUser()} instead
     */
    @Deprecated
    @Programmatic
    UserMemento getUser();

    //endregion


}
