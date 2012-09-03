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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.commons.resource.ResourceStreamSourceChainOfResponsibility;
import org.apache.isis.core.commons.resource.ResourceStreamSourceFileSystem;

/**
 * Adapter for {@link IsisConfigurationBuilder}, loading the specified
 * configuration resource (file) from the given {@link ResourceStreamSource}(s).
 * 
 * <p>
 * If a property is in multiple configuration resources then the latter
 * resources will overwrite the former.
 */
public class IsisConfigurationBuilderResourceStreams implements IsisConfigurationBuilder {

    private static final Logger LOG = Logger.getLogger(IsisConfigurationBuilderResourceStreams.class);

    private final Set<String> configurationResourcesFound = Sets.newLinkedHashSet();
    private final Set<String> configurationResourcesNotFound = Sets.newLinkedHashSet();
    
    static class ConfigurationResourceAndPolicy {
        private final String configurationResource;
        private final NotFoundPolicy notFoundPolicy;

        public ConfigurationResourceAndPolicy(final String configurationResource, final NotFoundPolicy notFoundPolicy) {
            this.configurationResource = configurationResource;
            this.notFoundPolicy = notFoundPolicy;
        }

        public String getConfigurationResource() {
            return configurationResource;
        }

        public NotFoundPolicy getNotFoundPolicy() {
            return notFoundPolicy;
        }

        @Override
        public String toString() {
            return String.format("%s{%s}", configurationResource, notFoundPolicy);
        }
    }

    private final ResourceStreamSource resourceStreamSource;

    private final List<ConfigurationResourceAndPolicy> configurationResources = new ArrayList<ConfigurationResourceAndPolicy>();
    private final Properties additionalProperties = new Properties();
    private boolean includeSystemProperties = false;

    /**
     * Most recent snapshot of {@link IsisConfiguration} obtained from
     * {@link #configurationLoader}.
     * 
     * <p>
     * Whenever further configuration is merged in, this cache is invalidated.
     */
    private IsisConfiguration cachedConfiguration;

    // ////////////////////////////////////////////////////////////
    // Constructor, initialization
    // ////////////////////////////////////////////////////////////

    private static ResourceStreamSource createComposite(final ResourceStreamSource... resourceStreamSources) {
        final ResourceStreamSourceChainOfResponsibility composite = new ResourceStreamSourceChainOfResponsibility();
        for (final ResourceStreamSource rss : resourceStreamSources) {
            if (rss == null) {
                continue;
            }
            composite.addResourceStreamSource(rss);
        }
        return composite;
    }

    public IsisConfigurationBuilderResourceStreams() {
        this(ResourceStreamSourceFileSystem.create(ConfigurationConstants.DEFAULT_CONFIG_DIRECTORY));
    }

    public IsisConfigurationBuilderResourceStreams(final ResourceStreamSource... resourceStreamSources) {
        this(createComposite(resourceStreamSources));
    }

    public IsisConfigurationBuilderResourceStreams(final ResourceStreamSource resourceStreamSource) {
        this.resourceStreamSource = resourceStreamSource;
        addDefaultConfigurationResources();
    }

    /**
     * May be overridden by subclasses if required.
     */
    protected void addDefaultConfigurationResources() {
        addConfigurationResource(ConfigurationConstants.DEFAULT_CONFIG_FILE, NotFoundPolicy.FAIL_FAST);
        addConfigurationResource(ConfigurationConstants.WEB_CONFIG_FILE, NotFoundPolicy.CONTINUE);
    }

    // ////////////////////////////////////////////////////////////
    // ResourceStreamSource
    // ////////////////////////////////////////////////////////////

    @Override
    public ResourceStreamSource getResourceStreamSource() {
        return resourceStreamSource;
    }

    // ////////////////////////////////////////////////////////////
    // populating or updating
    // ////////////////////////////////////////////////////////////

    /**
     * Registers the configuration resource (usually, a file) with the specified
     * name from the first {@link ResourceStreamSource} available.
     * 
     * <p>
     * If the configuration resource cannot be found then the provided
     * {@link NotFoundPolicy} determines whether an exception is thrown or not.
     * 
     * <p>
     * Must be called before {@link #getConfiguration()}; the resource is
     * actually read on {@link #getConfiguration()}.
     */
    @Override
    public synchronized void addConfigurationResource(final String configurationResource, final NotFoundPolicy notFoundPolicy) {
        configurationResources.add(new ConfigurationResourceAndPolicy(configurationResource, notFoundPolicy));
        invalidateCache();
    }

    public synchronized void setIncludeSystemProperties(final boolean includeSystemProperties) {
        this.includeSystemProperties = includeSystemProperties;
        invalidateCache();
    }

    /**
     * Adds additional property.
     */
    @Override
    public synchronized void add(final String key, final String value) {
        if (key == null || value == null) {
            return;
        }
        additionalProperties.setProperty(key, value);
        if (LOG.isInfoEnabled()) {
            LOG.info("added " + key + "=" + value);
        }
        invalidateCache();
    }

