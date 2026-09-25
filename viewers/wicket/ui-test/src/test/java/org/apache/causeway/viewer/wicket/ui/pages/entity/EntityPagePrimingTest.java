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
package org.apache.causeway.viewer.wicket.ui.pages.entity;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.priming.PrimingService;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.tester.WicketTester;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EntityPagePrimingTest {

    @Test
    void primesExactRootOncePerRequestAndAgainOnLaterRequest() {
        final WicketTester tester = new WicketTester();
        try {
            final Object page = new Object();
            final Object pojo = new Object();
            final ObjectSpecification specification = mock(ObjectSpecification.class);
            final ManagedObject objectAdapter = mock(ManagedObject.class);
            when(objectAdapter.objSpec()).thenReturn(specification);
            when(objectAdapter.getPojo()).thenReturn(pojo);
            final PrimingService primingService = mock(PrimingService.class);
            final ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
            when(serviceRegistry.lookupService(PrimingService.class))
                    .thenReturn(Optional.of(primingService));

            final RequestCycle firstRequest = tester.getRequestCycle();
            EntityPage.primeForRendering(page, objectAdapter, serviceRegistry);
            EntityPage.primeForRendering(page, objectAdapter, serviceRegistry);
            verify(primingService, times(1)).primeView(specification, pojo);

            tester.processRequest();
            final RequestCycle secondRequest = tester.getRequestCycle();
            assertNotSame(firstRequest, secondRequest);
            EntityPage.primeForRendering(page, objectAdapter, serviceRegistry);
            verify(primingService, times(2)).primeView(specification, pojo);
        } finally {
            tester.destroy();
        }
    }

    @Test
    void noRegisteredPrimingServiceLeavesRenderingUnchanged() {
        final WicketTester tester = new WicketTester();
        try {
            final ManagedObject objectAdapter = mock(ManagedObject.class);
            final ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
            when(serviceRegistry.lookupService(PrimingService.class))
                    .thenReturn(Optional.empty());

            EntityPage.primeForRendering(new Object(), objectAdapter, serviceRegistry);

            verify(objectAdapter, never()).objSpec();
        } finally {
            tester.destroy();
        }
    }

    @Test
    void propagatesViewPrimerFailure() {
        final WicketTester tester = new WicketTester();
        try {
            final ObjectSpecification specification = mock(ObjectSpecification.class);
            final Object pojo = new Object();
            final ManagedObject objectAdapter = mock(ManagedObject.class);
            when(objectAdapter.objSpec()).thenReturn(specification);
            when(objectAdapter.getPojo()).thenReturn(pojo);
            final PrimingService primingService = mock(PrimingService.class);
            final IllegalStateException expected = new IllegalStateException("prime failed");
            org.mockito.Mockito.doThrow(expected)
                    .when(primingService).primeView(specification, pojo);
            final ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
            when(serviceRegistry.lookupService(PrimingService.class))
                    .thenReturn(Optional.of(primingService));

            final IllegalStateException actual = assertThrows(
                    IllegalStateException.class,
                    () -> EntityPage.primeForRendering(
                            new Object(), objectAdapter, serviceRegistry));

            assertSame(expected, actual);
        } finally {
            tester.destroy();
        }
    }
}
