/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.testdomain.tests;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.resources._Json;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.runtime.spring.IsisBoot;
import org.apache.isis.testdomain.jdo.JdoTestDomainModule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {JdoTestDomainModule.class, IsisBoot.class})
class SpringServiceProvisioningTest {

    @BeforeEach
    void setUp() {
     
    }
    
    @Test
    void builtInServicesShouldBeSetUp() throws IOException {
        
        val serviceRegistry = IsisContext.getServiceRegistry();
        val managedServices = serviceRegistry.streamRegisteredBeans()
                .map(BeanAdapter::getBeanClass)
                .map(Class::getName)
                .collect(Collectors.toCollection(TreeSet::new));
        
        val singletonJson = _Resources.loadAsString(this.getClass(), "builtin-singleton.json", StandardCharsets.UTF_8);
        val singletonSet = new TreeSet<>(_Json.readJsonList(String.class, singletonJson));
        
        // same as managedServices.containsAll(singletonSet) but more verbose in case of failure        
        assertEquals(setToString(singletonSet), setToString(_Sets.intersectSorted(managedServices, singletonSet)));
        
        //TODO also test for request-scoped service (requires a means to mock a request-context)
        
    }
    
    private static String setToString(Set<?> set){
        return set.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }
        
}
