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
 * Defines the {@link org.apache.isis.config.IsisConfiguration}
 * which collects an immutable set of configuration options (like a hashmap),
 * along with a number of supporting classes.
 *
 * <p>
 * Chief among these supporting classes is {@link org.apache.isis.config.builder.IsisConfigurationBuilder},
 * which holds a (mutable) collection of properties and is used to build an
 * {@link org.apache.isis.config.IsisConfiguration}.  The
 * {@link org.apache.isis.config.builder.IsisConfigurationBuilder} and
 * {@link org.apache.isis.config.IsisConfiguration} types form
 * an mutable/immutable pair (cf {@link java.lang.StringBuilder} / {@link java.lang.String}).
 *
 * <p>
 * The {@link org.apache.isis.config.builder.IsisConfigurationBuilder} is used
 * by {@link org.apache.isis.core.commons.components.InstallerAbstract}, an
 * implementation of {@link org.apache.isis.core.commons.components.Installer}
 * that allows the configuration to be added to as each component is
 * installed.
 */
package org.apache.isis.config;