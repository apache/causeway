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
package org.apache.isis.applib;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.fixturescripts.FixtureScript;

/**
 * Convenience adapter, configured using an {@link Builder}.
 */
public abstract class AppManifestAbstract implements AppManifest {

    private final List<Class<?>> modules;
    private final List<Class<?>> additionalServices;
    private final String authMechanism;
    private final List<Class<? extends FixtureScript>> fixtures;
    private final Map<String, String> configurationProperties;

    public AppManifestAbstract(final Builder builder) {

        final List<Class<?>> builderModules = builder.modules;
        overrideModules(builderModules);
        this.modules = builderModules;

        final List<Class<?>> builderAdditionalServices = builder.additionalServices;
        overrideAdditionalServices(builderAdditionalServices);

        this.additionalServices = builderAdditionalServices;

        final String overriddenAuthMechanism = overrideAuthMechanism();
        this.authMechanism = overriddenAuthMechanism != null ? overriddenAuthMechanism : builder.authMechanism;

        final List<Class<? extends FixtureScript>> builderFixtures = builder.fixtures;
        overrideFixtures(builderFixtures);
        this.fixtures = builderFixtures;

        // note uses this.fixtures, so must come afterwards...
        this.configurationProperties = createConfigurationProperties(builder.propertyResources, builder.individualConfigProps, this.fixtures);
    }

    private Map<String, String> createConfigurationProperties(
            final List<PropertyResource> propertyResources,
            final List<ConfigurationProperty> individualConfigProps,
            final List<Class<? extends FixtureScript>> fixtures) {
        final Map<String, String> props = Maps.newHashMap();
        for (PropertyResource propertyResource : propertyResources) {
            propertyResource.loadPropsInto(props);
        }
        for (ConfigurationProperty individualConfigProp : individualConfigProps) {
            individualConfigProp.put(props);
        }
        if(!fixtures.isEmpty()) {
            props.put("isis.persistor.datanucleus.install-fixtures","true");
        }
        overrideConfigurationProperties(props);
        return props;
    }

    @Override
    public final List<Class<?>> getModules() {
        return modules;
    }

    /**
     * Optional hook to allow subclasses to tweak modules previously defined in constructor.
     *
     * <p>
     *     Alternatively, can compose a different builder and pass into the constructor,
     *     using {@link Builder#withAdditionalModules(Class[])}.
     * </p>
     */
    protected void overrideModules(List<Class<?>> modules) {
        // default implementation does nothing.
    }

    @Override
    public final List<Class<?>> getAdditionalServices() {
        return additionalServices;
    }

    /**
     * Optional hook to allow subclasses to tweak services previously defined
     *
     * <p>
     *     Alternatively, can compose a different builder and pass into the constructor,
     *     using {@link Builder#withAdditionalServices(Class[])}
     * </p>
     */
    protected void overrideAdditionalServices(final List<Class<?>> additionalServices) {
        // default implementation does nothing.
    }

    @Override
    public final String getAuthenticationMechanism() {
        return authMechanism;
    }

    /**
     * Optional hook to override both the {@link #getAuthenticationMechanism()} and {@link #getAuthorizationMechanism()}.
     *
     * <p>
     *     Alternatively, can compose a different builder and pass into the constructor,
     *     using {@link Builder#withAuthMechanism(String)} .
     * </p>
     */
    protected String overrideAuthMechanism() {
        return null;
    }

    @Override
    public final String getAuthorizationMechanism() {
        return authMechanism;
    }

    @Override
    public final List<Class<? extends FixtureScript>> getFixtures() {
        return fixtures;
    }

    /**
     * Optional hook to allow subclasses to tweak fixtures previously specified
     *
     * <p>
     *     Alternatively, can compose a different builder and pass into the constructor,
     *     using {@link Builder#withFixtureScripts(Class[])} .
     * </p>
     */
    protected void overrideFixtures(final List<Class<? extends FixtureScript>> fixtureScripts) {
        // default implementation does nothing.
    }

    @Override
    public final Map<String, String> getConfigurationProperties() {
        return this.configurationProperties;
    }


    /**
     * Optional hook to allow subclasses to tweak configuration properties previously specified
     *
     * <p>
     *     Alternatively, can compose a different builder and pass into the constructor,
     *     using {@link Builder#withConfigurationProperties(Map)} .
     * </p>
     */
    protected void overrideConfigurationProperties(final Map<String, String> configurationProperties) {
        // default implementation does nothing.
    }

