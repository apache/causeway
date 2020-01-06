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
package org.apache.isis.wrapper.proxy;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.codegen.bytebuddy.services.ProxyFactoryServiceByteBuddy;
import org.apache.isis.runtime.services.wrapper.handlers.DelegatingInvocationHandler;
import org.apache.isis.runtime.services.wrapper.proxy.ProxyCreator;

public class ProxyCreatorTestUsingCodegenPlugin {

    private ProxyCreator proxyCreator;

    @Before
    public void setUp() throws Exception {
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
    public void proxyShouldDelegateCalls() {

        final DelegatingInvocationHandlerForTest handler = new DelegatingInvocationHandlerForTest();
        final Employee proxyOfEmployee = proxyCreator.instantiateProxy(handler);

        Assert.assertNotNull(proxyOfEmployee);

        Assert.assertNotEquals(Employee.class.getName(), proxyOfEmployee.getClass().getName());

        Assert.assertFalse(handler.wasInvoked("getName"));

        Assert.assertEquals("hi", proxyOfEmployee.getName());

        Assert.assertTrue(handler.wasInvoked("getName"));

    }

}
