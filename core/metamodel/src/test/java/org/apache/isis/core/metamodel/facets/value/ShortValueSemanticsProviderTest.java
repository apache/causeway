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
import org.apache.isis.core.metamodel.facets.value.shortint.ShortValueSemanticsProviderAbstract;
import org.apache.isis.core.metamodel.facets.value.shortint.ShortWrapperValueSemanticsProvider;

public class ShortValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase {

    private ShortValueSemanticsProviderAbstract value;
    private Short short1;
    private FacetHolder holder;

    @Before
    public void setUpObjects() throws Exception {

        short1 = Short.valueOf((short) 32);
        allowMockAdapterToReturn(short1);

        holder = FacetHolderAbstract.forTesting(metaModelContext);

        setValue(value = new ShortWrapperValueSemanticsProvider(holder));
    }

    @Test
    public void testInvalidParse() throws Exception {
        try {
            value.parseTextEntry(null, "one");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    @Test
    public void testTitleOfForPositiveValue() {
        assertEquals("32", value.displayTitleOf(short1));
    }

    @Test
    public void testTitleOfForLargestNegativeValue() {
        assertEquals("-128", value.displayTitleOf(Short.valueOf((short) -128)));
    }

    @Test
    public void testParse() throws Exception {
        final Object newValue = value.parseTextEntry(null, "120");
        assertEquals(Short.valueOf((short) 120), newValue);
    }

    @Test
    public void testParseOfOddEntry() throws Exception {
        final Object newValue = value.parseTextEntry(null, "1,20.0");
        assertEquals(Short.valueOf((short) 120), newValue);
    }

}
