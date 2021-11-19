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
package org.apache.isis.viewer.wicket.ui.components.scalars.uuid;

import java.util.Locale;
import java.util.UUID;

import org.apache.wicket.util.convert.ConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.core.config.valuetypes.ValueSemanticsRegistry;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.valuesemantics.UUIDValueSemantics;
import org.apache.isis.core.security._testing.InteractionService_forTesting;
import org.apache.isis.viewer.wicket.model.converter.ConverterBasedOnValueSemantics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

// see also BigDecimalConverter_roundtrip, for more rigorous testing
@SuppressWarnings("unused")
class UuidConverterTest_roundtrip {

    final UUID valid = UUID.randomUUID();

    private ConverterBasedOnValueSemantics<UUID> converter;

    private InteractionService interactionService;
    private MetaModelContext mmc;

    @BeforeEach
    void setUp() throws Exception {

        UUIDValueSemantics valueSemantics;
        mmc = MetaModelContext_forTesting.builder()
                .valueSemantic(valueSemantics = new UUIDValueSemantics())
                .interactionProvider(interactionService = new InteractionService_forTesting())
                .build();
        //valueSemantics.setSpecificationLoader(mmc.getSpecificationLoader());

        // pre-requisites for testing
        val reg = mmc.getServiceRegistry().lookupServiceElseFail(ValueSemanticsRegistry.class);
        assertNotNull(reg.selectValueSemantics(UUID.class));
        assertTrue(reg.selectValueSemantics(UUID.class).isNotEmpty());
        assertNotNull(mmc.getServiceRegistry().lookupServiceElseFail(InteractionService.class));
        assertNotNull(mmc.getInteractionProvider());


        converter = newConverter(CustomerWithUuid.class);
    }

    @Test
    void happy_case() {

        assertEquals(
                valid, converter.convertToObject(valid.toString(), Locale.ENGLISH));
        assertEquals(
                valid.toString(), converter.convertToString(valid, Locale.ENGLISH));
    }

    @Test
    void when_null() {
        assertNull(converter.convertToObject(null, Locale.ENGLISH));
        assertNull(converter.convertToObject("", Locale.ENGLISH));
        assertNull(converter.convertToString(null, Locale.ENGLISH));
    }

    @Test
    void invalid() {
        assertThrows(ConversionException.class,
                ()->converter.convertToObject("junk", Locale.ENGLISH),
                "Failed to convert 'junk' to a UUID");
    }

    // -- HELPER

    private ConverterBasedOnValueSemantics<UUID> newConverter(final Class<?> type) {
        val customerSpec = mmc.getSpecificationLoader().specForTypeElseFail(type);
        val prop = customerSpec.getPropertyElseFail("value");
        return new ConverterBasedOnValueSemantics<>(prop, ScalarRepresentation.EDITING);
    }

    // -- SCENARIOS

    @DomainObject
    static class CustomerWithUuid {
        @Property @Getter @Setter
        private UUID value;
    }


}
