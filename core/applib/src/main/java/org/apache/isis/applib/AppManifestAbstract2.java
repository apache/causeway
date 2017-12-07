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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.fixturescripts.FixtureScript;

/**
 * Convenience adapter, configured using a {@link AppManifestAbstract.Builder}.
 */
public abstract class AppManifestAbstract2 extends AppManifestAbstract implements AppManifest2 {

    public static class Default extends AppManifestAbstract2 {
        public Default(final AppManifestAbstract.Builder builder) {
            super(builder);
        }
    }

    public static class Builder extends AppManifestAbstract.Builder {

        public static AppManifestAbstract.Builder forModule(Module module) {
            return module instanceof ModuleAbstract
                    ? new BuilderWrappingModuleAbstract((ModuleAbstract)module)
                    : new Builder(module);
        }

        private final Module module;

        private Builder(Module module) {
            this.module = module;

            final List<Module> transitiveDependencies = Module.Util.transitiveDependenciesOf(module);
            final Class[] moduleTransitiveDependencies = asClasses(transitiveDependencies);

            final List<Class<?>> additionalModules = Module.Util.transitiveDependenciesAsClassOf(module);
            final List<Class<?>> additionalServices = Module.Util.transitiveAdditionalServicesOf(module);

            withAdditionalModules(moduleTransitiveDependencies);
            withAdditionalModules(additionalModules);
            withAdditionalServices(additionalServices);
        }

        private static Class[] asClasses(final List<Module> dependencies) {
            final List<Class<? extends Module>> list = new ArrayList<>();
            for (Module dependency : dependencies) {
                Class<? extends Module> aClass = dependency.getClass();
                list.add(aClass);
            }
            return list.toArray(new Class[] {});
        }

        @Override
        public AppManifest build() {
            return new Default(this);
        }
    }

    /**
     * A {@link AppManifestAbstract.Builder} implementation that delegates to the wrapped {@link ModuleAbstract} for transitive modules,
     * services and configuration properties, but continues to manage fixture scripts and auth mechanisms.
     */
    public static class BuilderWrappingModuleAbstract extends AppManifestAbstract.Builder {

        private final ModuleAbstract moduleAbstract;

        private BuilderWrappingModuleAbstract(ModuleAbstract moduleAbstract) {
            this.moduleAbstract = moduleAbstract;
        }

        @Override
        public AppManifestAbstract.Builder withAdditionalModules(final Class<?>... modules) {
            moduleAbstract.withAdditionalModules(modules);
            return this;
        }

        @Override
        public AppManifestAbstract.Builder withAdditionalModules(final List<Class<?>> modules) {
            moduleAbstract.withAdditionalModules(modules);
            return this;
        }

        @Override
        public AppManifestAbstract.Builder withAdditionalServices(final Class<?>... additionalServices) {
            moduleAbstract.withAdditionalServices(additionalServices);
            return this;
        }

        @Override
        public AppManifestAbstract.Builder withAdditionalServices(final List<Class<?>> additionalServices) {
            moduleAbstract.withAdditionalServices(additionalServices);
            return this;
        }

        @Override
        public AppManifestAbstract.Builder withConfigurationProperties(final Map<String, String> configurationProperties) {
            moduleAbstract.withConfigurationProperties(configurationProperties);
            return this;
        }

        @Override
        public AppManifestAbstract.Builder withConfigurationPropertiesFile(final String propertiesFile) {
            moduleAbstract.withConfigurationPropertiesFile(propertiesFile);
            return this;
        }

        @Override
        public AppManifestAbstract.Builder withConfigurationPropertiesFile(
                final Class<?> propertiesFileContext,
                final String propertiesFile,
                final String... furtherPropertiesFiles) {
            moduleAbstract.withConfigurationPropertiesFile(propertiesFileContext, propertiesFile, furtherPropertiesFiles);
            return this;
        }

        @Override
        public AppManifestAbstract.Builder withConfigurationProperty(final String key, final String value) {
            moduleAbstract.withConfigurationProperty(key, value);
            return this;
        }

        @Override
        public List<Class<?>> getAllModulesAsClass() {
            return moduleAbstract.getAllModulesAsClass();
        }

        @Override
        public Set<Class<?>> getAllAdditionalServices() {
            return moduleAbstract.getAllAdditionalServices();
        }

        @Override
        public List<PropertyResource> getAllPropertyResources() {
            return moduleAbstract.getAllPropertyResources();
        }

        @Override
        public List<ConfigurationProperty> getAllIndividualConfigProps() {
            return moduleAbstract.getAllIndividualConfigProps();
        }

        @Override
        public AppManifest build() {
            return new Default(this);
        }

    }

    private final Module module;
    public AppManifestAbstract2(final AppManifestAbstract.Builder builder) {
        super(builder);
        if (!(builder instanceof Builder)) {
            throw new IllegalArgumentException("Requires an AppManifestAbstract2.Builder2");
        }
        Builder builder2 = (Builder) builder;
        this.module = builder2.module;
    }

    public <M extends Module & AppManifestBuilder<?>> AppManifestAbstract2(final M module) {
        super(module);
        this.module = module;
    }

    @Programmatic
    public Module getModule() {
        return module;
    }

    @Programmatic
    public FixtureScript getRefDataSetupFixture() {
        return new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                List<Module> modules = Module.Util.transitiveDependenciesOf(module);
                for (Module module : modules) {
                    FixtureScript fixtureScript = module.getRefDataSetupFixture();
                    if(fixtureScript != null) {
                        executionContext.executeChild(this, fixtureScript);
                    }
                }
            }
        };
    }

    @Programmatic
    public FixtureScript getTeardownFixture() {
        return new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                List<Module> modules = Module.Util.transitiveDependenciesOf(module);
                Collections.reverse(modules);
                for (Module module : modules) {
                    final FixtureScript fixtureScript = module.getTeardownFixture();
                    if(fixtureScript != null) {
                        executionContext.executeChild(this, fixtureScript);
                    }
                }
            }
        };
    }



}
