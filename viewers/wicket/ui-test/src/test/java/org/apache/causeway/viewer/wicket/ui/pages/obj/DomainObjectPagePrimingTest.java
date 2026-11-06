/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.wicket.ui.pages.obj;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.priming.PrimingService;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.util.PageUtils;

class DomainObjectPagePrimingTest {
    @Test void initialRepeatedPreparationAndLaterFullAjaxRequestsUseFreshAdmission() {
        var tester = new WicketTester();
        try {
            var f = new Fixture();
            var pageIdentity = new Object();
            var root = new WebMarkupContainer("root") {
                private static final long serialVersionUID = 1L;
                @Override protected void onInitialize() {
                    super.onInitialize();
                    f.prime(pageIdentity);
                }
                @Override protected void onConfigure() {
                    super.onConfigure();
                    f.prime(pageIdentity);
                }
            };
            var link = new AjaxLink<Void>("refresh") {
                private static final long serialVersionUID = 1L;
                @Override public void onClick(final AjaxRequestTarget target) {
                    assertTrue(PageUtils.isAjax());
                    f.prime(pageIdentity);
                    f.prime(pageIdentity);
                    target.add(root);
                }
            };
            root.setOutputMarkupId(true);
            root.add(link);
            tester.startComponentInPage(root, Markup.of("<div wicket:id='root'><a wicket:id='refresh'>refresh</a></div>"));
            assertEquals(1, f.calls.get());
            var first = RequestCycle.get();
            tester.startPage(tester.getLastRenderedPage());
            assertEquals(2, f.calls.get());
            assertNotSame(first, RequestCycle.get());
            tester.executeAjaxEvent(link, "click");
            assertEquals(3, f.calls.get());
        } finally { tester.destroy(); }
    }

    @Test void actualInitialPageHookPrimesBeforeLayoutMetadataAndDoesNotRetry() throws Exception {
        var tester = new WicketTester();
        try {
            var f = new Fixture();
            var page = mock(DomainObjectPage.class);
            doCallRealMethod().when(page).onInitialize();
            when(page.getServiceRegistry()).thenReturn(f.services);
            var field = DomainObjectPage.class.getDeclaredField("model");
            field.setAccessible(true);
            field.set(page, f.model);
            // Stop at the next preparation boundary instead of bootstrapping a full domain UI.
            var boundary = new IllegalStateException("layout metadata reached");
            when(f.spec.getFeatureIdentifier()).thenAnswer(call -> {
                assertEquals(1, f.calls.get());
                throw boundary;
            });
            assertSame(boundary, assertThrows(IllegalStateException.class, page::onInitialize));
            assertSame(boundary, assertThrows(IllegalStateException.class, page::onInitialize));
            assertEquals(1, f.calls.get());
        } finally { tester.destroy(); }
    }

    @Test void actualDomainPageNewRequestHookAuthorizesAndPrimesOnce() throws Exception {
        var tester = new WicketTester();
        try {
            var f = new Fixture();
            var page = mock(DomainObjectPage.class);
            doCallRealMethod().when(page).onNewRequestCycle();
            when(page.getServiceRegistry()).thenReturn(f.services);
            var field = DomainObjectPage.class.getDeclaredField("model");
            field.setAccessible(true);
            field.set(page, f.model);
            page.onNewRequestCycle();
            page.onNewRequestCycle();
            assertEquals(1, f.calls.get());
            tester.processRequest();
            page.onNewRequestCycle();
            assertEquals(2, f.calls.get());
        } finally { tester.destroy(); }
    }

    @Test void unauthorizedRootCannotReachRegistryOrPrimerAndDoesNotConsumeAdmission() {
        var tester = new WicketTester();
        try {
            var f = new Fixture();
            var page = new Object();
            when(f.model.isVisible()).thenReturn(false);
            assertThrows(ObjectMember.AuthorizationException.class, () -> f.prime(page));
            verifyNoInteractions(f.services);
            assertEquals(0, f.calls.get());
            when(f.model.isVisible()).thenReturn(true);
            f.prime(page);
            assertEquals(1, f.calls.get());
        } finally { tester.destroy(); }
    }

    @Test void admissionUsesPageIdentityNotEquality() {
        var tester = new WicketTester();
        try {
            var f = new Fixture();
            var first = new EqualPage();
            var second = new EqualPage();
            assertEquals(first, second);
            f.prime(first);
            f.prime(second);
            f.prime(first);
            assertEquals(2, f.calls.get());
        } finally { tester.destroy(); }
    }

    @Test void failureIsIdenticalAndNotRetriedWithinRequest() {
        var tester = new WicketTester();
        try {
            var f = new Fixture();
            var page = new Object();
            var failure = new IllegalStateException("view primer failed");
            doAnswer(call -> { f.calls.incrementAndGet(); throw failure; }).when(f.priming).primeView(any(), any());
            assertSame(failure, assertThrows(IllegalStateException.class, () -> f.prime(page)));
            f.prime(page);
            assertEquals(1, f.calls.get());
            tester.processRequest();
            assertSame(failure, assertThrows(IllegalStateException.class, () -> f.prime(page)));
            assertEquals(2, f.calls.get());
        } finally { tester.destroy(); }
    }

    @Test void missingPrimingServiceLeavesRootUntouchedAfterAuthorization() {
        var tester = new WicketTester();
        try {
            var model = mock(UiObjectWkt.class);
            when(model.isVisible()).thenReturn(true);
            var services = mock(ServiceRegistry.class);
            when(services.lookupService(PrimingService.class)).thenReturn(Optional.empty());
            DomainObjectPage.primeForRendering(new Object(), model, null, services);
            verify(model).isVisible();
        } finally { tester.destroy(); }
    }

    private static final class EqualPage {
        @Override public boolean equals(final Object obj) { return obj instanceof EqualPage; }
        @Override public int hashCode() { return 1; }
    }
    private static final class Fixture {
        final AtomicInteger calls = new AtomicInteger();
        final Object pojo = new Object();
        final ObjectSpecification spec = mock(ObjectSpecification.class, RETURNS_DEEP_STUBS);
        final ManagedObject root;
        final UiObjectWkt model = mock(UiObjectWkt.class);
        final PrimingService priming = mock(PrimingService.class);
        final ServiceRegistry services = mock(ServiceRegistry.class);
        Fixture() {
            when(spec.getSpecificationLoader().specForType(Object.class)).thenReturn(Optional.of(spec));
            root = ManagedObject.other(spec, pojo);
            when(model.getObject()).thenReturn(root);
            when(model.isVisible()).thenReturn(true);
            when(services.lookupService(PrimingService.class)).thenReturn(Optional.of(priming));
            doAnswer(call -> {
                assertSame(spec, call.getArgument(0));
                assertSame(pojo, call.getArgument(1));
                calls.incrementAndGet();
                return null;
            }).when(priming).primeView(any(), any());
        }
        void prime(final Object page) { DomainObjectPage.primeForRendering(page, model, root, services); }
    }
}
