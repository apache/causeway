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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.core.codegen.bytebuddy.services.ProxyFactoryServiceByteBuddy;
import org.apache.causeway.core.runtime.wrap.WrappingObject;
import org.apache.causeway.core.runtimeservices.RuntimeServicesTestAbstract;

class ProxyCreatorTestUsingCodegenPlugin extends RuntimeServicesTestAbstract {

    private ProxyGenerator proxyGenerator = new ProxyGenerator(new ProxyFactoryServiceByteBuddy());

    @DomainObject(nature = Nature.VIEW_MODEL)
    public static class Employee {
        private String name;
        public String getName() {
            return name;
        }
        public void setName(final String name) {
            this.name = name;
        }
    }

    @Test
    void proxyShouldDelegateCalls() {

        final Employee employee = new Employee();
        var employeeSpec = getMetaModelContext().getSpecificationLoader().loadSpecification(Employee.class);

        var proxy = proxyGenerator.objectProxy(employee, employeeSpec, SyncControl.control());

        assertNotNull(proxy);
        assertTrue(proxy instanceof WrappingObject);
        assertNotEquals(Employee.class.getName(), proxy.getClass().getName());
        assertNull(proxy.getName());

        // requires interaction infrastructure ... (however, tested with regression tests separately)
        //proxy.setName("hi");
        //assertEquals("hi", proxy.getName());
    }

}
