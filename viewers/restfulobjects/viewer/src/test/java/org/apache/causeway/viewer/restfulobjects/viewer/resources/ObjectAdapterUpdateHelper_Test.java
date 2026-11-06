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
package org.apache.causeway.viewer.restfulobjects.viewer.resources;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.rendering.context.ResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender.JsonValueEncoderService;

/**
 * Guards against a regression of a bug in the Restful Objects viewer: when persisting
 * a new object via {@code POST /objects/{type}}, the viewer must enforce property
 * hide/disable consents, so a caller cannot set properties that the domain model
 * marks as hidden or disabled on create.
 *
 * <p>{@link ObjectAdapterUpdateHelper#copyOverProperties(JsonRepresentation)}
 * is the code path used by {@code DomainObjectResourceServerside#persist(...)}.
 * A provided value for a hidden property is silently ignored; a provided value for
 * a disabled property is rejected (the operation fails).
 */
class ObjectAdapterUpdateHelper_Test {

    private static final String PROP_ID = "someProperty";

    private final MetaModelContext mmc = mock(MetaModelContext.class, RETURNS_DEEP_STUBS);
    private final JsonValueEncoderService jsonValueEncoder = mock(JsonValueEncoderService.class);
    private final ResourceContext resourceContext = mock(ResourceContext.class);
    private final ObjectSpecification ownerSpec = mock(ObjectSpecification.class);
    private final ObjectSpecification propertySpec = mock(ObjectSpecification.class);
    private final SpecificationLoader ownerSpecLoader = mock(SpecificationLoader.class);
    private final SpecificationLoader propertySpecLoader = mock(SpecificationLoader.class);
    private ManagedObject ownerAdapter;
    private ManagedObject valueAdapter;

    @BeforeEach
    void setUp() {
        when(resourceContext.getMetaModelContext()).thenReturn(mmc);
        when(resourceContext.interactionInitiatedBy()).thenReturn(InteractionInitiatedBy.USER);
        when(resourceContext.where()).thenReturn(Where.OBJECT_FORMS);
        when(mmc.getServiceRegistry().lookupServiceElseFail(JsonValueEncoderService.class))
                .thenReturn(jsonValueEncoder);
        when(ownerSpec.isValue()).thenReturn(true);
        when(ownerSpec.getSpecificationLoader()).thenReturn(ownerSpecLoader);
        when(ownerSpecLoader.specForType(any())).thenReturn(Optional.of(ownerSpec));

        when(propertySpec.isValue()).thenReturn(true);
        when(propertySpec.getSpecificationLoader()).thenReturn(propertySpecLoader);
        when(propertySpecLoader.specForType(any())).thenReturn(Optional.of(propertySpec));

        ownerAdapter = ManagedObject.value(ownerSpec, "owner");
        valueAdapter = ManagedObject.value(propertySpec, "someValue");

        when(jsonValueEncoder.asAdapter(any(), any(), any())).thenReturn(valueAdapter);
    }

    @Test
    void persisting_new_should_not_apply_hidden_property() {

        final var property = property(/*visible*/ false, /*usable*/ true);
        when(ownerSpec.streamAssociations(MixedIn.EXCLUDED)).thenReturn(Stream.of(property));

        final var helper = new ObjectAdapterUpdateHelper(resourceContext, ownerAdapter);
        final boolean allOk = helper.copyOverProperties(membersMap());

        // a hidden property must be silently ignored (never applied)
        verify(property, never()).set(any(), any(), any());
        assertTrue(allOk, "a hidden property being provided should be silently ignored, not flagged as an error");
    }

    @Test
    void persisting_new_should_not_apply_disabled_property() {

        final var property = property(/*visible*/ true, /*usable*/ false);
        when(ownerSpec.streamAssociations(MixedIn.EXCLUDED)).thenReturn(Stream.of(property));

        final var helper = new ObjectAdapterUpdateHelper(resourceContext, ownerAdapter);
        final boolean allOk = helper.copyOverProperties(membersMap());

        // a disabled property must be rejected (never applied)
        verify(property, never()).set(any(), any(), any());
        assertFalse(allOk, "a disabled property being provided should be rejected");
    }

    @Test
    void persisting_new_should_apply_visible_and_enabled_property() {

        final var property = property(/*visible*/ true, /*usable*/ true);
        when(ownerSpec.streamAssociations(MixedIn.EXCLUDED)).thenReturn(Stream.of(property));

        final var helper = new ObjectAdapterUpdateHelper(resourceContext, ownerAdapter);
        final boolean allOk = helper.copyOverProperties(membersMap());

        // control: a normal (visible and enabled) property is applied
        verify(property).set(any(), any(), any());
        assertTrue(allOk);
    }

    // -- HELPER

    private OneToOneAssociation property(final boolean visible, final boolean usable) {
        final var property = mock(OneToOneAssociation.class);
        when(property.isOneToOneAssociation()).thenReturn(true);
        when(property.getId()).thenReturn(PROP_ID);
        when(property.getElementType()).thenReturn(propertySpec);
        when(property.isMandatory()).thenReturn(false);
        when(property.isVisible(any(), any(), any())).thenReturn(visible ? allow() : veto("hidden"));
        when(property.isUsable(any(), any(), any())).thenReturn(usable ? allow() : veto("disabled"));
        when(property.isAssociationValid(any(), any(), any())).thenReturn(allow());
        return property;
    }

    private JsonRepresentation membersMap() {
        final var propertyRepr = JsonRepresentation.newMap();
        propertyRepr.mapPutString("value", "someValue");
        final var members = JsonRepresentation.newMap();
        members.mapPutJsonRepresentation(PROP_ID, propertyRepr);
        return members;
    }

    private static Consent allow() {
        return Consent.Allow.DEFAULT;
    }

    private static Consent veto(final String reason) {
        return new Consent.Veto(reason);
    }

}