    /**
     * Adds additional properties.
     */
    @Override
    public synchronized void add(final Properties properties) {
        final Enumeration<?> keys = properties.propertyNames();
        while (keys.hasMoreElements()) {
            final String key = (String) keys.nextElement();
            add(key, properties.getProperty(key));
        }
        invalidateCache();
    }

    // ////////////////////////////////////////////////////////////
    // getConfiguration
    // ////////////////////////////////////////////////////////////

    /**
     * Returns the current {@link IsisConfiguration configuration}.
     */
    @Override
    public synchronized IsisConfiguration getConfiguration() {
        if (cachedConfiguration != null) {
            return cachedConfiguration;
        }

        final IsisConfigurationDefault configuration = new IsisConfigurationDefault(getResourceStreamSource());
        loadConfigurationResources(configuration);
        // TODO: this hack should move elsewhere, where the DeploymentType is
        // known.
        addShowExplorationOptionsIfNotSpecified(configuration);
        addSystemPropertiesIfRequested(configuration);
        addAdditionalProperties(configuration);
        return cachedConfiguration = configuration;
    }

    private void loadConfigurationResources(final IsisConfigurationDefault configuration) {
        for (final ConfigurationResourceAndPolicy configResourceAndPolicy : configurationResources) {
            loadConfigurationResource(configuration, configResourceAndPolicy);
        }
    }

    private void loadConfigurationResource(final IsisConfigurationDefault configuration, final ConfigurationResourceAndPolicy configResourceAndPolicy) {
        final String configurationResource = configResourceAndPolicy.getConfigurationResource();
        final NotFoundPolicy notFoundPolicy = configResourceAndPolicy.getNotFoundPolicy();
        if (LOG.isDebugEnabled()) {
            LOG.debug("loading configuration resource: " + configurationResource + ", notFoundPolicy: " + notFoundPolicy);
        }
        loadConfigurationResource(configuration, configurationResource, notFoundPolicy);
    }

    /**
     * Loads the configuration resource (usually, a file) with the specified
     * name from the first {@link ResourceStreamSource} available.
     * 
     * <p>
     * If the configuration resource cannot be found then the provided
     * {@link NotFoundPolicy} determines whether an exception is thrown or not.
     */
    protected void loadConfigurationResource(final IsisConfigurationDefault configuration, final String configurationResource, final NotFoundPolicy notFoundPolicy) {
        try {
            final PropertiesReader propertiesReader = loadConfigurationResource(resourceStreamSource, configurationResource);
            addProperties(configuration, propertiesReader.getProperties());
            configurationResourcesFound.add(configurationResource);
            if (LOG.isDebugEnabled()) {
                LOG.debug("'" + configurationResource + "' FOUND");
            }
            return;
        } catch (final IOException ex) {
            // keep going
        }
        if (notFoundPolicy == NotFoundPolicy.FAIL_FAST) {
            throw new IsisException("failed to load '" + configurationResource + "'; tried using: " + resourceStreamSource.getName());
        } else {
            configurationResourcesNotFound.add(configurationResource);
            if (LOG.isDebugEnabled()) {
                LOG.debug("'" + configurationResource + "' not found, but not needed");
            }
        }
    }

    private PropertiesReader loadConfigurationResource(final ResourceStreamSource resourceStreamSource, final String configurationResource) throws IOException {
        return new PropertiesReader(resourceStreamSource, configurationResource);
    }

    private void addShowExplorationOptionsIfNotSpecified(final IsisConfigurationDefault configuration) {
        if (configuration.getString(ConfigurationConstants.SHOW_EXPLORATION_OPTIONS) == null) {
            configuration.add(ConfigurationConstants.SHOW_EXPLORATION_OPTIONS, "yes");
        }
    }

    private void addSystemPropertiesIfRequested(final IsisConfigurationDefault configuration) {
        if (includeSystemProperties) {
            addProperties(configuration, System.getProperties());
        }
    }

    private void addAdditionalProperties(final IsisConfigurationDefault configuration) {
        addProperties(configuration, additionalProperties);
    }

    protected void addProperties(final IsisConfigurationDefault configuration, final Properties properties) {
        configuration.add(properties);
    }

    private void invalidateCache() {
        cachedConfiguration = null;
    }


    
    // ////////////////////////////////////////////////////////////
    // Logging
    // ////////////////////////////////////////////////////////////

    @Override
    public void dumpResourcesToLog() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Configuration resources FOUND:");
            for (String resource : configurationResourcesFound) {
                LOG.info("*  " + resource);
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Configuration resources NOT FOUND (but not needed):");
            for (String resource : configurationResourcesNotFound) {
                LOG.info("*  " + resource);
            }
        }
    }


    // ////////////////////////////////////////////////////////////
    // Injectable
    // ////////////////////////////////////////////////////////////

    @Override
    public void injectInto(final Object candidate) {
        if (IsisConfigurationBuilderAware.class.isAssignableFrom(candidate.getClass())) {
            final IsisConfigurationBuilderAware cast = IsisConfigurationBuilderAware.class.cast(candidate);
            cast.setConfigurationBuilder(this);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("resourceStream", resourceStreamSource).add("configResources", configurationResources).toString();
    }

}
