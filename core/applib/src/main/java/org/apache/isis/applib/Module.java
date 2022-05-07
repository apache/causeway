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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.fixturescripts.FixtureScript;

/**
 * Represents a collection of entities and domain services that provide a set of coherent functionality under a
 * single package (or subpackages therein), with the module itself residing at the top-level package.
 *
 * <p>
 *     Moreover, each module can indicate its immediate dependencies (using {@link #getDependencies()}); from this the
 *     framework can compute the full set of modules (and therefore entities and domain services) that make up the
 *     application.
 * </p>
 *
 * <p>
 *     Each module can also optionally define {@link #getAdditionalModules() additional (legacy) modules},
 *     {@link #getAdditionalServices() additional (non-module) services} and also any additional
 *     {@link #getIndividualConfigProps() configuration} {@link #getPropertyResources() properties} that should be
 *     contributed to the overall set of config properties used to bootstrap the application.
 * </p>
 *
 * <p>
 *     Finally, each module can also optionally define a {@link #getRefDataSetupFixture() reference-data fixture} (for
 *     reference data entities of the module) and also a {@link #getTeardownFixture() tear-down fixture} (for all
 *     entities of the module).  These are executed automatically (and in the correct order) within integration tests,
 *     simplifying setup and teardown of such tests.
 * </p>
 *
 * <p>
 *     <b>IMPORTANT</b>: implementations are expected to have value-semantics. This allows each {@link Module} to define
 *     its respective {@link #getDependencies() dependencies} simply by instantiating the {@link Module}s on which
 *     it depends.  The framework relies on value semantics to remove any duplicates from the computed graph of
 *     all depenencies for the entire running application.  The easiest way to simply inherit from
 *     {@link ModuleAbstract}.
 * </p>
 */
public interface Module {

    /**
     * As per Maven's &lt;dependencies&gt;&lt;/dependencies&gt; element; in the future might be derived
     * (code generated?) from java 9's <code>module-info.java</code> metadata
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
    Set<Class<?>> getAdditionalModules();

    /**
     * Optionally each module can define a {@link FixtureScript} which holds immutable "reference data".
     * These are automatically executed whenever running integration tests (but are ignored when bootstrapping the
     * runtime as a webapp).
     */
    FixtureScript getRefDataSetupFixture();

    /**
     * Optionally each module can define a tear-down {@link FixtureScript}, used to remove the contents of <i>all</i>
     * entities (both reference data and operational/transactional data).
     * These are automatically executed whenever running integration tests (but are ignored when bootstrapping the
     * runtime as a webapp).
     */
    FixtureScript getTeardownFixture();

    /**
     * Optionally each module can define additional "legacy" domain services that have not been defined within modules,
     * or that have not been annotated with &#64;{@link DomainService}
     */
    Set<Class<?>> getAdditionalServices();

    /**
     * Optionally each module can define additional configuration properties.
     */
    Map<String,String> getIndividualConfigProps();

    /**
     * Optionally each module can define additional configuration properties, specified in terms of
     * {@link PropertyResource} (a configuration file laoded relative to a base class on the class path).
     */
    List<PropertyResource> getPropertyResources();

    class Util {
        private Util(){}

        /**
         * Recursively obtain the transitive dependencies.
         *
         * <p>
         *     The dependencies are returned in order, with this (the top-most) module last.
         * </p>
         */
        static List<Module> transitiveDependenciesOf(Module module) {
            final List<Module> ordered = Lists.newArrayList();
            final List<Module> visited = Lists.newArrayList();
            appendDependenciesTo(ordered, module, visited);
            final LinkedHashSet<Module> sequencedSet = Sets.newLinkedHashSet(ordered);
            return Lists.newArrayList(sequencedSet);
        }

        /**
         * Obtain the {@link Module#getAdditionalModules()} of this module and all its
         * {@link Module.Util#transitiveDependenciesOf(Module) transitive dependencies}.
         *
         * <p>
         *     No guarantees are made as to the order of these additional module classes.
         * </p>
         */
        static List<Class<?>> transitiveAdditionalModulesOf(Module module) {
            final Set<Class<?>> modules = Sets.newHashSet();
            final List<Module> transitiveDependencies = transitiveDependenciesOf(module);
            for (Module transitiveDependency : transitiveDependencies) {
                final Set<Class<?>> additionalModules = transitiveDependency.getAdditionalModules();
                if(additionalModules != null && !additionalModules.isEmpty()) {
                    for (Class<?> clazz : additionalModules) {
                        if(Module.class.isAssignableFrom(clazz)) {
                            throw new IllegalArgumentException("Module " + transitiveDependency + " has returned " + clazz + " from getAdditionalModules().  This class implements 'Module' interface so should instead be returned from getDependencies()");
                        }
                    }
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
        static List<Class<?>> transitiveAdditionalServicesOf(Module module) {
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

        static Map<String, String> transitiveIndividualConfigPropsOf(final Module module) {
            final Map<String,String> transitiveIndividualConfigProps = Maps.newLinkedHashMap();

            final List<Module> transitiveDependencies = transitiveDependenciesOf(module);
            for (Module transitiveDependency : transitiveDependencies) {
                transitiveIndividualConfigProps.putAll(transitiveDependency.getIndividualConfigProps());
            }
            return transitiveIndividualConfigProps;
        }

        static List<PropertyResource> transitivePropertyResourcesOf(final Module module) {
            final List<PropertyResource> transitivePropertyResources = Lists.newArrayList();

            final List<Module> transitiveDependencies = transitiveDependenciesOf(module);
            for (Module transitiveDependency : transitiveDependencies) {
                transitivePropertyResources.addAll(transitiveDependency.getPropertyResources());
            }

            return transitivePropertyResources;
        }
    }

}
