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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;

public class Module_Util_transitiveDependenciesOf_Test {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public class ModuleImpl extends ModuleAbstract {
        private final String name;
        public ModuleImpl(final String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
    }

    static class ModuleP {}
    static class ModuleQ {}
    static class ModuleR {}

    static class ServiceX {}
    static class ServiceY {}
    static class ServiceZ {}

    final Module moduleF = new ModuleImpl("F");
    final Module moduleE = new ModuleImpl("E") {
        {
            withAdditionalServices(ServiceX.class);
        }
        @Override
        public Set<Class<?>> getAdditionalModules() {
            return Sets.<Class<?>>newHashSet(ModuleP.class);
        }
    };
    final Module moduleD = new ModuleImpl("D") {
        @Override public Set<Module> getDependencies() {
            return Sets.newHashSet(moduleE);
        }
    };

    final Module moduleC = new ModuleImpl("C") {
        @Override public Set<Module> getDependencies() {
            return Sets.newHashSet(moduleE, moduleD);
        }
        @Override
        public Set<Class<?>> getAdditionalModules() {
            return Sets.newHashSet(ModuleQ.class, ModuleR.class);
        }
        {
            withAdditionalServices(ServiceY.class, ServiceZ.class);
        }
    };
    final Module moduleB = new ModuleImpl("B") {
        @Override public Set<Module> getDependencies() {
            return Sets.newHashSet(moduleF, moduleC);
        }
    };
    final Module moduleA = new ModuleImpl("A") {
        @Override public Set<Module> getDependencies() {
            return Sets.newHashSet(moduleE, moduleC);
        }
    };

    final Module moduleG = new ModuleImpl("G") {
        @Override public Set<Module> getDependencies() {
            return Sets.newHashSet(moduleH);
        }
    };
    final Module moduleH = new ModuleImpl("H") {
        @Override public Set<Module> getDependencies() {
            return Sets.newHashSet(moduleI);
        }
    };

    final Module moduleI = new ModuleImpl("I") {
        @Override public Set<Module> getDependencies() {
            return Sets.newHashSet(moduleG);
        }
    };


    @Test
    public void no_cyclic_dependencies() throws Exception {

        // moduleF
        // moduleE [P; X]
        // moduleD            -> moduleE
        // moduleC [Q,R; Y,Z] -> moduleE, moduleD
        // moduleB            -> moduleF, moduleC
        // moduleA            -> moduleE, moduleC

        assertTransitiveDependencies(
                moduleF, Arrays.asList(moduleF));
        assertTransitiveDependenciesAsClass(
                moduleF, Collections.<Class<?>>emptyList());
        assertTransitiveServices(
                moduleF, Collections.<Class<?>>emptyList());

        assertTransitiveDependencies(
                moduleE, Arrays.asList(moduleE));
        assertTransitiveDependenciesAsClass(
                moduleE, Lists.<Class<?>>newArrayList(ModuleP.class));
        assertTransitiveServices(
                moduleE, Lists.<Class<?>>newArrayList(ServiceX.class));

        assertTransitiveDependencies(
                moduleD, Arrays.asList(moduleE, moduleD));
        assertTransitiveDependenciesAsClass(
                moduleD, Lists.<Class<?>>newArrayList(ModuleP.class));
        assertTransitiveServices(
                moduleD, Lists.<Class<?>>newArrayList(ServiceX.class));

        assertTransitiveDependencies(
                moduleC, Arrays.asList(moduleE, moduleD, moduleC));
        assertTransitiveDependenciesAsClass(
                moduleC, Arrays.asList(ModuleP.class, ModuleQ.class, ModuleR.class));
        assertTransitiveServices(
                moduleC, Arrays.asList(ServiceX.class, ServiceY.class, ServiceZ.class));

        assertTransitiveDependencies(
                moduleB, Arrays.asList(moduleE, moduleD, moduleC, moduleF, moduleB));
        assertTransitiveDependenciesAsClass(
                moduleB, Arrays.asList(ModuleP.class, ModuleQ.class, ModuleR.class));
        assertTransitiveServices(
                moduleB, Arrays.asList(ServiceX.class, ServiceY.class, ServiceZ.class));

        assertTransitiveDependencies(
                moduleA, Arrays.asList(moduleE, moduleD, moduleC, moduleA));
        assertTransitiveDependenciesAsClass(
                moduleA, Arrays.asList(ModuleP.class, ModuleQ.class, ModuleR.class));
        assertTransitiveServices(
                moduleA, Arrays.asList(ServiceX.class, ServiceY.class, ServiceZ.class));

    }

    @Test
    public void with_cyclic_dependencies() throws Exception {

        expectedException.expect(IllegalStateException.class);

        Module.Util.transitiveDependenciesOf(moduleG);
    }

    void assertTransitiveDependencies(
            final Module module, final List<Module> expected) {
        final List<Module> dependencies = Module.Util.transitiveDependenciesOf(module);
        assertTrue(dependencies.containsAll(expected));
        assertTrue(expected.containsAll(dependencies));
    }

    void assertTransitiveServices(
            final Module module, final List<Class<?>> expected) {
        final List<Class<?>> services = Module.Util.transitiveAdditionalServicesOf(module);
        assertTrue(services.containsAll(expected));
        assertTrue(expected.containsAll(services));
    }

    void assertTransitiveDependenciesAsClass(
            final Module module, final List<Class<?>> expected) {
        final List<Class<?>> dependenciesAsClass = Module.Util.transitiveAdditionalModulesOf(module);
        assertTrue(dependenciesAsClass.containsAll(expected));
        assertTrue(expected.containsAll(dependenciesAsClass));
    }

}