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
 * Remoting Command API.
 * 
 * <p>
 * Defines two installers, one for client and one for server:
 * <ul>
 * <li> the {@link ClientConnectionInstaller} is an extension of {@link org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceMechanismInstaller},
 *      intended to install a <tt>persistor-proxy</tt> as well as any additional {@link org.apache.isis.core.metamodel.facetdecorator.FacetDecorator}s
 *      for authentication, authorisation and so forth.  The implementation must specify the marshalling mechanism (encoding, xstream etc) as
 *      well as the transport (sockets etc).
 * <li> the {@link IsisViewerInstaller} sets up a listener to run on the server,
 *      for a (marshalling, transport) combination.
 * </ul>
 */
package org.apache.isis.runtimes.dflt.runtime.remoting;

