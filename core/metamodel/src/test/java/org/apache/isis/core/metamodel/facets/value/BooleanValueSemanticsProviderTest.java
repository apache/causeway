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
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;
import org.apache.isis.core.metamodel.facets.value.booleans.BooleanValueSemanticsProviderAbstract;
import org.apache.isis.core.metamodel.facets.value.booleans.BooleanWrapperValueSemanticsProvider;

public class BooleanValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase {

    private BooleanValueSemanticsProviderAbstract value;

    private Boolean booleanObj;
    private FacetHolder facetHolder;

    @Before
    public void setUpObjects() throws Exception {
        booleanObj = Boolean.valueOf(true);
        facetHolder = FacetHolderAbstract.forTesting(metaModelContext);
        setValue(value = new BooleanWrapperValueSemanticsProvider(facetHolder));
    }

    @Test
    public void testParseFalseString() throws Exception {
        final Object parsed = value.parseTextEntry(null, "faLSe");
        assertEquals(Boolean.valueOf(false), parsed);
    }

    @Test
    public void testParseStringWithPrecedingSpace() throws Exception {
        final Object parsed = value.parseTextEntry(null, " false");
        assertEquals(Boolean.valueOf(false), parsed);
    }

    @Test
    public void testParseStringWithTrailingSpace() throws Exception {
        final Object parsed = value.parseTextEntry(null, " false");
        assertEquals(Boolean.valueOf(false), parsed);
    }

    @Test
    public void testParseTrueString() throws Exception {
        final Object parsed = value.parseTextEntry(null, "TRue");
        assertEquals(Boolean.valueOf(true), parsed);
    }

    @Test
    public void testParseInvalidString() throws Exception {
        try {
            value.parseTextEntry(null, "yes");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    @Test
    public void testTitle() throws Exception {
        assertEquals("True", value.displayTitleOf(booleanObj));
    }

    @Test
    public void testTitleWhenNotSet() throws Exception {
        assertEquals("", value.titleString(null));
    }

    @Test
    public void testEncode() throws Exception {
        assertEquals("T", value.toEncodedString(booleanObj));
    }

    @Test
    public void testDecode() throws Exception {
        final Object parsed = value.fromEncodedString("T");
        assertEquals(Boolean.valueOf(true), parsed);
    }

    @Test
    public void testIsSet() {
        allowMockAdapterToReturn(Boolean.valueOf(true));
        assertEquals(true, value.isSet(mockAdapter));
    }

    @Test
    public void testIsNotSet() {
        allowMockAdapterToReturn(Boolean.valueOf(false));
        assertEquals(false, value.isSet(mockAdapter));
    }
}
