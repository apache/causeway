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
 * The {@link org.apache.isis.applib.fixturescripts.FixtureScriptsSpecificationProvider} SPI service is an
 * alternative to subclassing the {@link org.apache.isis.applib.fixturescripts.FixtureScripts} domain service.
 * The logic that would normally be in the subclass moves to the provider service instead, and the framework
 * instantiates a fallback default instance, {@link org.apache.isis.applib.fixturescripts.FixtureScriptsDefault}.
 *
 * @see <a href="http://isis.apache.org/migration-notes/migration-notes.html#_migration-notes_1.8.0-to-1.9.0_fixture-scripts-specification-provider">Reference guide</a>
 */
package org.apache.isis.applib.services.fixturespec;
