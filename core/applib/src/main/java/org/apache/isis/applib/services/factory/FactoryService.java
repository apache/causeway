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

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.repository.RepositoryService;

public interface FactoryService {

    /**
     * Create a new instance of the specified class, but do not persist it.
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
     * </p>
     */
    @Programmatic
    <T> T instantiate(final Class<T> domainClass);

    @Programmatic
    default <T> T create(final Class<T> domainClass){
        return instantiate( domainClass);
    };

    @Programmatic
    <T> T mixin( Class<T> mixinClass, Object mixedIn);

}
