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

package org.apache.isis.core.commons.config;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.components.Installer;

public abstract class InstallerAbstract implements Installer, IsisConfigurationBuilderAware, IsisConfigurationAware {

    private final String type;
    private final String name;

    private IsisConfigurationBuilder isisConfigurationBuilder;
    private IsisConfiguration configuration;

    /**
     * Subclasses should pass in the type defined as a constant in the
     * subinterface of Installer.
     * 
     * <p>
     * For example, <tt>PersistenceMechanismInstaller</tt> has a constant
     * <tt>PersistenceMechanismInstaller#TYPE</tt>. Any implementation of
     * <tt>PersistenceMechanismInstaller</tt> should pass this constant value up
     * to this constructor.
     */
    public InstallerAbstract(final String type, final String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns <tt>[type.properties, type_name.properties</tt>.
     * 
     * <p>
     * For example,
     * <tt>[persistor.properties, persistor_in-memory.properties]</tt>.
     * 
     * @see #getType()
     * @see #getName()
     */
    @Override
    public List<String> getConfigurationResources() {
        final List<String> resourceList = Lists.newArrayList();
        final String componentImplementationFile = getType() + "_" + getName() + ".properties";
        resourceList.add(componentImplementationFile);
        final String componentFile = getType() + ".properties";
        resourceList.add(componentFile);
        addConfigurationResources(resourceList);
        return Collections.unmodifiableList(resourceList);
    }

    /**
     * Optional hook method to allow subclasses to specify any additional config
     * resources.
     */
    protected void addConfigurationResources(final List<String> configurationResources) {
    }

    // ////////////////////////////////////////////////////
    // init, shutdown
    // ////////////////////////////////////////////////////

    /**
     * Default implementation does nothing.
     */
    @Override
    public void init() {
        // no-op implementation, subclasses may override!
    }

    /**
     * Default implementation does nothing.
     */
    @Override
    public void shutdown() {
        // no-op implementation, subclasses may override!
    }

    /**
     * Either this method or {@link #setConfiguration(IsisConfiguration)} should
     * be called prior to calling {@link #getConfiguration()}.
     * 
     * <p>
     * If a {@link #setConfiguration(IsisConfiguration) configuration} has
     * already been provided, then throws {@link IllegalStateException}.
     */
    @Override
    public void setConfigurationBuilder(final IsisConfigurationBuilder isisConfigurationBuilder) {
        if (configuration != null) {
            throw new IllegalStateException("A IsisConfiguration has already been provided.");
        }
        this.isisConfigurationBuilder = isisConfigurationBuilder;
    }

    /**
     * Either this method or
     * {@link #setConfigurationBuilder(IsisConfigurationBuilder)} should be
     * called prior to calling {@link #getConfiguration()}.
     * 
     * <p>
     * If a {@link #setConfigurationBuilder(IsisConfigurationBuilder)
     * configuration builder} has already been provided, then throws
     * {@link IllegalStateException}.
     */
    public void setConfiguration(final IsisConfiguration configuration) {
        if (isisConfigurationBuilder != null) {
            throw new IllegalStateException("A IsisConfigurationBuilder has already been provided.");
        }
        this.configuration = configuration;
    }

    /**
     * Returns a <i>snapshot</i> of the current configuration provided by the
     * {@link #setConfigurationBuilder(IsisConfigurationBuilder) injected}
     * {@link IsisConfigurationBuilder}.
     * 
     * <p>
     * Implementation note: the implementation is in fact just
     * {@link InstallerLookup}.
     */
    public IsisConfiguration getConfiguration() {
        if (isisConfigurationBuilder != null) {
            return isisConfigurationBuilder.getConfiguration();
        } else if (configuration != null) {
            return configuration;
        } else {
            throw new IllegalStateException("Neither a ConfigurationBuilder nor Configuration has not been provided");
        }
    }

    /**
     * Helper for subclasses implementing {@link #getTypes()}.
     */
    protected static List<Class<?>> listOf(final Class<?>... classes) {
        return Collections.unmodifiableList(Lists.<Class<?>> newArrayList(classes));
    }

    /**
     * Helper for subclasses implementing {@link #getTypes()}.
     */
    protected static List<Class<?>> listOf(final List<Class<?>> classList, final Class<?>... classes) {
        final List<Class<?>> arrayList = Lists.<Class<?>> newArrayList(classes);
        arrayList.addAll(0, classList);
        return Collections.unmodifiableList(arrayList);
    }

}
