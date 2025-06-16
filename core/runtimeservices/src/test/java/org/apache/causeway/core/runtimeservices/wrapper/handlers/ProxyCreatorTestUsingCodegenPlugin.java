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
package org.apache.causeway.core.runtimeservices.wrapper.handlers;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.core.codegen.bytebuddy.services.ProxyFactoryServiceByteBuddy;
import org.apache.causeway.core.runtime.wrap.WrapperInvocationHandler;

import lombok.Getter;
import lombok.Setter;

class ProxyCreatorTestUsingCodegenPlugin {

    private ProxyGenerator proxyGenerator;

    @BeforeEach
    void setUp() throws Exception {
        proxyGenerator = new ProxyGenerator(new ProxyFactoryServiceByteBuddy());
    }

    public static class Employee {
        private String name;
        public String getName() {
            return name;
        }
        public void setName(final String name) {
            this.name = name;
        }
    }

    private static class WrapperInvocationHandlerForTest implements WrapperInvocationHandler {
        private final Employee delegate = new Employee();
        private final Set<String> invoked = new HashSet<>();
        private final WrapperInvocationHandler.Context context = new WrapperInvocationHandler.Context(
                delegate, null, null, null, null, null);
                

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            invoked.add(method.getName());
            return "hi";
        }

        @Getter @Setter 
        private boolean resolveObjectChangedEnabled = false;

        public boolean wasInvoked(final String methodName) {
            return invoked.contains(methodName);
        }

        @Override
        public WrapperInvocationHandler.Context context() {
            return context;
        }
        
    }

    @Test
    void proxyShouldDelegateCalls() {

        final WrapperInvocationHandlerForTest handler = new WrapperInvocationHandlerForTest();
        final Employee proxyOfEmployee = proxyGenerator.instantiateProxy(handler);

        assertNotNull(proxyOfEmployee);

        assertNotEquals(Employee.class.getName(), proxyOfEmployee.getClass().getName());

        assertFalse(handler.wasInvoked("getName"));

        assertEquals("hi", proxyOfEmployee.getName());

        assertTrue(handler.wasInvoked("getName"));

    }

}
