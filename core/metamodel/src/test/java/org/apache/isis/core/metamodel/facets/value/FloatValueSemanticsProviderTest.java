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
package org.apache.isis.core.metamodel.facets.value;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.core.metamodel.valuesemantics.FloatValueSemantics;

public class FloatValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase {

    private FloatValueSemantics value;
    private Float float1;

    @Before
    public void setUpObjects() throws Exception {

        float1 = Float.valueOf(32.5f);
        allowMockAdapterToReturn(float1);

        setSemantics(value = new FloatValueSemantics());
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
        assertEquals("32.5", value.simpleTextRepresentation(null, float1));
    }

    @Test
    public void testParse() throws Exception {
        final Object parsed = value.parseTextRepresentation(null, "120.50");
        assertEquals(120.5f, ((Float) parsed).floatValue(), 0.0);
    }

    @Test
    public void testParseBadlyFormatedEntry() throws Exception {
        final Object parsed = value.parseTextRepresentation(null, "1,20.0");
        assertEquals(120.0f, ((Float) parsed).floatValue(), 0.0);
    }

    @Test
    public void testEncode() throws Exception {
        assertEquals("32.5", getEncodeableFacet().toEncodedString(createAdapter(float1)));
    }

    @Test
    public void testDecode() throws Exception {
        final Object restored = value.fromEncodedString("10.25");
        assertEquals(10.25, ((Float) restored).floatValue(), 0.0);
    }

}
