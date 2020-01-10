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
 * The {@link org.apache.isis.applib.services.metrics.MetricsService} is a request-scoped domain service that hooks
 * into the JDO/DataNucleus ObjectStore to provide a number of counters relating to numbers of object loaded,
 * dirtied etc.
 *
 * @see <a href="http://isis.apache.org/guides/rgsvc/rgsvc.html#_rgsvc_persistence-layer-api_MetricsService">Reference guide</a>
 */
package org.apache.isis.applib.services.metrics;