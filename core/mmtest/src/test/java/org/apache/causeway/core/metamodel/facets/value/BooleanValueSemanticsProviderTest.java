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
import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.core.metamodel.valuesemantics.BooleanValueSemantics;

class BooleanValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<Boolean> {

    private BooleanValueSemantics value;

    private Boolean booleanObj;

    @BeforeEach
    void setUpObjects() throws Exception {
        booleanObj = Boolean.valueOf(true);
        setSemantics(value = new BooleanValueSemantics());
    }

    @Test
    void parseFalseString() throws Exception {
        final Object parsed = value.parseTextRepresentation(null, "faLSe");
        assertEquals(Boolean.valueOf(false), parsed);
    }

    @Test
    void parseStringWithPrecedingSpace() throws Exception {
        final Object parsed = value.parseTextRepresentation(null, " false");
        assertEquals(Boolean.valueOf(false), parsed);
    }

    @Test
    void parseStringWithTrailingSpace() throws Exception {
        final Object parsed = value.parseTextRepresentation(null, " false");
        assertEquals(Boolean.valueOf(false), parsed);
    }

    @Test
    void parseTrueString() throws Exception {
        final Object parsed = value.parseTextRepresentation(null, "TRue");
        assertEquals(Boolean.valueOf(true), parsed);
    }

    @Test
    void parseInvalidString() throws Exception {
        try {
            value.parseTextRepresentation(null, "yes");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    @Test
    void title() throws Exception {
        assertEquals("True", value.titlePresentation(null, booleanObj));
    }

    @Test
    void titleWhenNotSet() throws Exception {
        assertEquals(PlaceholderLiteral.NULL_REPRESENTATION.getLiteral(),
                value.titlePresentation(null, null));
    }

    @Override
    protected Boolean getSample() {
        return booleanObj;
    }

    @Override
    protected void assertValueEncodesToJsonAs(final Boolean a, final String json) {
        assertEquals("true", json);
    }

}