    /**
     * Used to build an {@link AppManifest} either {@link #build() directly}, or implicitly by passing into
     * {@link AppManifestAbstract}'s {@link AppManifestAbstract#AppManifestAbstract(Builder) constructor}.
     */
    public static class Builder {


        final List<Class<?>> modules = Lists.newArrayList();
        List<Class<?>> additionalServices  = Lists.newArrayList();
        String authMechanism = "shiro";
        List<Class<? extends FixtureScript>> fixtures = Lists.newArrayList();

        List<ConfigurationProperty> individualConfigProps = Lists.newArrayList();
        List<PropertyResource> propertyResources = Lists.newArrayList();

        private Map<String,String> configurationProperties = Maps.newHashMap();

        private Builder() {}

        /**
         * Factory method.
         */
        public static Builder forModules(final List<Class<?>> modules) {
            return new Builder().withAdditionalModules(modules);
        }
        public static Builder forModules(final Class<?>... modules) {
            return forModules(Arrays.asList(modules));
        }

        public Builder withAdditionalModules(final Class<?>... modules) {
            return withAdditionalModules(Arrays.asList(modules));
        }

        public Builder withAdditionalModules(final List<Class<?>> modules) {
            if(modules == null) {
                throw new IllegalArgumentException("List of modules must not be null");
            }
            this.modules.addAll(modules);
            return this;
        }

        public Builder withAuthMechanism(final String authMechanism) {
            this.authMechanism = authMechanism;
            return this;
        }

        public Builder withAdditionalServices(final Class<?>... additionalServices) {
            return withAdditionalServices(Arrays.asList(additionalServices));
        }

        public Builder withAdditionalServices(final List<Class<?>> additionalServices) {
            if(additionalServices == null) {
                throw new IllegalArgumentException("List of additional services must not be null");
            }
            this.additionalServices = Lists.newArrayList(additionalServices);
            return this;
        }

        public Builder withFixtureScripts(final Class<? extends FixtureScript>... fixtures) {
            return withFixtureScripts(Arrays.asList(fixtures));
        }

        public Builder withFixtureScripts(final List<Class<? extends FixtureScript>> fixtures) {
            if(fixtures == null) {
                throw new IllegalArgumentException("List of fixtures must not be null");
            }
            this.fixtures = Lists.newArrayList(fixtures);
            return this;
        }

        public Builder withConfigurationProperties(final Map<String,String> configurationProperties) {
            this.configurationProperties.putAll(configurationProperties);
            return this;
        }

        public Builder withConfigurationPropertiesFile(final String propertiesFile) {
            return withConfigurationPropertiesFile(getClass(), propertiesFile);
        }

        public Builder withConfigurationPropertiesFile(
                final Class<?> propertiesFileContext, final String propertiesFile, final String... furtherPropertiesFiles) {
            addPropertyResource(propertiesFileContext, propertiesFile);
            for (final String otherFile : furtherPropertiesFiles) {
                addPropertyResource(propertiesFileContext, otherFile);
            }
            return this;
        }

        private void addPropertyResource(final Class<?> propertiesFileContext, final String propertiesFile) {
            propertyResources.add(new PropertyResource(propertiesFileContext, propertiesFile));
        }

        public Builder withConfigurationProperty(final String key, final String value) {
            individualConfigProps.add(new ConfigurationProperty(key,value));
            return this;
        }

        public AppManifest build() {
            return new AppManifestAbstract(this) {};
        }

    }

    static class ConfigurationProperty {
        private final String key;
        private final String value;

        ConfigurationProperty(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        void put(final Map<String, String> props) {
            props.put(key, value);
        }
    }

    static class PropertyResource {
        private final Class<?> propertiesFileContext;
        private final String propertiesFile;

        PropertyResource(final Class<?> propertiesFileContext, final String propertiesFile) {
            this.propertiesFileContext = propertiesFileContext;
            this.propertiesFile = propertiesFile;
        }

        void loadPropsInto(
                final Map<String, String> props) {
            final Properties properties = new Properties();
            try {
                try (final InputStream stream = propertiesFileContext.getResourceAsStream(propertiesFile)) {
                    properties.load(stream);
                    for (Object key : properties.keySet()) {
                        final Object value = properties.get(key);
                        if (key instanceof String && value instanceof String) {
                            props.put((String) key, (String) value);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(
                        String.format("Failed to load '%s' file ", this), e);
            }
        }
    }
}
