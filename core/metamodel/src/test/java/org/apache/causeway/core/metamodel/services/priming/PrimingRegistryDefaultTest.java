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
package org.apache.causeway.core.metamodel.services.priming;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.priming.PrimingRegistrar;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

class PrimingRegistryDefaultTest {
    @Test void collectsAfterLoadingAndFreezesMultipleDeterministicCallbacks() {
        var f = new Fixture();
        var calls = new ArrayList<String>();
        var invoice = new Invoice();
        f.registrars(registry -> {
            registry.actions(Invoice.class, List.of("approve", "recalculate"), (target, args) -> {
                assertSame(invoice, target);
                assertEquals("argument", args.get(0, String.class));
                calls.add("first");
            });
            registry.view(Invoice.class, target -> calls.add("view"));
        }, registry -> registry.action(Invoice.class, "approve", (target, args) -> calls.add("second")));
        f.registry.onMetamodelAboutToBeLoaded();
        assertThrows(IllegalStateException.class, () -> f.registry.view(Invoice.class, target -> { }));
        f.registry.primeAction(f.spec, "approve", invoice, List.of("argument"));
        assertTrue(calls.isEmpty());
        f.registry.onMetamodelLoaded();
        f.registry.primeAction(f.spec, "approve", invoice, List.of("argument"));
        f.registry.primeAction(f.spec, "recalculate", invoice, List.of("argument"));
        f.registry.primeView(f.spec, invoice);
        assertEquals(List.of("first", "second", "first", "view"), calls);
        assertThrows(IllegalStateException.class, () -> f.registry.action(Invoice.class, "approve", (target, args) -> { }));
        assertThrows(IllegalStateException.class, () -> f.registry.view(Invoice.class, target -> { }));
    }

    @Test void exactTypesDoNotMatchSubtypesAndUnregisteredNamesDoNothing() {
        var f = new Fixture();
        var count = new AtomicInteger();
        f.registrars(registry -> {
            registry.action(Invoice.class, "approve", (target, args) -> count.incrementAndGet());
            registry.view(Invoice.class, target -> count.incrementAndGet());
        });
        f.registry.onMetamodelLoaded();
        var subtype = mock(ObjectSpecification.class);
        when(subtype.logicalTypeName()).thenReturn("demo.SpecialInvoice");
        f.registry.primeAction(subtype, "approve", new SpecialInvoice(), List.of());
        f.registry.primeView(subtype, new SpecialInvoice());
        f.registry.primeAction(f.spec, "unknown", new Invoice(), List.of());
        assertEquals(0, count.get());
    }

    @Test void contributedActionResolvesAgainstExactMixeeSpecification() {
        var f = new Fixture();
        var target = new Invoice();
        when(f.approve.isDeclaredOnMixin()).thenReturn(true);
        var calls = new AtomicInteger();
        f.registrars(registry -> registry.action(Invoice.class, "approve", (actual, args) -> {
            assertSame(target, actual);
            calls.incrementAndGet();
        }));
        f.registry.onMetamodelLoaded();
        f.registry.primeAction(f.spec, "approve", target, List.of());
        assertEquals(1, calls.get());
        verify(f.spec).getAction("approve", MixedIn.INCLUDED);
    }

    @Test void invalidClassesAndActionNamesFailWithDiagnosticsAndCloseWindow() {
        var registrations = List.<PrimingRegistrar>of(
                registry -> registry.view(SpecialInvoice.class, target -> { }),
                registry -> registry.action(Invoice.class, "missing", (target, args) -> { }),
                registry -> registry.action(Invoice.class, "demo.Invoice#approve", (target, args) -> { }),
                registry -> registry.action(Invoice.class, " approve", (target, args) -> { }),
                registry -> registry.action(Invoice.class, "", (target, args) -> { }),
                registry -> registry.actions(Invoice.class, List.of(), (target, args) -> { }));
        for (var registration : registrations) {
            var f = new Fixture();
            f.registrars(registration);
            var failure = assertThrows(IllegalStateException.class, f.registry::onMetamodelLoaded);
            assertTrue(failure.getMessage().contains(registration.getClass().getName()));
            assertTrue(failure.getCause().getMessage().contains("domainType="));
            assertThrows(IllegalStateException.class, () -> f.registry.view(Invoice.class, target -> { }));
        }
    }

