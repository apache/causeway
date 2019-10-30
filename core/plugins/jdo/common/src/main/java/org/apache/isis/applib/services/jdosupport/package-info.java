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
 * The {@link org.apache.isis.applib.services.jdosupport.IsisJdoSupport} service provides a number of general purpose
 * methods for working with the JDO/DataNucleus objectstore. In general these act at a lower-level of abstraction than
 * the APIs normally used (specifically, those of {@link org.apache.isis.applib.services.repository.RepositoryService}),
 * but nevertheless deal with some of the most common use cases. For service also provides access to the underlying
 * JDO PersistenceManager for full control.
 *
 * @see <a href="http://isis.apache.org/guides/rgsvc/rgsvc.html#_rgsvc_persistence-layer-api_IsisJdoSupport">Reference guide</a>
 */
package org.apache.isis.applib.services.jdosupport;