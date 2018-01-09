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

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
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

    //region > commit (DEPRECATED)

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

    //region > newViewModelInstance


    /**
     * Create a new {@link ViewModel} instance of the specified type, initializing with the specified memento.
     *
     */
    @Programmatic
    <T> T newViewModelInstance(final Class<T> ofType, final String memento);

    //endregion


    //region > isValid, validate

    /**
     * Whether the object is in a valid state, that is that none of the
     * validation of properties, collections and object-level is vetoing.
     * 
     * @see #validate(Object)
     *
     * @deprecated - semantics of when this can be called in the object's lifecycle is not well-defined
     */
    @Deprecated
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
     *
     * @deprecated - semantics of when this can be called in the object's lifecycle is not well-defined
     */
    @Deprecated
    @Programmatic
    String validate(Object domainObject);

    //endregion

    //region > isViewModel

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.metamodel.MetaModelService#sortOf(Class, MetaModelService.Mode)} instead.
     */
    @Deprecated
    @Programmatic
    boolean isViewModel(Object domainObject);

    //endregion

    //region > persist, remove (DEPRECATED)

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#persist(Object)} instead. Please note that {@link org.apache.isis.applib.services.repository.RepositoryService#persist(Object)} will not throw an exception if the Domain Object is already persistent, so the implementation will be the same as that of {@link org.apache.isis.applib.services.repository.RepositoryService#persist(Object)}instead.
     */
    @Deprecated
    @Programmatic
    void persist(Object domainObject);

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#remove(Object)} instead.
     */
    @Deprecated
    @Programmatic
    void remove(Object persistentDomainObject);

    //endregion


}