    @Test void similarButNonexactActionResolutionIsRejected() {
        var f = new Fixture();
        when(f.approve.getFeatureIdentifier()).thenReturn(Identifier.actionIdentifier(LogicalType.fqcn(Invoice.class), "another"));
        f.registrars(registry -> registry.action(Invoice.class, "approve", (target, args) -> { }));
        assertThrows(IllegalStateException.class, f.registry::onMetamodelLoaded);
    }

    @Test void failedRegistrationDoesNotPublishPartialCallbacks() {
        var f = new Fixture();
        var count = new AtomicInteger();
        f.registrars(registry -> {
            registry.view(Invoice.class, target -> count.incrementAndGet());
            registry.action(Invoice.class, "approve", (target, args) -> count.incrementAndGet());
            throw new IllegalStateException("startup failure");
        });
        assertThrows(IllegalStateException.class, f.registry::onMetamodelLoaded);
        f.registry.primeView(f.spec, new Invoice());
        f.registry.primeAction(f.spec, "approve", new Invoice(), List.of());
        assertEquals(0, count.get());
        assertThrows(IllegalStateException.class, () -> f.registry.view(Invoice.class, target -> { }));
    }

    @Test void callbackFailuresEscapeIdenticallyWithoutRetry() {
        var f = new Fixture();
        var count = new AtomicInteger();
        var failure = new IllegalStateException("callback");
        f.registrars(registry -> {
            registry.action(Invoice.class, "approve", (target, args) -> { count.incrementAndGet(); throw failure; });
            registry.view(Invoice.class, target -> { count.incrementAndGet(); throw failure; });
        });
        f.registry.onMetamodelLoaded();
        assertSame(failure, assertThrows(IllegalStateException.class, () -> f.registry.primeAction(f.spec, "approve", new Invoice(), List.of())));
        assertSame(failure, assertThrows(IllegalStateException.class, () -> f.registry.primeView(f.spec, new Invoice())));
        assertEquals(2, count.get());
    }

    @Test void metamodelModuleRegistersRegistryAndNoRegistrarsIsSafe() {
        assertTrue(Arrays.asList(CausewayModuleCoreMetamodel.class.getAnnotation(Import.class).value()).contains(PrimingRegistryDefault.class));
        var f = new Fixture();
        f.registrars();
        f.registry.onMetamodelLoaded();
        f.registry.primeView(f.spec, new Invoice());
        f.registry.primeAction(f.spec, "approve", new Invoice(), List.of());
    }

    private static class Invoice { }
    private static class SpecialInvoice extends Invoice { }
    private static final class Fixture {
        final SpecificationLoader loader = mock(SpecificationLoader.class);
        final ServiceRegistry services = mock(ServiceRegistry.class);
        final ObjectSpecification spec = mock(ObjectSpecification.class);
        final ObjectAction approve = mock(ObjectAction.class);
        final PrimingRegistryDefault registry = new PrimingRegistryDefault(loader, services);
        Fixture() {
            doReturn(Invoice.class).when(spec).correspondingClass();
            when(spec.logicalTypeName()).thenReturn("demo.Invoice");
            doReturn(Can.of(spec)).when(loader).snapshotSpecifications();
            when(approve.getFeatureIdentifier()).thenReturn(Identifier.actionIdentifier(LogicalType.eager(Invoice.class, "demo.Invoice"), "approve"));
            var recalculate = mock(ObjectAction.class);
            when(recalculate.getFeatureIdentifier()).thenReturn(Identifier.actionIdentifier(LogicalType.eager(Invoice.class, "demo.Invoice"), "recalculate"));
            when(spec.getAction("approve", MixedIn.INCLUDED)).thenReturn(Optional.of(approve));
            when(spec.getAction("recalculate", MixedIn.INCLUDED)).thenReturn(Optional.of(recalculate));
        }
        void registrars(final PrimingRegistrar... registrars) {
            when(services.select(PrimingRegistrar.class)).thenReturn(Can.ofArray(registrars));
        }
    }
}
