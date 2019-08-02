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

package org.apache.isis.metamodel.facets.value;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.metamodel.facets.object.parseable.InvalidEntryException;
import org.apache.isis.metamodel.facets.value.chars.CharValueSemanticsProviderAbstract;
import org.apache.isis.metamodel.facets.value.chars.CharWrapperValueSemanticsProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CharacterValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {

    private CharValueSemanticsProviderAbstract value;

    private Character character;

    private FacetHolder holder;

    @Before
    public void setUpObjects() throws Exception {
        character = Character.valueOf('r');
        holder = new FacetHolderImpl();
        setValue(value = new CharWrapperValueSemanticsProvider(holder));
    }

    @Test
    public void testParseLongString() throws Exception {
        try {
            value.parseTextEntry(null, "one");
            fail();
        } catch (final InvalidEntryException expected) {
        }
    }

    @Test
    public void testTitleOf() {
        assertEquals("r", value.displayTitleOf(character));
    }

    @Test
    public void testValidParse() throws Exception {
        final Object parse = value.parseTextEntry(null, "t");
        assertEquals(Character.valueOf('t'), parse);
    }

    @Test
    public void testEncode() throws Exception {
        assertEquals("r", value.toEncodedString(character));
    }

    @Test
    public void testDecode() throws Exception {
        final Object restore = value.fromEncodedString("Y");
        assertEquals(Character.valueOf('Y'), restore);
    }
}
