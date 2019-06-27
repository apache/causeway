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
package org.apache.isis.metamodel.facets.object.domainservice;

import org.junit.Test;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.metamodel.facets.object.domainservice.DomainServiceMenuOrder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DomainServiceMenuOrder_Test {

    @DomainService(menuOrder = "100")
    public static class ServiceWithDomainService100 {
    }

    @DomainServiceLayout(menuOrder = "100")
    public static class ServiceWithDomainServiceLayout100 {
    }

    @DomainService(menuOrder = "100")
    @DomainServiceLayout(menuOrder = "101")
    public static class ServiceWithDomainService100AndDomainServiceLayout101 {
    }

    @DomainService(menuOrder = "101")
    @DomainServiceLayout(menuOrder = "100")
    public static class ServiceWithDomainService101AndDomainServiceLayout100 {
    }

    @DomainService()
    @DomainServiceLayout()
    public static class ServiceWithDomainServiceAndDomainServiceLayout {
    }

    @DomainService()
    public static class ServiceWithDomainService {
    }

    @DomainServiceLayout()
    public static class ServiceWithDomainServiceLayout {
    }

    @Test
    public void orderOf() throws Exception {
        assertOrder(ServiceWithDomainService.class, Integer.MAX_VALUE - 100);
        assertOrder(ServiceWithDomainServiceLayout.class, Integer.MAX_VALUE - 100);
        assertOrder(ServiceWithDomainServiceAndDomainServiceLayout.class, Integer.MAX_VALUE - 100);

        assertOrder(ServiceWithDomainService100.class, 100);
        assertOrder(ServiceWithDomainServiceLayout100.class, 100);

        assertOrder(ServiceWithDomainService100AndDomainServiceLayout101.class, 100);
        assertOrder(ServiceWithDomainService101AndDomainServiceLayout100.class, 100);
    }

    private static void assertOrder(final Class<?> cls, final int expected) {
        String menuOrder = DomainServiceMenuOrder.orderOf(cls);
        assertThat(menuOrder, is(equalTo("" + expected)));
    }



}