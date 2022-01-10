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
package org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.valuesemantics.temporal.LocalDateTimeValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.temporal.legacy.JavaUtilDateValueSemantics;
import org.apache.isis.viewer.wicket.ui.components.scalars.ConverterTester;

import lombok.Getter;
import lombok.Setter;

class JavaUtilDateConverterTest {

    @SuppressWarnings("deprecation")
    final java.util.Date valid = new java.util.Date(2013-1900, 03-1, 13, 17, 15, 59);
    ConverterTester<java.util.Date> converterTester;

    @BeforeEach
    void setUp() throws Exception {

        converterTester = new ConverterTester<java.util.Date>(java.util.Date.class,
                new JavaUtilDateValueSemantics(),
                new LocalDateTimeValueSemantics());
        converterTester.setScenario(
                Locale.ENGLISH,
                converterTester.converterForProperty(
                        CustomerWithJavaSqlDate.class, "value", ScalarRepresentation.EDITING));
    }

    @Test
    void happy_case() {
        converterTester.assertRoundtrip(valid, "2013-03-13 17:15:59");
    }

    @Test
    void when_null() {
        converterTester.assertHandlesEmpty();
    }

    @Test
    void invalid() {
        converterTester.assertConversionFailure("junk", "Not recognised as a java.time.LocalDateTime: junk");
    }

    // -- SCENARIOS

    @DomainObject
    static class CustomerWithJavaSqlDate {
        @Property @Getter @Setter
        private java.sql.Date value;
    }

}
