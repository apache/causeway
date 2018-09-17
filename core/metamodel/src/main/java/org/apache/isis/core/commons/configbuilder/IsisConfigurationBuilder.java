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
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.config.IsisConfigurationDefault.ContainsPolicy;
import org.apache.isis.core.commons.config.NotFoundPolicy;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.commons.resource.ResourceStreamSourceChainOfResponsibility;
import org.apache.isis.core.commons.resource.ResourceStreamSourceFileSystem;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandler;

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
public final class IsisConfigurationBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(IsisConfigurationBuilder.class);

    // -- constructor, fields

    private final ResourceStreamSourceChainOfResponsibility resourceStreamSourceChain;

    /* package */ final IsisConfigurationDefault configuration;
    private boolean locked;

    private final Set<String> configurationResourcesFound = _Sets.newLinkedHashSet();
    private final Set<String> configurationResourcesNotFound = _Sets.newLinkedHashSet();

    public IsisConfigurationBuilder() {
        this(ResourceStreamSourceFileSystem.create(ConfigurationConstants.DEFAULT_CONFIG_DIRECTORY));
    }

    public IsisConfigurationBuilder(final ResourceStreamSource... resourceStreamSources) {
        this(createComposite(Arrays.asList(resourceStreamSources)));
    }

    public IsisConfigurationBuilder(final List<ResourceStreamSource> resourceStreamSources) {
        this(createComposite(resourceStreamSources));
    }

    public IsisConfigurationBuilder(final ResourceStreamSourceChainOfResponsibility resourceStreamSourceChain) {
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

    public void addDefaultConfigurationResourcesAndPrimers() {
        addDefaultConfigurationResources();
        addDefaultPrimers();
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
    }



    // -- addResourceStreamSource, addResourceStreamSources

    public void addResourceStreamSource(final ResourceStreamSource resourceStreamSource) {
        addResourceStreamSources(resourceStreamSource);
    }

    public void addResourceStreamSources(final ResourceStreamSource... resourceStreamSources) {
        addResourceStreamSources(Arrays.asList(resourceStreamSources));
    }

    public void addResourceStreamSources(final List<ResourceStreamSource> resourceStreamSources) {
        ensureNotLocked();
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
     * Must be called before {@link IsisConfigurationBuilder#getConfiguration()}.
     */
    public void addConfigurationResource(
            final String configurationResource,
            final NotFoundPolicy notFoundPolicy,
            final IsisConfigurationDefault.ContainsPolicy containsPolicy) {

        ensureNotLocked();

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
    public void add(final String key, final String value) {
        ensureNotLocked();
        configuration.add(key, value);
    }

    /**
     * Adds/updates property; if already present then _will_ be replaced.
     */
    public void put(final String key, final String value) {
        ensureNotLocked();
        configuration.put(key, value);
    }




    // -- parseAndPrimeWith, primeWith

    public boolean parseAndPrimeWith(final List<OptionHandler> optionHandlers, final String[] args) {

        // add options (ie cmd line flags)
        final Options options = new Options();
        for (final OptionHandler optionHandler : optionHandlers) {
            optionHandler.addOption(options);
        }

        // parse options from the cmd line
        final boolean parsedOk = parseAndPrimeWith(options, optionHandlers, args);

        if(parsedOk) {
            for (final OptionHandler optionHandler : optionHandlers) {
                primeWith(optionHandler);
            }
        }

        return parsedOk;
    }

    private boolean parseAndPrimeWith(final Options options, final List<OptionHandler> optionHandlers, final String[] args) {
        final BootPrinter printer = new BootPrinter(getClass());
        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine commandLine = parser.parse(options, args);
            for (final OptionHandler optionHandler : optionHandlers) {
                if (!optionHandler.handle(commandLine, printer, options)) {
                    return false;
                }
            }
        } catch (final ParseException e) {
            printer.printErrorMessage(e.getMessage());
            printer.printHelp(options);
            return false;
        }
        return true;
    }

    public interface Primer {
        void prime(IsisConfigurationBuilder isisConfigurationBuilder);
    }

    public void primeWith(final Primer primer) {
        ensureNotLocked();

        LOG.debug("priming configurations for '{}'", primer);
        primer.prime(this);
    }



    // -- getConfiguration, peekConfiguration, isLocked

    /**
     * Returns the {@link IsisConfiguration}; this will cause the configuration to be locked
     */
    public IsisConfigurationDefault getConfiguration() {
        if(!locked) {
            locked = true;
            dumpResourcesToLog();
        }
        return configuration;
    }

    /**
     * Set once {@link #getConfiguration()} is called.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Creates a copy of the current {@link #getConfiguration()}, without locking.
     *
     * <p>
     *     Used while bootstrapping, to obtain the web.server port etc.
     * </p>
     */
    public IsisConfiguration peekConfiguration() {
        IsisConfigurationDefault cfg = new IsisConfigurationDefault(resourceStreamSourceChain);
        // no locking
        Properties props = new Properties();
        props.putAll(configuration.asMap());
        cfg.add(props, ContainsPolicy.OVERWRITE);
        return cfg;
    }

    private void ensureNotLocked() {
        if (locked) {
            throw new IsisException("Configuration has been locked and cannot be changed");
        }
    }

    public String peekAt(String key) {
        return configuration.getString(key);
    }

    // -- dumpResourcesToLog, toString

    /**
     * Log a summary of resources found or not found.
     */
    public void dumpResourcesToLog() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Configuration resources FOUND:");
            for (String resource : configurationResourcesFound) {
                LOG.debug("*  {}", resource);
            }
            LOG.debug("Configuration resources NOT FOUND (but not needed):");
            for (String resource : configurationResourcesNotFound) {
                LOG.debug("*  {}", resource);
            }
        }
    }

    private final static ToString<IsisConfigurationBuilder> toString =
            ToString.<IsisConfigurationBuilder>
    toString("resourceStream", x->x.resourceStreamSourceChain)
    .thenToString("configResources", x->x.configurationResourcesFound)
    ;

    @Override
    public String toString() {
        return toString.toString(this);
    }

}
