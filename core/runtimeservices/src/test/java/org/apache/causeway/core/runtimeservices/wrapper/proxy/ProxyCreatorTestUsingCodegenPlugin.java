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
package org.apache.causeway.core.runtimeservices.wrapper.proxy;

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
import org.apache.causeway.core.runtimeservices.wrapper.handlers.DelegatingInvocationHandler;

class ProxyCreatorTestUsingCodegenPlugin {

    private ProxyCreator proxyCreator;

    @BeforeEach
    void setUp() throws Exception {
        proxyCreator = new ProxyCreator(new ProxyFactoryServiceByteBuddy());
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

    private static class DelegatingInvocationHandlerForTest implements DelegatingInvocationHandler<Employee> {
        private final Employee delegate = new Employee();
        private final Set<String> invoked = new HashSet<>();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            invoked.add(method.getName());
            return "hi";
        }

        @Override
        public Employee getDelegate() {
            return delegate;
        }

        @Override
        public boolean isResolveObjectChangedEnabled() {
            return false;
        }

        @Override
        public void setResolveObjectChangedEnabled(boolean resolveObjectChangedEnabled) {
        }

        public boolean wasInvoked(String methodName) {
            return invoked.contains(methodName);
        }
    }

    @Test
    void proxyShouldDelegateCalls() {

        final DelegatingInvocationHandlerForTest handler = new DelegatingInvocationHandlerForTest();
        final Employee proxyOfEmployee = proxyCreator.instantiateProxy(handler);

        assertNotNull(proxyOfEmployee);

        assertNotEquals(Employee.class.getName(), proxyOfEmployee.getClass().getName());

        assertFalse(handler.wasInvoked("getName"));

        assertEquals("hi", proxyOfEmployee.getName());

        assertTrue(handler.wasInvoked("getName"));

    }

}
