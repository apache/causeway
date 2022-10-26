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

import org.apache.causeway.applib.exceptions.recoverable.InvalidEntryException;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.core.metamodel.valuesemantics.CharacterValueSemantics;

class CharacterValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<Character> {

    private CharacterValueSemantics valueSemantics;

    private Character character;

    @BeforeEach
    public void setUpObjects() throws Exception {
        character = Character.valueOf('r');
        setSemantics(valueSemantics = new CharacterValueSemantics());
    }

    @Test
    public void testParseLongString() throws Exception {
        try {
            valueSemantics.parseTextRepresentation(null, "one");
            fail();
        } catch (final InvalidEntryException expected) {
        }
    }

    @Test
    public void testTitleOf() {
        assertEquals("r", valueSemantics.titlePresentation(null, character));
    }

    @Test
    public void testValidParse() throws Exception {
        final Object parse = valueSemantics.parseTextRepresentation(null, "t");
        assertEquals(Character.valueOf('t'), parse);
    }

    @Test
    public void testEncode() throws Exception {
        assertEquals("r", valueSemantics.decompose(character).toJson());
    }

    @Test
    public void testDecode() throws Exception {
        final Object restore = valueSemantics.compose(
                ValueDecomposition.fromJson(valueSemantics.getSchemaValueType(), "Y"));
        assertEquals(Character.valueOf('Y'), restore);
    }

    @Override
    protected Character getSample() {
        return character;
    }

    @Override
    protected void assertValueEncodesToJsonAs(final Character a, final String json) {
        assertEquals("r", json);
    }
}
