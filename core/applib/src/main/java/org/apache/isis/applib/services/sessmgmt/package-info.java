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
 * The {@link org.apache.isis.applib.services.sessmgmt.SessionManagementService} provides the ability to
 * programmatically manage sessions. The primary use case is for fixture scripts or other routines that are invoked
 * from the UI and which create or modify large amounts of data. A classic example is migrating data from one system
 * to another.
 *
 * @see <a href="http://isis.apache.org/guides/rgsvc/rgsvc.html#_rgsvc_application-layer-api_SessionManagementService">Reference guide</a>
 */
package org.apache.isis.applib.services.sessmgmt;