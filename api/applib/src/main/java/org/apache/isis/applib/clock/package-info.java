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
 * Defines a {@link org.apache.isis.legacy.applib.clock.Clock} singleton
 * that can be used to obtain the current time.
 *
 * <p>
 * All of the built-in value types relating to time have
 * {@link org.apache.isis.applib.adapters.ValueSemanticsProvider#getDefaultsProvider() defaults}
 * that use the {@link org.apache.isis.legacy.applib.clock.Clock} singleton.  For
 * consistency, domain objects and services should also use the Clock.
 *
 * <p>
 * Providing a clock is useful for testing, allowing the "current" time
 * to be placed under programmatic control (by {@link org.apache.isis.applib.fixtures.FixtureClock#initialize() initializing}
 * a {@link org.apache.isis.applib.fixtures.FixtureClock}.  Otherwise though
 * the {@link org.apache.isis.legacy.applib.clock.Clock} just uses the time from the
 * current system.
 *
 * <p>
 * Note: this design also means that other {@link org.apache.isis.legacy.applib.clock.Clock}
 * implementations - such as a one that accesses the time from an NNTP time
 * daemon - could also be used.
 */
package org.apache.isis.applib.clock;