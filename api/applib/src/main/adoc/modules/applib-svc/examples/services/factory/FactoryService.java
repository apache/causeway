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

package org.apache.isis.applib.services.factory;

import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.commons.exceptions.IsisException;

public interface FactoryService {

    /**
     * Gets an instance (possibly shared or independent) of the specified {@code requiredType}, 
     * with injection points resolved 
     * and any life-cycle callback processed.
     * 
     * @param <T>
     * @param requiredType
     * @return (non-null), an instance of {@code requiredType}, if available and unique
     * (i.e. not multiple candidates found with none marked as primary)
     * 
     * @throws NoSuchElementException if result is empty
     * @throws IsisException if instance creation failed
     * 
     * @apiNote does not force the requiredType to be added to the meta-model
     * 
     * @since 2.0
     */
    <T> T get(Class<T> requiredType);

    /**
     * Creates a new Mixin instance.
     * @param <T>
     * @param mixinClass
     * @param mixedIn
     * @return
     * @apiNote forces the mixinClass to be added to the meta-model if not already
     */
    <T> T mixin(Class<T> mixinClass, Object mixedIn);

    /**
     * Creates a new ViewModel instance, and initializes according to the given {@code mementoStr} 
     * @param viewModelClass
     * @param mementoStr - ignored if {@code null}
     * @apiNote forces the viewModelClass to be added to the meta-model if not already
     * @since 2.0
     */
    <T> T viewModel(Class<T> viewModelClass, @Nullable String mementoStr);

    /**
     * Creates a new ViewModel instance 
     * @param viewModelClass
     * @apiNote forces the viewModelClass to be added to the meta-model if not already
     * @since 2.0
     */
    default <T> T viewModel(Class<T> viewModelClass) {
        return viewModel(viewModelClass, /*mementoStr*/null);
    }

    // -- DEPRECATIONS
    
    /**
     * Creates a new instance of the specified class, but does not persist it.
     *
     * <p>
     * It is recommended that the object be initially instantiated using
     * this method, though the framework will also handle the case when
     * the object is simply <i>new()</i>ed up.  The benefits of using
     * {@link #instantiate(Class)} are:
     * </p>
     *
     * <ul>
     * <li>any services will be injected into the object immediately
     *     (otherwise they will not be injected until the framework
     *     becomes aware of the object, typically when it is
     *     {@link RepositoryService#persist(Object) persist}ed</li>
     * <li>the default value for any properties (usually as specified by
     *     <tt>default<i>Xxx</i>()</tt> supporting methods) will (since 2.0) be
     *     used</li>
     * <li>the <tt>created()</tt> callback will not be called.
     * </ul>
     *
     * <p>
     * The corollary is: if your code never uses <tt>default<i>Xxx</i>()</tt>
     * supporting methods or the <tt>created()</tt> callback, then you can
     * alternatively just <i>new()</i> up the object rather than call this
     * method.
     * </p>
     * @deprecated with semantic changes since 2.0 previous behavior is no longer guaranteed, 
     * instead consider use of {@link #get(Class)} if applicable
     */
    @Deprecated
    <T> T instantiate(Class<T> domainClass);


}
