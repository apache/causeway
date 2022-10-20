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
import org.apache.causeway.core.metamodel.valuesemantics.ShortValueSemantics;

class ShortValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<Short> {

    private ShortValueSemantics value;
    private Short short1;

    @BeforeEach
    public void setUpObjects() throws Exception {

        short1 = Short.valueOf((short) 32);
        allowMockAdapterToReturn(short1);

        setSemantics(value = new ShortValueSemantics());
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
    public void testTitleOfForPositiveValue() {
        assertEquals("32", value.titlePresentation(null, short1));
    }

    @Test
    public void testTitleOfForLargestNegativeValue() {
        assertEquals("-128", value.titlePresentation(null, Short.valueOf((short) -128)));
    }

    @Test
    public void testParse() throws Exception {
        final Object newValue = value.parseTextRepresentation(null, "120");
        assertEquals(Short.valueOf((short) 120), newValue);
    }

    @Test
    public void testParseOfOddEntry() throws Exception {
        final Object newValue = value.parseTextRepresentation(null, "1,20.0");
        assertEquals(Short.valueOf((short) 120), newValue);
    }

    @Override
    protected Short getSample() {
        return short1;
    }

    @Override
    protected void assertValueEncodesToJsonAs(final Short a, final String json) {
        assertEquals("32", json);
    }

}
