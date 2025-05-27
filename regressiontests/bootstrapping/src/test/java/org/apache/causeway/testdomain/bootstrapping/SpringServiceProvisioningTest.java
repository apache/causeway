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
package org.apache.causeway.testdomain.bootstrapping;

import java.io.IOException;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.ioc._SingletonBeanProvider;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_headless;

import static org.apache.causeway.commons.internal.collections._Collections.toStringJoiningNewLine;
import static org.apache.causeway.commons.internal.collections._Sets.intersectSorted;

//import org.apache.causeway.testdomain.Incubating;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
        },
        properties = {
                // "causeway.core.meta-model.introspector.parallelize=false",
                // "logging.level.ObjectSpecificationAbstract=TRACE"
        })
@TestPropertySource({
    //CausewayPresets.DebugDiscovery
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel,
})
//@Incubating("with development work on 'v2' the reference list of services constantly changes")
class SpringServiceProvisioningTest {

    @Inject private ServiceRegistry serviceRegistry;
    @Inject private CausewaySystemEnvironment causewaySystemEnvironment;

    @BeforeEach
    void beforeEach() {

    }

    @Test
    void dump_all() throws IOException {

        final List<String> beans = causewaySystemEnvironment.getIocContainer().streamAllBeans()
                .map(_SingletonBeanProvider::id)
                .sorted()
                .collect(Collectors.toList());

        var beansFound = toStringJoiningNewLine(beans);
        System.out.println("--- Beans discovered by Causeway ---");
        System.out.println(beansFound);
        System.out.println("--------------------------------");
    }

    @Test @Disabled("constantly changing")
    void builtInServices_shouldBeSetUp() throws IOException {

        var managedServices = serviceRegistry.streamRegisteredBeans()
                .map(_SingletonBeanProvider::beanClass)
                .map(Class::getName)
                .collect(Collectors.toCollection(TreeSet::new));

        var singletonListing = _Resources.loadAsStringUtf8(this.getClass(), "builtin-domain-services.list");
        var expectedSingletons = _Strings.splitThenStreamTrimmed(singletonListing, "\n")
                .filter(entry->!entry.startsWith("#"))
                .filter(entry->!entry.startsWith("org.apache.causeway.testdomain."))
                .collect(Collectors.toCollection(TreeSet::new));

        assertFalse(expectedSingletons.isEmpty());

        var servicesFound = toStringJoiningNewLine(managedServices);
        System.out.println("--- Services discovered by Causeway ---");
        System.out.println(servicesFound);
        System.out.println("--------------------------------");

        // same as managedServices.containsAll(singletonSet) but more verbose in case of
        // failure
        assertEquals(toStringJoiningNewLine(expectedSingletons),
                toStringJoiningNewLine(intersectSorted(managedServices, expectedSingletons)));

        // TODO also test for request-scoped service (requires a means to mock a
        // request-context)

    }

}
