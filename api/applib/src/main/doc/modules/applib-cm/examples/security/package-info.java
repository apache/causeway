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
 * This package defines types that represent the
 * currently logged-in {@link org.apache.isis.applib.security.UserMemento user}
 * and their {@link org.apache.isis.applib.security.RoleMemento role}s.
 *
 * <p>
 * Typically domain objects do not need to have any knowledge of <i>who</i>
 * is using them, because authorization is provided declaratively by the
 * framework and is type-based.  However, there are occasions; for example,
 * only an <tt>Employee</tt> and his superiors might be allowed to view their salary.
 *
 * <p>
 * The types are suffixed &quot;Memento&quot; because they snapshot the user
 * and roles at the time that the user logs in, but are not updated after that
 * point.
 */
package org.apache.isis.applib.security;