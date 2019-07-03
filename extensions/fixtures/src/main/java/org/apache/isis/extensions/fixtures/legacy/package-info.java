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
 * Defines the {@link org.apache.isis.extensions.fixtures.legacy.InstallableFixture}
 * interface and supporting classes.
 *
 * <p>
 *     <b>NOTE THAT</b> {@link org.apache.isis.extensions.fixtures.legacy.fixturescripts.FixtureScript} interface provides a more complete
 *     approach to handling fixtures, and should generally be used in preference to
 *     {@link org.apache.isis.extensions.fixtures.legacy.InstallableFixture}.
 * </p>
 *
 * <p>
 * Fixtures are used to initialize the system, typically for either testing
 * or for demo/prototyping purposes.  Initializing the system means:
 * <ul>
 * <li><p>setting up objects within the persistent object store (typically only relevant
 * if using the in-memory object store; other object stores will generally
 * ignore these fixtures)</p></li>
 * <li><p>setting the current {@link org.apache.isis.applib.fixtures.DateFixture date}</p></li>
 * <li><p>{@link org.apache.isis.applib.fixtures.SwitchUserFixture switching}
 * the current user while fixtures are being installed (eg so that a workflow
 * can be picked up midway through)</p></li>
 * <li><p>specifying the {@link org.apache.isis.applib.fixtures.LogonFixture currently logged on} user once the fixtures have been installed</p></li>
 * </ul>
 *
 */
package org.apache.isis.extensions.fixtures.legacy;