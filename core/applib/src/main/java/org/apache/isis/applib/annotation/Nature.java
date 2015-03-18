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
package org.apache.isis.applib.annotation;

/**
 * The different sorts of domain objects recognized by Isis.
 *
 * <p>
 *     Note that {@link #EXTERNAL_ENTITY} and {@link #VIEW_MODEL} are implemented identically internally; the
 *     difference is one of intent.
 * </p>
 */
public enum Nature {

    /**
     * The default; allows the programmer to combine <tt>@DomainObject</tt> annotation with the
     * <tt>@ViewModel</tt> annotation or implementing the <tt>ViewModel</tt> interface.
     */
    NOT_SPECIFIED,

    /**
     * A domain entity whose persistence is managed internally by Isis, using JDO as the persistence implementation.
     * Domain entities are considered to be part of the domain model layer.
     *
     * <p>
     *     Domain entities are considered to be part of the domain model layer.
     * </p>
     *
     * <p>
     *    Currently implies no additional semantics other than documentation.
     * </p>
     */
    JDO_ENTITY,
    /**
     * A domain entity that is a wrapper/proxy/stub to some externally managed entity.  Domain entities are
     * considered to be part of the domain model layer.
     *
     * <p>
     *     The identity of an external entity is determined solely by the state of entity's properties (that have
     *     not been set to be ignored using {@link org.apache.isis.applib.annotation.Property#notPersisted()}).
     * </p>
     */
    EXTERNAL_ENTITY,
    /**
     * A domain entity that is a wrapper/proxy/stub to a &quot;synthetic&quot; entity, for example one that is
     * constructed from some sort of internal memory data structure.
     *
     * <p>
     *     As for a {@link #EXTERNAL_ENTITY}, the identity of a synthetic entity is determined solely by the state of
     *     entity's properties (that have not been set to be ignored using {@link org.apache.isis.applib.annotation.Property#notPersisted()}).
     * </p>
     */
    INMEMORY_ENTITY,
    /**
     * An object that is conceptually part of the application layer, and which surfaces behaviour and/or state that
     * is aggregate of one or more domain entity.
     *
     * <p>
     *     The identity of a view model is determined solely by the state of object's properties (that have
     *     not been set to be ignored using {@link org.apache.isis.applib.annotation.Property#notPersisted()}).
     * </p>
     */
    VIEW_MODEL
}
