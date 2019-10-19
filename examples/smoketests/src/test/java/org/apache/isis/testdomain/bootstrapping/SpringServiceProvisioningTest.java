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
package org.apache.isis.testdomain.bootstrapping;

import java.io.IOException;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.ioc.ManagedBeanAdapter;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.config.IsisPresets;
//import org.apache.isis.testdomain.Incubating;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;

import static org.apache.isis.commons.internal.collections._Collections.toStringJoiningNewLine;
import static org.apache.isis.commons.internal.collections._Sets.intersectSorted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class, 
        },
        properties = {
                "logging.config=log4j2-test.xml",
                // "isis.reflector.introspector.parallelize=false",
                // "logging.level.org.apache.isis.metamodel.specloader.specimpl.ObjectSpecificationAbstract=TRACE"
        })
@TestPropertySource({
    IsisPresets.DebugDiscovery
})
//@Incubating("with development work on 'v2' the reference list of services constantly changes")
class SpringServiceProvisioningTest {
    
    @Inject private ServiceRegistry serviceRegistry; 

    @BeforeEach
    void beforeEach() {

    }

    @Test
    void builtInServices_shouldBeSetUp() throws IOException {

        val managedServices = serviceRegistry.streamRegisteredBeans()
                .map(ManagedBeanAdapter::getBeanClass)
                .map(Class::getName)
                .collect(Collectors.toCollection(TreeSet::new));

        val singletonListing = _Resources.loadAsStringUtf8(this.getClass(), "builtin-singleton.list");
        val expectedSingletons = _Strings.splitThenStreamTrimmed(singletonListing, "\n")
                .filter(entry->!entry.startsWith("#"))
                .collect(Collectors.toCollection(TreeSet::new));
        
        assertFalse(expectedSingletons.isEmpty());
        
        val servicesFound = toStringJoiningNewLine(managedServices);
        System.out.println("--- Beans discovered by Isis ---");
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
