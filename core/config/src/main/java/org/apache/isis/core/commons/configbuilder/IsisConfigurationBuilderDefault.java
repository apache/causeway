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

package org.apache.isis.core.commons.configbuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest2;
import org.apache.isis.applib.AppManifestAbstract2;
import org.apache.isis.applib.Module;
import org.apache.isis.applib.PropertyResource;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.NotFoundPolicy;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.commons.resource.ResourceStreamSourceChainOfResponsibility;
import org.apache.isis.core.commons.resource.ResourceStreamSourceFileSystem;
import org.apache.isis.core.commons.resource.ResourceStreamSource_UsingClass;

/**
 * Holds a mutable set of properties representing the configuration.
 *
 * This implementation loads the specified
 * configuration resource (file) from the given {@link ResourceStreamSource}(s).
 *
 * <p>
 * If a property is in multiple configuration resources then the latter
 * resources will overwrite the former.
 *
 * <p>
 * Mutable/immutable pair with the {@link IsisConfiguration}. To obtain the
 * configuration, use {@link #getConfiguration()}.
 *
 * @see {@link IsisConfiguration} for more details on the mutable/immutable pair pattern.
 *
 */
final class IsisConfigurationBuilderDefault implements IsisConfigurationBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(IsisConfigurationBuilderDefault.class);

    // -- constructor, fields

    private /*final*/ ResourceStreamSourceChainOfResponsibility resourceStreamSourceChain;

    private IsisConfigurationDefault configuration;

    private final Set<String> configurationResourcesFound = _Sets.newLinkedHashSet();
    private final Set<String> configurationResourcesNotFound = _Sets.newLinkedHashSet();

//    public IsisConfigurationBuilderDefault() {
//        this(ResourceStreamSourceFileSystem.create(ConfigurationConstants.DEFAULT_CONFIG_DIRECTORY));
//    }

//    private IsisConfigurationBuilderDefault(final ResourceStreamSource... resourceStreamSources) {
//        this(createComposite(Arrays.asList(resourceStreamSources)));
//    }

