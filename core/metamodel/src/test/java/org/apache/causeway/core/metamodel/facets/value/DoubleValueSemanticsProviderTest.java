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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.causeway.core.metamodel.valuesemantics.DoubleValueSemantics;

class DoubleValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<Double> {

    private DoubleValueSemantics value;
    private Double doubleObj;

    @BeforeEach
    public void setUpObjects() throws Exception {

        setSemantics(value = new DoubleValueSemantics());

        doubleObj = Double.valueOf(32.5d);
        allowMockAdapterToReturn(doubleObj);
    }

    @Test
    public void testValue() {
        assertEquals("32.5", value.titlePresentation(null, doubleObj));
    }

    @Test
    public void testInvalidParse() throws Exception {
        try {
            value.parseTextRepresentation(null, "one");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    @Test
    public void testTitleOf() {
        assertEquals("35,000,000", value.titlePresentation(null, Double.valueOf(35000000.0)));
    }

    @Test
    public void testParse() throws Exception {
        final Object newValue = value.parseTextRepresentation(null, "120.56");
        assertEquals(120.56, ((Double) newValue).doubleValue(), 0.0);
    }

    @Test
    public void testParse2() throws Exception {
        final Object newValue = value.parseTextRepresentation(null, "1,20.0");
        assertEquals(120, ((Double) newValue).doubleValue(), 0.0);
    }

    @Override
    protected Double getSample() {
        return doubleObj;
    }

    @Override
    protected void assertValueEncodesToJsonAs(final Double a, final String json) {
        assertEquals("32.5", json);
    }

}
