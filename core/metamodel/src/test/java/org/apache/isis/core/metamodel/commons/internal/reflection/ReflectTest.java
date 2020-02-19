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

package org.apache.isis.core.metamodel.commons.internal.reflection;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.jaxb.JaxbServiceDefault;
import org.apache.isis.core.commons.internal.collections._Sets;
import org.apache.isis.core.commons.internal.reflection._Reflect.InterfacePolicy;
import org.apache.isis.core.metamodel.services.user.UserServiceDefault;

import static org.apache.isis.core.commons.internal.reflection._Reflect.getAnnotation;
import static org.apache.isis.core.commons.internal.reflection._Reflect.streamAllMethods;
import static org.apache.isis.core.commons.internal.reflection._Reflect.streamTypeHierarchy;

import lombok.val;

//TODO we are using real world classes from the framework, we could instead isolate these tests
// if we provide some custom classes for hierarchy traversal here (could be nested); 
// then move this test to the 'commons' module, where it belongs
class ReflectTest {

    @Test
    void typeHierarchy() {

        Class<?> type = UserServiceDefault.SudoServiceSpi.class;

        val typeSet = streamTypeHierarchy(type, InterfacePolicy.EXCLUDE)
                .map(Class::getName)
                .collect(Collectors.toSet());

        assertSetContainsAll(_Sets.<String>of(
                    "org.apache.isis.core.metamodel.services.user.UserServiceDefault$SudoServiceSpi",
                    "java.lang.Object"),
                typeSet);

    }
    

    @Test
    void typeHierarchyAndInterfaces() {

        Class<?> type = UserServiceDefault.SudoServiceSpi.class;

        val typeSet = streamTypeHierarchy(type, InterfacePolicy.INCLUDE)
                .map(Class::getName)
                .collect(Collectors.toSet());

        assertSetContainsAll(_Sets.<String>of(
                    "org.apache.isis.core.metamodel.services.user.UserServiceDefault$SudoServiceSpi",
                    "org.apache.isis.applib.services.sudo.SudoService$Spi",
                    "java.lang.Object"), 
                typeSet);

    }

    @Test
    void allMethods() {

        Class<?> type = UserServiceDefault.SudoServiceSpi.class;

        val typeSet = streamAllMethods(type, true)
                .map(m->m.toString())
                .collect(Collectors.toSet());

        assertSetContainsAll(_Sets.<String>of(
                "public abstract void org.apache.isis.applib.services.sudo.SudoService$Spi.releaseRunAs()",
                "public abstract void org.apache.isis.applib.services.sudo.SudoService$Spi.runAs(java.lang.String,java.util.List)",
                "public void org.apache.isis.core.metamodel.services.user.UserServiceDefault$SudoServiceSpi.releaseRunAs()",
                "public void org.apache.isis.core.metamodel.services.user.UserServiceDefault$SudoServiceSpi.runAs(java.lang.String,java.util.List)"),
            typeSet);

    }

    @Test
    void annotationLookup() throws NoSuchMethodException, SecurityException {

        Class<?> type = UserServiceDefault.SudoServiceSpi.class;
        Method method = type.getMethod("runAs", new Class[] {String.class, List.class});

        Programmatic annot = getAnnotation(method, Programmatic.class, true, true);

        assertNotNull(annot);
    }

    @Test
    void typeHierarchyAndInterfaces2() {

        Class<?> type = JaxbServiceDefault.class;

        val typeSet = streamTypeHierarchy(type, InterfacePolicy.INCLUDE)
                .map(t->t.getName())
                .collect(Collectors.toSet());

        assertSetContainsAll(_Sets.<String>of(
                "org.apache.isis.applib.services.jaxb.JaxbServiceDefault",
                "org.apache.isis.applib.services.jaxb.JaxbService$Simple",
                "org.apache.isis.applib.services.jaxb.JaxbService",
                "java.lang.Object"),
            typeSet);

    }


    // -- HELPER
    
    private static void assertSetContainsAll(Set<String> shouldContain, Set<String> actuallyContains) {
        assertTrue(_Sets.minus(shouldContain, actuallyContains).isEmpty());
    }

}
