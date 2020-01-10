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

/**
 * Object Persistor API.
 *
 * <p>
 * Concrete implementations are in the <tt>persistor-xxx</tt> modules.  The
 * role of the {@link PersistenceSession} is to manage the lifecycle of
 * domain objects, creating them, retrieving them, persisting them, deleting them.
 * However, this object management role applies when deployed in client/server mode
 * as well as standalone.
 *
 * <p>
 * There are therefore just two implementations:
 * <ul>
 * <li> the <tt>persistor-objectstore</tt> implementation delegates to an <tt>ObjectAdapterStore</tt>
 *      API that actually persists objects to some persistent store (such as XML or RDBMS)</li>
 * <li> the <tt>persistor-proxy</tt> implementation in effect provides the client-side remoting library,
 *      using the remoting protocol defined in the <tt>remoting-command</tt> module.
 * </ul>
 *
 * <p>
 * Note that the {@link PersistenceSession} both extends a number of superinterfaces as well as uses implementations of
 * various helpers (for example {@link org.apache.isis.ServicesInjector.services.ServicesInjector} and {@link org.apache.isis.core.runtime.system.persistence.runtime.persistence.oidgenerator.OidGenerator}).
 * These superinterfaces and helper interfaces are not normally implemented directly, and it is the
 * responsibility of the {@link PersistenceMechanismInstaller} to ensure that the correct helper objects
 * are passed to the {@link PersistenceSession} implementation.
 */
package org.apache.isis.persistence.jdo.applib.fixturestate;

