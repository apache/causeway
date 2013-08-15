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
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(IsisConfigurationBuilderResourceStreams.class);
    
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

    private final Set<String> configurationResourcesFound = Sets.newLinkedHashSet();
    private final Set<String> configurationResourcesNotFound = Sets.newLinkedHashSet();
    private final ResourceStreamSource resourceStreamSource;
    private final IsisConfigurationDefault configuration;
    private final List<ConfigurationResourceAndPolicy> configurationResources = new ArrayList<ConfigurationResourceAndPolicy>();
    private boolean locked;

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
        configuration = new IsisConfigurationDefault(resourceStreamSource);
    }

    /**
     * May be overridden by subclasses if required.
     */
    public void addDefaultConfigurationResources() {
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
        LOG.debug("looking for properties file " + configurationResource);
        loadConfigurationResource(configuration, new ConfigurationResourceAndPolicy(configurationResource, notFoundPolicy));
        configurationResources.add(new ConfigurationResourceAndPolicy(configurationResource, notFoundPolicy));
    }

    /**
     * Adds additional property.
     */
    @Override
    public synchronized void add(final String key, final String value) {
        if (locked) {
            throw new IsisException("Configuration has been locked and cannot be changed");
        }
        configuration.add(key, value);
    }

    public void lockConfiguration() {
        locked = true;
    }
    
    // ////////////////////////////////////////////////////////////
    // getConfiguration
    // ////////////////////////////////////////////////////////////

    /**
     * Returns the current {@link IsisConfiguration configuration}.
     */
    @Override
    public synchronized IsisConfiguration getConfiguration() {
        return configuration;
     }

    private void loadConfigurationResource(final IsisConfigurationDefault configuration, final ConfigurationResourceAndPolicy configResourceAndPolicy) {
        final String configurationResource = configResourceAndPolicy.getConfigurationResource();
        final NotFoundPolicy notFoundPolicy = configResourceAndPolicy.getNotFoundPolicy();
        LOG.debug("checking availability of configuration resource: " + configurationResource + ", notFoundPolicy: " + notFoundPolicy);
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
            LOG.info("loading properties from " + configurationResource);
            configuration.add(propertiesReader.getProperties());
            configurationResourcesFound.add(configurationResource);
            return;
        } catch (final IOException ignore) { }
        if (notFoundPolicy == NotFoundPolicy.FAIL_FAST) {
            throw new IsisException("failed to load '" + configurationResource + "'; tried using: " + resourceStreamSource.getName());
        } else {
            configurationResourcesNotFound.add(configurationResource);
            LOG.debug("'" + configurationResource + "' not found, but not needed");
        }
    }

    private PropertiesReader loadConfigurationResource(final ResourceStreamSource resourceStreamSource, final String configurationResource) throws IOException {
        return new PropertiesReader(resourceStreamSource, configurationResource);
    }

    
    // TODO review this, should this option default to yes?
    private void addShowExplorationOptionsIfNotSpecified(final IsisConfigurationDefault configuration) {
        if (configuration.getString(ConfigurationConstants.SHOW_EXPLORATION_OPTIONS) == null) {
            configuration.add(ConfigurationConstants.SHOW_EXPLORATION_OPTIONS, "yes");
        }
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
