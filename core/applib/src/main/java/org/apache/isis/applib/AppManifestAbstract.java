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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final List<Class<? extends FixtureScript>> fixtureClasses;
    private final Map<String, String> configurationProperties;

    public AppManifestAbstract(final BuilderAbstract<?> builder) {

        final List<Class<?>> builderModules = builder.getAllAdditionalModules();
        overrideModules(builderModules);
        this.modules = builderModules;

        final List<Class<?>> builderAdditionalServices = Lists.newArrayList(builder.getAllAdditionalServices());
        overrideAdditionalServices(builderAdditionalServices);

        this.additionalServices = builderAdditionalServices;

        this.authMechanism = determineAuthMechanism(builder);
        this.fixtureClasses = determineFixtures(builder);

        // note uses this.fixtures, so must come afterwards...
        this.configurationProperties = createConfigurationProperties(
                builder.getAllPropertyResources(), builder.getAllIndividualConfigProps(),
                this.fixtureClasses);
    }

    private String determineAuthMechanism(final ModuleOrBuilderAbstract<?> builder) {
        final String overriddenAuthMechanism = overrideAuthMechanism();
        if (overriddenAuthMechanism != null) {
            return overriddenAuthMechanism;
        } else {
            return obtainAuthMechanismFrom(builder);
        }
    }

    private List<Class<? extends FixtureScript>> determineFixtures(final ModuleOrBuilderAbstract<?> builder) {
        final List<Class<? extends FixtureScript>> builderFixtures = obtainBuilderFixturesFrom(builder);
        overrideFixtures(builderFixtures);
        return builderFixtures;
    }

    private static String obtainAuthMechanismFrom(final ModuleOrBuilderAbstract<?> builder) {
        if(builder instanceof Builder) {
            return ((Builder) builder).authMechanism;
        }
        if(builder instanceof AppManifestAbstract2.Builder) {
            return ((AppManifestAbstract2.Builder) builder).authMechanism;
        }
        return null;
    }

    private static List<Class<? extends FixtureScript>> obtainBuilderFixturesFrom(final ModuleOrBuilderAbstract<?> builder) {
        if(builder instanceof Builder) {
            return ((Builder) builder).fixtures;
        }
        if(builder instanceof AppManifestAbstract2.Builder) {
            return ((AppManifestAbstract2.Builder) builder).fixtures;
        }
        return Lists.newArrayList();
    }

    private Map<String, String> createConfigurationProperties(
            final List<PropertyResource> propertyResources,
            final Map<String,String> individualConfigProps,
            final List<Class<? extends FixtureScript>> fixtures) {
        final Map<String, String> props = Maps.newHashMap();
        for (PropertyResource propertyResource : propertyResources) {
            propertyResource.loadPropsInto(props);
        }
        for (final Map.Entry<String,String> individualConfigProp : individualConfigProps.entrySet()) {
            props.put(individualConfigProp.getKey(), individualConfigProp.getValue());
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
        return fixtureClasses;
    }

    /**
     * Optional hook to allow subclasses to tweak fixtures previously specified
     *
     * <p>
     *     Alternatively, can compose a different builder and pass into the constructor,
     *     using {@link Builder#withFixtureScripts(Class[])} .
     * </p>
     */
    protected void overrideFixtures(final List<Class<? extends FixtureScript>> fixtureScriptClasses) {
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
     * Default implementation of {@link AppManifestAbstract} that is built using a {@link Builder}.
     */
    public static class Default extends AppManifestAbstract {
        public Default(final AppManifestAbstract.Builder builder) {
            super(builder);
        }
    }

    public static abstract class BuilderAbstract<B extends BuilderAbstract<B>> extends ModuleOrBuilderAbstract<B> {

        String authMechanism = "shiro";
        List<Class<? extends FixtureScript>> fixtures = Lists.newArrayList();

        public B withAuthMechanism(final String authMechanism) {
            this.authMechanism = authMechanism;
            return (B)this;
        }

        public B withFixtureScripts(final Class<? extends FixtureScript>... fixtures) {
            return withFixtureScripts(Arrays.asList(fixtures));
        }

        public B withFixtureScripts(final List<Class<? extends FixtureScript>> fixtures) {
            if(fixtures == null) {
                return (B)this;
            }
            this.fixtures.addAll(fixtures);
            return (B)this;
        }

        List<Class<?>> getAllAdditionalModules() {
            return Lists.newArrayList(additionalModules);
        }

        Set<Class<?>> getAllAdditionalServices() {
            return additionalServices;
        }

        List<PropertyResource> getAllPropertyResources() {
            return getPropertyResources();
        }

        Map<String,String> getAllIndividualConfigProps() {
            return getIndividualConfigProps();
        }

        public abstract AppManifest build();

    }

    public static class Builder extends BuilderAbstract<Builder> {

        /**
         * Factory method.
         */
        public static AppManifestAbstract.Builder forModules(final List<Class<?>> modules) {
            return new AppManifestAbstract.Builder().withAdditionalModules(modules);
        }
        public static AppManifestAbstract.Builder forModules(final Class<?>... modules) {
            return forModules(Arrays.asList(modules));
        }

        @Override
        public AppManifest build() {
            return new AppManifestAbstract.Default(this);
        }

    }

}
