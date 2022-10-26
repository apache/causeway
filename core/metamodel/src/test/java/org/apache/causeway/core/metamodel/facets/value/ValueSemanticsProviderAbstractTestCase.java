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
package org.apache.causeway.core.metamodel.facets.value;

import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.object.value.ValueSerializer;
import org.apache.causeway.core.metamodel.facets.object.value.ValueSerializer.Format;
import org.apache.causeway.core.metamodel.facets.object.value.ValueSerializerDefault;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.valuesemantics.StringValueSemantics;

import lombok.Getter;

public abstract class ValueSemanticsProviderAbstractTestCase<T> {

    protected InteractionService mockInteractionService;
    protected ManagedObject mockAdapter;

    protected MetaModelContext metaModelContext;

    @Getter private ValueSemanticsProvider<T> semantics;
    @Getter private ValueSerializer<T> valueSerializer;

    @BeforeEach
    public void setUp() throws Exception {

        Locale.setDefault(Locale.UK);

        mockInteractionService = Mockito.mock(InteractionService.class);
        mockAdapter = Mockito.mock(ManagedObject.class);

        metaModelContext = MetaModelContext_forTesting.builder()
                .interactionService(mockInteractionService)
                .build();
    }

    protected void allowMockAdapterToReturn(final Object pojo) {
        Mockito.when(mockAdapter.getPojo()).thenReturn(pojo);
    }

    protected void setSemantics(final ValueSemanticsAbstract<T> valueSemantics) {
        this.semantics = valueSemantics;
        this.valueSerializer = ValueSerializerDefault
                .forSemantics(valueSemantics);
    }

    protected ManagedObject createAdapter(final Object object) {
        return mockAdapter;
    }

    protected Parser<T> getParser() {
        return semantics.getParser();
    }

    protected Renderer<T> getRenderer() {
        return semantics.getRenderer();
    }

    @Test
    public void testParseNull() throws Exception {
        if(!isValueSemanticsProviderSetup()) return;
        assertEquals(null, semantics.getParser().parseTextRepresentation(null, null));
    }

    @Test
    public void testParseEmptyString() throws Exception {
        if(!isValueSemanticsProviderSetup()) return;

        final Object newValue = semantics.getParser().parseTextRepresentation(null, "");

        if(semantics instanceof StringValueSemantics) {
            // string parsing is an unary identity
            assertEquals("", newValue);
        } else {
            assertNull(newValue);
        }

    }

    @ParameterizedTest
    @EnumSource(Format.class)
    public void testValueSerializer(final Format format) {
        if(!isValueSemanticsProviderSetup()) return;

        final T value = getSample();
        final String encoded = getValueSerializer().enstring(format, value);

        switch(format) {
        case JSON:
            assertValueEncodesToJsonAs(value, encoded);
            break;
        case URL_SAFE:
            assertTrue(_Strings.isUrlSafe(encoded));
        }

        T decoded = getValueSerializer().destring(format, encoded);

        Optional.ofNullable(semantics.getOrderRelation())
            .ifPresentOrElse(rel->Assertions.assertTrue(rel.equals(value, decoded)),
                    ()->Assertions.assertEquals(value, decoded));
    }

    protected abstract T getSample();
    protected abstract void assertValueEncodesToJsonAs(T a, String json);


    @ParameterizedTest
    @EnumSource(Format.class)
    public void testDecodeNULL(final Format format) throws Exception {
        if(!isValueSemanticsProviderSetup()) return;

        final Object newValue = getValueSerializer()
                .destring(format, ValueSerializerDefault.ENCODED_NULL);
        assertNull(newValue);
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    public void testEmptyEncoding(final Format format) {
        if(!isValueSemanticsProviderSetup()) return;

        assertEquals(ValueSerializerDefault.ENCODED_NULL, getValueSerializer()
                .enstring(format, null));
    }

    @Test
    public void testTitleOfForNullObject() {
        if(!isValueSemanticsProviderSetup()) return;

        if(semantics instanceof StringValueSemantics) {
            // string representation has null-to-empty semantics
            assertEquals("",
                    semantics.getRenderer().titlePresentation(null, null));
        } else {
            assertEquals("(none)",
                    semantics.getRenderer().titlePresentation(null, null));
        }

    }

    // precondition for testing
    private boolean isValueSemanticsProviderSetup() {
        return semantics!=null;
    }



}
