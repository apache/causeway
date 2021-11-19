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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.valuesemantics.UUIDValueSemantics;
import org.apache.isis.viewer.wicket.ui.components.scalars.ConverterTester;

import lombok.Getter;
import lombok.Setter;

class UuidConverterTest {

    final UUID valid = UUID.randomUUID();
    ConverterTester<UUID> converterTester;

    @BeforeEach
    void setUp() throws Exception {
        converterTester = new ConverterTester<>(UUID.class, new UUIDValueSemantics());
        converterTester.setScenario(
                Locale.ENGLISH,
                converterTester.converterForProperty(
                        CustomerWithUuid.class, "value", ScalarRepresentation.EDITING));
    }

    @Test
    void happy_case() {
        converterTester.assertRoundtrip(valid, valid.toString());
    }

    @Test
    void when_null() {
        converterTester.assertHandlesEmpty();
    }

    @Test
    void invalid() {
        converterTester.assertConversionFailure("junk", "Invalid UUID string: junk");
    }

    // -- SCENARIOS

    @DomainObject
    static class CustomerWithUuid {
        @Property @Getter @Setter
        private UUID value;
    }

}
