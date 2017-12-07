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

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.fixturescripts.FixtureScript;

/**
 * Convenience adapter, configured using an {@link Builder}.
 */
public abstract class AppManifestAbstract2 extends AppManifestAbstract implements AppManifest2 {

    public static class Builder2 extends Builder {

        private final Module module;

        private Builder2(Module module) {
            this.module = module;
        }

        public static Builder2 forModule(Module module) {
            return new Builder2(module);
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
            if(module instanceof ModuleAbstract) {
                return new AppManifestAbstract2((ModuleAbstract) module){};
            } else {
                final List<Module> transitiveDependencies = Module.Util.transitiveDependenciesOf(module);
                final Class[] moduleTransitiveDependencies = asClasses(transitiveDependencies);

                final List<Class<?>> additionalModules = Module.Util.transitiveDependenciesAsClassOf(module);
                final List<Class<?>> additionalServices = Module.Util.transitiveAdditionalServicesOf(module);

                withAdditionalModules(moduleTransitiveDependencies);
                withAdditionalModules(additionalModules);
                withAdditionalServices(additionalServices);

                return new AppManifestAbstract2(this) {};
            }
        }
    }

    private final Module module;
    public AppManifestAbstract2(final Builder builder) {
        super(builder);
        if (!(builder instanceof Builder2)) {
            throw new IllegalArgumentException("Requires an AppManifestAbstract2.Builder2");
        }
        Builder2 builder2 = (Builder2) builder;
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
