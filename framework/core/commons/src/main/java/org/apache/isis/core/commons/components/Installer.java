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

package org.apache.isis.core.commons.components;

import java.util.List;

/**
 * A factory for a {@link Component}, defining that component's
 * {@link #getType() type} and its {@link #getName() name}.
 * 
 * <p>
 * The ({@link #getType() type}, {@link #getName() name}) is expected to be a
 * unique identifier of a component.
 * 
 * <p>
 * The <i>default runtime</i> (<tt>org.apache.isis.runtimes.dflt</tt> module),
 * which adopts a service locator design, uses the
 * <tt>installer-registry.properties</tt> resource as a registry of all
 * available installers. The installers are loaded and indexed by their name and
 * type. Other runtime implementations may use different approaches.
 */
public interface Installer extends ApplicationScopedComponent {

    /**
     * The type of the installer, meaning the component type, and consistent
     * with the long option of the command line flag where applicable.
     * 
     * <p>
     * Examples are <tt>authentication</tt> or <tt>persistor</tt>.
     * 
     * <p>
     * Because all implementations of a given subinterface of {@link Installer}
     * should return the same value for this method, by convention these
     * subinterfaces define a constant which the implementation can just return.
     * 
     * <p>
     * Used, with {@link #getName()}, to determine the config files and config
     * keys for this installer.
     * 
     * @see #getConfigurationResources()
     */
    String getType();

    /**
     * The name (qualified by type).
     * 
     * <p>
     * Used, with {@link #getType()}, to determine the config files and config
     * keys for this installer.
     * 
     * @see #getConfigurationResources()
     */
    String getName();

    /**
     * The configuration resources (files) to merge in configuration properties.
     * 
     * <p>
     * For example, would return list of [<tt>persistor.properties</tt>, and
     * <tt>persistor_in-memory.properties</tt>] for the in-memory object store.
     * 
     * <p>
     * The implementation should look under keys prefixed either
     * <tt>isis.persistor</tt> or <tt>isis.persistor.in-memory</tt>.
     * 
     * <p>
     * Note that we use an '_' underscore to join the {@link #getType() type}
     * and {@link #getName() name} in the filenames, but a '.' (period) for the
     * keys.
     */
    List<String> getConfigurationResources();

    /**
     * The (classes of) the types that this installer makes available in the
     * {@link #getModule() module}.
     */
    List<Class<?>> getTypes();
}
