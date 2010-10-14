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


package org.apache.isis.commons.components;

import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * A factory for a component, used during it boot strapping process.
 * 
 * <p>
 * All installers listed (by class name) in
 * <tt>installer-registry.properties</tt> are loaded when the boot strap class
 * is initially loaded. Then named installers are used during boot to create
 * components for the system. The name method specified is the name that the
 * component can be installed by.
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
	 * For example, would return list of [<tt>persistor.properties</tt>,
	 * and <tt>persistor_in-memory.properties</tt>] for the in-memory
	 * object store.
	 * 
	 * <p>
	 * The implementation should look under keys prefixed either
	 * <tt>isis.persistor</tt> or
	 * <tt>isis.persistor.in-memory</tt>.
	 * 
	 * <p>
	 * Note that we use an '_' underscore to join the {@link #getType() type}
	 * and {@link #getName() name} in the filenames, but a '.' (period) for the
	 * keys.
	 */
	List<String> getConfigurationResources();
	
	/**
	 * The {@link Module} used to bootstrap the component.
	 */
	Module getModule();
	
	/**
	 * The (classes of) the types that this installer makes available in the {@link #getModule() module}.
	 */
	List<Class<?>> getTypes();
}

