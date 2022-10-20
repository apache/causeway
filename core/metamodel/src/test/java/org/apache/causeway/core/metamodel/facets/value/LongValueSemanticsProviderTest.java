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
import org.apache.causeway.core.metamodel.valuesemantics.LongValueSemantics;

public class LongValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<Long> {

    private LongValueSemantics value;

    private Long longObj;

    @BeforeEach
    public void setUpObjects() throws Exception {
        longObj = Long.valueOf(367322);
        allowMockAdapterToReturn(longObj);

        setSemantics(value = new LongValueSemantics());
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
    public void testOutputAsString() {
        assertEquals("367,322", value.titlePresentation(null, longObj));
    }

    @Test
    public void testParse() throws Exception {
        final Object parsed = value.parseTextRepresentation(null, "120");
        assertEquals("120", parsed.toString());
    }

    @Test
    public void testParseWithBadlyFormattedEntry() throws Exception {
        final Object parsed = value.parseTextRepresentation(null, "1,20.0");
        assertEquals("120", parsed.toString());
    }

    @Override
    protected Long getSample() {
        return longObj;
    }

    @Override
    protected void assertValueEncodesToJsonAs(final Long a, final String json) {
        assertEquals("367322", json);
    }

}
