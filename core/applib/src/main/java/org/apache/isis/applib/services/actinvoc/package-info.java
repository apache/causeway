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
 * The @{@link org.apache.isis.applib.services.actinvoc.ActionInvocationContext} domain service is a
 * <code>@RequestScoped</code> service intended to support the implementation of "bulk" actions annotated with
 * {@link org.apache.isis.applib.annotation.Action#invokeOn()}.
 *
 * <p>
 * This allows the user to select multiple objects in a table and then invoke the same action against all of them.
 * </p>
 *
 * @see <a href="http://isis.apache.org/guides/rgsvc/rgsvc.html#_rgsvc_application-layer-api_ActionInvocationContext">Reference guide</a>
 */
package org.apache.isis.applib.services.actinvoc;