//    private IsisConfigurationBuilderDefault(final List<ResourceStreamSource> resourceStreamSources) {
//        this(createComposite(resourceStreamSources));
//    }
    
    // -- FACTORIES
    
    static IsisConfigurationBuilder empty() {
        ResourceStreamSourceChainOfResponsibility chain = createComposite(Collections.emptyList());
        IsisConfigurationBuilderDefault builder = new IsisConfigurationBuilderDefault(chain);
        return builder;
    }
    
    static IsisConfigurationBuilderDefault getDefault() {
        
        ResourceStreamSourceChainOfResponsibility chain = createComposite(Arrays.asList(
                ResourceStreamSourceFileSystem.create(ConfigurationConstants.DEFAULT_CONFIG_DIRECTORY)        
                ));
        IsisConfigurationBuilderDefault builder = new IsisConfigurationBuilderDefault(chain);
        
        builder.addDefaultPrimers();
        
        builder.addDefaultConfigurationResources();
        
        return builder;
        
    }
    
    // -- 

    private IsisConfigurationBuilderDefault(final ResourceStreamSourceChainOfResponsibility resourceStreamSourceChain) {
        this.resourceStreamSourceChain = resourceStreamSourceChain;
        configuration = new IsisConfigurationDefault(resourceStreamSourceChain);
    }

    private static ResourceStreamSourceChainOfResponsibility createComposite(
            final List<ResourceStreamSource> resourceStreamSources) {
        final ResourceStreamSourceChainOfResponsibility composite = new ResourceStreamSourceChainOfResponsibility();
        for (final ResourceStreamSource rss : resourceStreamSources) {
            if (rss == null) {
                continue;
            }
            composite.addResourceStreamSource(rss);
        }
        return composite;
    }


    private void addDefaultConfigurationResources() {
        IsisConfigurationDefault.ContainsPolicy ignorePolicy = IsisConfigurationDefault.ContainsPolicy.IGNORE;
        NotFoundPolicy continuePolicy = NotFoundPolicy.CONTINUE;

        addConfigurationResource(ConfigurationConstants.DEFAULT_CONFIG_FILE, NotFoundPolicy.FAIL_FAST, ignorePolicy);

        addConfigurationResource(ConfigurationConstants.WEB_CONFIG_FILE, continuePolicy, ignorePolicy);
        addConfigurationResource("war.properties", continuePolicy, ignorePolicy);

        addConfigurationResource("viewer_wicket.properties", continuePolicy, ignorePolicy);
        addConfigurationResource("viewer_restful.properties", continuePolicy, ignorePolicy);
        addConfigurationResource("viewer_restfulobjects.properties", continuePolicy, ignorePolicy);

        addConfigurationResource("persistor_datanucleus.properties", continuePolicy, ignorePolicy);
        addConfigurationResource("persistor.properties", continuePolicy, ignorePolicy);

        addConfigurationResource("authentication_shiro.properties", continuePolicy, ignorePolicy);
        addConfigurationResource("authentication_bypass.properties", continuePolicy, ignorePolicy);
        addConfigurationResource("authentication.properties", continuePolicy, ignorePolicy);

        addConfigurationResource("authorization_shiro.properties", continuePolicy, ignorePolicy);
        addConfigurationResource("authorization_bypass.properties", continuePolicy, ignorePolicy);
        addConfigurationResource("authorization.properties", continuePolicy, ignorePolicy);

        addConfigurationResource("reflector_java.properties", continuePolicy, ignorePolicy);
        addConfigurationResource("reflector.properties", continuePolicy, ignorePolicy);

        addConfigurationResource("fixtures-installer_configuration.properties", continuePolicy, ignorePolicy);
        addConfigurationResource("fixtures-installer.properties", continuePolicy, ignorePolicy);

        addConfigurationResource("services-installer_annotation.properties", continuePolicy, ignorePolicy);
        addConfigurationResource("services-installer_configuration.properties", continuePolicy, ignorePolicy);

        addConfigurationResource("services-installer_configuration-and-annotation.properties", continuePolicy, ignorePolicy);
        addConfigurationResource("services-installer.properties", continuePolicy, ignorePolicy);

        // both override and overrides are accepted (because I keep forgetting which)
        addConfigurationResource("override.properties", NotFoundPolicy.CONTINUE, IsisConfigurationDefault.ContainsPolicy.OVERWRITE);
        addConfigurationResource("overrides.properties", NotFoundPolicy.CONTINUE, IsisConfigurationDefault.ContainsPolicy.OVERWRITE);
    }

    private void addDefaultPrimers() {
        primeWith(new PrimerForSystemProperties());
        primeWith(new PrimerForEnvironmentVariablesIsisPrefix());
        primeWith(new PrimerForEnvironmentVariableISIS_OPTS());
        primeWith(new PrimerForServletContext());
    }

    // -- addResourceStreamSource, addResourceStreamSources

    @Override
    public void addResourceStreamSource(final ResourceStreamSource resourceStreamSource) {
        addResourceStreamSources(resourceStreamSource);
    }

    @Override
    public void addResourceStreamSources(final ResourceStreamSource... resourceStreamSources) {
        addResourceStreamSources(Arrays.asList(resourceStreamSources));
    }

    @Override
    public void addResourceStreamSources(final List<ResourceStreamSource> resourceStreamSources) {
        for (ResourceStreamSource resourceStreamSource : resourceStreamSources) {
            this.resourceStreamSourceChain.addResourceStreamSource(resourceStreamSource);
        }
    }

    // -- addConfigurationResource

    /**
     * Registers the configuration resource (usually, a file) with the specified
     * name from the first {@link ResourceStreamSource} available.
     *
     * <p>
     * If the configuration resource cannot be found then the provided
     * {@link NotFoundPolicy} determines whether an exception is thrown or not.
     *
     * <p>
     * Must be called before {@link IsisConfigurationBuilderDefault#getConfiguration()}.
     */
    @Override
    public void addConfigurationResource(
            final String configurationResource,
            final NotFoundPolicy notFoundPolicy,
            final IsisConfigurationDefault.ContainsPolicy containsPolicy) {

        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format(
                    "checking availability of configuration resource: %s, notFoundPolicy: %s",
                    configurationResource, notFoundPolicy));
        }
        loadConfigurationResource(configurationResource, notFoundPolicy, containsPolicy);
    }

    /**
     * Loads the configuration resource (usually, a file) with the specified
     * name from the first {@link ResourceStreamSource} available.
     *
     * <p>
     * If the configuration resource cannot be found then the provided
     * {@link NotFoundPolicy} determines whether an exception is thrown or not.
     */
    private void loadConfigurationResource(
            final String configurationResource,
            final NotFoundPolicy notFoundPolicy,
            final IsisConfigurationDefault.ContainsPolicy containsPolicy) {

        try {
            final PropertiesReader propertiesReader =
                    loadConfigurationResource(resourceStreamSourceChain, configurationResource);
            LOG.info("loading properties from {}", configurationResource);
            configuration.add(propertiesReader.getProperties(), containsPolicy);
            configurationResourcesFound.add(configurationResource);
            return;
        } catch (final IOException ignore) {
            // ignore
        }
        if (notFoundPolicy == NotFoundPolicy.FAIL_FAST) {
            throw new IsisException(String.format(
                    "failed to load '%s'; tried using: %s",
                    configurationResource, resourceStreamSourceChain.getName()));
        }
        configurationResourcesNotFound.add(configurationResource);
        if(LOG.isDebugEnabled()) {
            LOG.debug("'{}' not found, but not needed", configurationResource);

        }
    }

    private PropertiesReader loadConfigurationResource(final ResourceStreamSource resourceStreamSource, final String configurationResource) throws IOException {
        return new PropertiesReader(resourceStreamSource, configurationResource);
    }

    // -- add, put

    /**
     * Adds additional property; if already present then will _not_ be replaced.
     */
    @Override
    public void add(final String key, final String value) {
        configuration.add(key, value);
    }

    /**
     * Adds/updates property; if already present then _will_ be replaced.
     */
    @Override
    public void put(final String key, final String value) {
        configuration.put(key, value);
    }

    // -- PRIMING

    @Override
    public void primeWith(final Primer primer) {
        LOG.debug("priming configurations for '{}'", primer);
        primer.prime(this);
    }
    
    // -- LOAD MODULE TREE
    
    @Override
    public void addTopModule(Module topModule) {
        final AppManifestAbstract2.Builder manifestBuilder = AppManifestAbstract2.Builder
                .forModule(topModule);
        final AppManifestAbstract2 manifest = new AppManifestAbstract2(manifestBuilder) {};
        addAppManifest(manifest);
    }
    
    @Override
    public void addAppManifest(AppManifest2 appManifest) {
        configuration.setAppManifest(appManifest);
        appManifest.getConfigurationProperties().forEach((k, v)->{
            put(k, v);
        });
    }
        
    // -- LOAD SINGLE RESOURCE
        
    @Override
    public void addPropertyResource(PropertyResource propertyResource) {
        IsisConfigurationDefault.ContainsPolicy ignorePolicy = IsisConfigurationDefault.ContainsPolicy.IGNORE;
        NotFoundPolicy continuePolicy = NotFoundPolicy.CONTINUE;
        
        addResourceStreamSource(new ResourceStreamSource_UsingClass(propertyResource.getResourceContext()));
        addConfigurationResource(propertyResource.getResourceName(), continuePolicy, ignorePolicy);
    }

    // -- PEEKING
    
    @Override
    public String peekAtString(String key) {
        return configuration.getString(key);
    }

    @Override
    public String peekAtString(String key, String defaultValue) {
        return configuration.getString(key, defaultValue);
    }

    @Override
    public boolean peekAtBoolean(String key) {
        return configuration.getBoolean(key);
    }

    @Override
    public boolean peekAtBoolean(String key, boolean defaultValue) {
        return configuration.getBoolean(key, defaultValue);
    }
    
    @Override
    public String[] peekAtList(String key) {
        return configuration.getList(key);
    }
    
    // -- BUILD

    @Override
    public IsisConfiguration build() {

        configuration.triggerTypeDiscovery();
        
        if (LOG.isDebugEnabled()) {
            dumpResourcesToLog();    
        }

        final IsisConfigurationDefault ref = configuration;
        
        configuration = null; // once built this builder is no longer usable
        
        return ref;
    }

    // -- dumpResourcesToLog, toString

    /**
     * Log a summary of resources found or not found.
     */
    @Override
    public void dumpResourcesToLog() {
        LOG.info("Configuration resources FOUND:");
        for (String resource : configurationResourcesFound) {
            LOG.info("*  {}", resource);
        }
        LOG.info("Configuration resources NOT FOUND (but not needed):");
        for (String resource : configurationResourcesNotFound) {
            LOG.info("*  {}", resource);
        }
    }

    @Override
    public String toString() {
        return String.format("IsisConfigurationBuilder {resourceStream=%s, configResources=%s}", 
                resourceStreamSourceChain, configurationResourcesFound);
    }

   

}
