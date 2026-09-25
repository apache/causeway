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
package org.apache.causeway.core.metamodel.services.priming;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.services.priming.PrimingRegistrar;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrimingRegistryDefaultTest {

    static class Invoice {}
    static class InvoiceSubtype extends Invoice {}
    static class Unknown {}

    private SpecificationLoader specificationLoader;
    private ServiceRegistry serviceRegistry;
    private ObjectSpecification invoiceSpecification;
    private ObjectSpecification subtypeSpecification;

    @BeforeEach
    void setUp() {
        specificationLoader = mock(SpecificationLoader.class);
        serviceRegistry = mock(ServiceRegistry.class);
        invoiceSpecification = specification(Invoice.class, "test.Invoice", "approve", "recalculate");
        subtypeSpecification = specification(InvoiceSubtype.class, "test.InvoiceSubtype");
        when(specificationLoader.snapshotSpecifications())
                .thenReturn(Can.of(invoiceSpecification, subtypeSpecification));
    }

    @Test
    void registersSeveralActionsAndViewAndDispatchesByExactKey() {
        final List<String> calls = new ArrayList<>();
        final PrimingRegistrar registrar = registry -> {
            registry.actions(
                    Invoice.class,
                    Arrays.asList("approve", "recalculate"),
                    (invoice, arguments) -> calls.add("action:" + arguments.get(0, String.class)));
            registry.view(Invoice.class, invoice -> calls.add("view"));
        };
        when(serviceRegistry.select(PrimingRegistrar.class)).thenReturn(Can.of(registrar));
        final PrimingRegistryDefault registry = initializedRegistry();
        final Invoice invoice = new Invoice();

        registry.primeAction(
                invoiceSpecification,
                "approve",
                invoice,
                Collections.singletonList("argument"));
        registry.primeView(invoiceSpecification, invoice);
        registry.primeView(subtypeSpecification, new InvoiceSubtype());

        assertEquals(Arrays.asList("action:argument", "view"), calls);
    }

    @Test
    void invokesEveryMatchingPrimerInRegistrationOrder() {
        final List<String> calls = new ArrayList<>();
        final PrimingRegistrar first = registry ->
                registry.view(Invoice.class, invoice -> calls.add("first"));
        final PrimingRegistrar second = registry ->
                registry.view(Invoice.class, invoice -> calls.add("second"));
        when(serviceRegistry.select(PrimingRegistrar.class)).thenReturn(Can.of(first, second));
        final PrimingRegistryDefault registry = initializedRegistry();

        registry.primeView(invoiceSpecification, new Invoice());

        assertEquals(Arrays.asList("first", "second"), calls);
    }

    @Test
    void unknownClassFailsDuringInitializationWithRegistrarContext() {
        final PrimingRegistrar registrar = registry -> registry.view(Unknown.class, unknown -> {});
        when(serviceRegistry.select(PrimingRegistrar.class)).thenReturn(Can.of(registrar));
        final PrimingRegistryDefault registry = new PrimingRegistryDefault(
                specificationLoader, serviceRegistry);
        registry.onMetamodelAboutToBeLoaded();

        final IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                registry::onMetamodelLoaded);

        assertTrue(failure.getMessage().contains(registrar.getClass().getName()));
        assertTrue(failure.getCause().getMessage().contains(Unknown.class.getName()));
    }

    @Test
    void unknownOrNonLocalActionNameFailsDuringInitialization() {
        final PrimingRegistrar registrar = registry ->
                registry.action(Invoice.class, "test.Invoice#missing", (invoice, arguments) -> {});
        when(serviceRegistry.select(PrimingRegistrar.class)).thenReturn(Can.of(registrar));
        final PrimingRegistryDefault registry = new PrimingRegistryDefault(
                specificationLoader, serviceRegistry);
        registry.onMetamodelAboutToBeLoaded();

        final IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                registry::onMetamodelLoaded);

        assertTrue(failure.getCause().getMessage().contains("test.Invoice#missing"));
    }

    @Test
    void rejectsRegistrationAfterRegistryIsFrozen() {
        when(serviceRegistry.select(PrimingRegistrar.class)).thenReturn(Can.empty());
        final PrimingRegistryDefault registry = initializedRegistry();

        assertThrows(IllegalStateException.class,
                () -> registry.view(Invoice.class, invoice -> {}));
    }

    private PrimingRegistryDefault initializedRegistry() {
        final PrimingRegistryDefault registry = new PrimingRegistryDefault(
                specificationLoader, serviceRegistry);
        registry.onMetamodelAboutToBeLoaded();
        registry.onMetamodelLoaded();
        return registry;
    }

    private static ObjectSpecification specification(
            final Class<?> correspondingClass,
            final String logicalTypeName,
            final String... actionNames) {
        final ObjectSpecification specification = mock(ObjectSpecification.class);
        org.mockito.Mockito.doReturn(correspondingClass)
                .when(specification).getCorrespondingClass();
        when(specification.logicalTypeName()).thenReturn(logicalTypeName);
        for (String actionName : actionNames) {
            final ObjectAction action = mock(ObjectAction.class);
            final Identifier identifier = mock(Identifier.class);
            when(identifier.memberLogicalName()).thenReturn(actionName);
            when(action.getFeatureIdentifier()).thenReturn(identifier);
            when(specification.getAction(actionName, MixedIn.INCLUDED))
                    .thenReturn(Optional.of(action));
        }
        return specification;
    }
}
