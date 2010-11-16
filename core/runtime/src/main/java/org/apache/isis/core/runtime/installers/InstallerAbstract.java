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


package org.apache.isis.core.runtime.installers;

import java.util.Collections;
import java.util.List;

import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.metamodel.config.ConfigurationBuilder;
import org.apache.isis.core.metamodel.config.ConfigurationBuilderAware;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.runtime.persistence.PersistenceMechanismInstaller;

import com.google.inject.Module;
import com.google.inject.internal.Lists;
import com.google.inject.util.Modules;

public abstract class InstallerAbstract implements Installer,
		ConfigurationBuilderAware {

	private final String type;
	private final String name;
	
	private ConfigurationBuilder configurationBuilder;
	private IsisConfiguration configuration;

	/**
	 * Subclasses should pass in the type defined as a constant in the
	 * subinterface of Installer.
	 * 
	 * <p>
	 * For example, {@link PersistenceMechanismInstaller} has a constant
	 * {@link PersistenceMechanismInstaller#TYPE}. Any implementation of
	 * {@link PersistenceMechanismInstaller} should pass this constant value up
	 * to this constructor.
	 */
	public InstallerAbstract(final String type, final String name) {
		this.type = type;
		this.name = name;
	}

	public String getType() {
		return type;
	}

	
	public String getName() {
		return name;
	}

	/**
	 * Returns <tt>[type.properties, type_name.properties</tt>.
	 * 
	 * <p>
	 * For example, <tt>[persistor.properties, persistor_in-memory.properties]</tt>.
	 * 
	 * @see #getType()
	 * @see #getName()
	 */
	public List<String> getConfigurationResources() {
		List<String> resourceList = Lists.newArrayList();
		String componentFile = getType() + ".properties";
		resourceList.add(componentFile);
		String componentImplementationFile = getType() + "_" + getName() + ".properties";
		resourceList.add(componentImplementationFile);
		addConfigurationResources(resourceList);
		return Collections.unmodifiableList(resourceList);
	}

	/**
	 * Optional hook method to allow subclasses to specify any additional config resources.
	 */
	protected void addConfigurationResources(List<String> configurationResources) {
	}

	//////////////////////////////////////////////////////
	// init, shutdown
	//////////////////////////////////////////////////////
	
	/**
	 * Default implementation does nothing.
	 */
	public void init() {
		// no-op implementation, subclasses may override!
	}

	/**
	 * Default implementation does nothing.
	 */
	public void shutdown() {
		// no-op implementation, subclasses may override!
	}


	/**
	 * Either this method or {@link #setConfiguration(IsisConfiguration)}
	 * should be called prior to calling {@link #getConfiguration()}.
	 * 
	 * <p>
	 * If a {@link #setConfiguration(IsisConfiguration) configuration}
	 * has already been provided, then throws {@link IllegalStateException}.
	 */
	public void setConfigurationBuilder(
			ConfigurationBuilder configurationBuilder) {
		if (configuration != null) {
			throw new IllegalStateException(
					"A IsisConfiguration has already been provided.");
		}
		this.configurationBuilder = configurationBuilder;
	}

	/**
	 * Either this method or
	 * {@link #setConfigurationBuilder(ConfigurationBuilder)} should be called
	 * prior to calling {@link #getConfiguration()}.
	 * 
	 * <p>
	 * If a {@link #setConfigurationBuilder(ConfigurationBuilder) configuration
	 * builder} has already been provided, then throws
	 * {@link IllegalStateException}.
	 */
	public void setConfiguration(IsisConfiguration configuration) {
		if (configurationBuilder != null) {
			throw new IllegalStateException(
					"A IsisConfiguration has already been provided.");
		}
		this.configuration = configuration;
	}

	/**
	 * Returns a <i>snapshot</i> of the current configuration provided by the
	 * {@link #setConfigurationBuilder(ConfigurationBuilder) injected}
	 * {@link ConfigurationBuilder}.
	 * 
	 * <p>
	 * Implementation note: the implementation is in fact just
	 * {@link InstallerLookupDefault}.
	 */
	public IsisConfiguration getConfiguration() {
		if (configurationBuilder != null) {
			return configurationBuilder.getConfiguration();
		} else if (configuration != null) {
			return configuration;
		} else {
			throw new IllegalStateException(
					"Neither a ConfigurationBuilder nor Configuration has not been provided");
		}
	}
	
	@Override
	public Module getModule() {
		return Modules.EMPTY_MODULE;
	}

	/**
	 * Helper for subclasses implementing {@link #getTypes()}.
	 */
	protected static List<Class<?>> listOf(final Class<?>... classes) {
		return Collections.unmodifiableList(Lists.<Class<?>>newArrayList(classes));
	}

	/**
	 * Helper for subclasses implementing {@link #getTypes()}.
	 */
	protected static List<Class<?>> listOf(List<Class<?>> classList, final Class<?>... classes) {
		List<Class<?>> arrayList = Lists.<Class<?>>newArrayList(classes);
    	arrayList.addAll(0, classList);
		return Collections.unmodifiableList(arrayList);
	}

}

