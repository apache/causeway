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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.isis.applib.fixturescripts.FixtureScript;

public interface Module {

    /**
     * As per Maven's &lt;dependencies&gt;&lt;/dependencies&gt; element; in the future might be derived (code generated?) from java 9's <code>module-info.java</code> metadata
     *
     * <p>
     *     We use Set (rather than List) because we rely on {@link Module} being a value type based solely on its
     *     class.  What this means is that each module can simply instantiate its dependencies, and the framework will
     *     be able to eliminate duplicates.
     * </p>
     */
    Set<Module> getDependencies();

    /**
     * Support for "legacy" modules that do not implement {@link Module}.
     */
    Set<Class<?>> getDependenciesAsClass();

    FixtureScript getRefDataSetupFixture();

    FixtureScript getTeardownFixture();

    Set<Class<?>> getAdditionalServices();

    class Util {
        private Util(){}

        /**
         * Recursively obtain the transitive dependencies.
         *
         * <p>
         *     The dependencies are returned in order, with this (the top-most) module last.
         * </p>
         */
        public static List<Module> transitiveDependenciesOf(Module module) {
            final List<Module> ordered = Lists.newArrayList();
            final List<Module> visited = Lists.newArrayList();
            appendDependenciesTo(ordered, module, visited);
            final LinkedHashSet<Module> sequencedSet = Sets.newLinkedHashSet(ordered);
            return Lists.newArrayList(sequencedSet);
        }

        /**
         * Obtain the {@link Module#getDependenciesAsClass()} of this module and all its
         * {@link Module.Util#transitiveDependenciesOf(Module) transitive dependencies}.
         *
         * <p>
         *     No guarantees are made as to the order of these additional module classes.
         * </p>
         */
        public static List<Class<?>> transitiveDependenciesAsClassOf(Module module) {
            final Set<Class<?>> modules = Sets.newHashSet();
            final List<Module> transitiveDependencies = transitiveDependenciesOf(module);
            for (Module transitiveDependency : transitiveDependencies) {
                final Set<Class<?>> additionalModules = transitiveDependency.getDependenciesAsClass();
                if(additionalModules != null && !additionalModules.isEmpty()) {
                    modules.addAll(additionalModules);
                }
            }
            return Lists.newArrayList(modules);
        }

        /**
         * Obtain the {@link #getAdditionalServices()} of this module and all its
         * {@link Module.Util#transitiveDependenciesOf(Module) transitive dependencies}.
         *
         * <p>
         *     No guarantees are made as to the order of these additional service classes.
         * </p>
         */
        public static List<Class<?>> transitiveAdditionalServicesOf(Module module) {
            final Set<Class<?>> services = Sets.newHashSet();
            final List<Module> transitiveDependencies = Util.transitiveDependenciesOf(module);
            for (Module transitiveDependency : transitiveDependencies) {
                final Set<Class<?>> additionalServices = transitiveDependency.getAdditionalServices();
                if(additionalServices != null && !additionalServices.isEmpty()) {
                    services.addAll(additionalServices);
                }
            }
            return Lists.newArrayList(services);
        }

        private static void appendDependenciesTo(
                final List<Module> ordered,
                final Module module,
                final List<Module> visited) {

            if(visited.contains(module)) {
                throw new IllegalStateException(String.format(
                        "Cyclic dependency detected; visited: %s", visited));
            } else {
                visited.add(module);
            }

            final Set<Module> dependencies = module.getDependencies();
            if(dependencies.isEmpty() || ordered.containsAll(dependencies)) {
                ordered.add(module);
                visited.clear(); // reset
            } else {
                for (Module dependency : dependencies) {
                    appendDependenciesTo(ordered, dependency, visited);
                }
            }
            if(!ordered.contains(module)) {
                ordered.add(module);
            }
        }

        public static AppManifestAbstract.Builder builderFor(final Module module) {
            final List<Module> transitiveDependencies = Module.Util.transitiveDependenciesOf(module);
            final Class[] moduleTransitiveDependencies = asClasses(transitiveDependencies);

            final List<Class<?>> additionalModules = Module.Util.transitiveDependenciesAsClassOf(module);
            final List<Class<?>> additionalServices = Module.Util.transitiveAdditionalServicesOf(module);

            final AppManifestAbstract.Builder builder =
                    AppManifestAbstract.Builder
                            .forModules(moduleTransitiveDependencies)
                            .withAdditionalModules(additionalModules)
                            .withAdditionalServices(additionalServices);

            return builder;
        }

        private static Class[] asClasses(final List<Module> dependencies) {
            final List<Class<? extends Module>> list = new ArrayList<>();
            for (Module dependency : dependencies) {
                Class<? extends Module> aClass = dependency.getClass();
                list.add(aClass);
            }
            return list.toArray(new Class[] {});
        }


    }

}
