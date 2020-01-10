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

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facets.value.string.StringValueSemanticsProvider;

public class StringValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {

    private StringValueSemanticsProvider value;

    private String string;

    private FacetHolder holder;

    @Before
    public void setUpObjects() throws Exception {
        string = "text entry";
        holder = new FacetHolderImpl();
        setValue(value = new StringValueSemanticsProvider(holder));
    }

    @Test
    public void testTitleOf() {
        assertEquals("text entry", value.displayTitleOf(string));
    }

    @Test
    public void testParse() throws Exception {
        final Object parsed = value.parseTextEntry(null, "tRUe");
        assertEquals("tRUe", parsed.toString());
    }

    @Test
    public void testEncodeNormalString() throws Exception {
        allowMockAdapterToReturn("/slash");
        assertEquals("//slash", getEncodeableFacet().toEncodedString(mockAdapter));
    }

    @Test
    public void testEncodeNULLString() throws Exception {
        allowMockAdapterToReturn("NULL");
        assertEquals("/NULL", getEncodeableFacet().toEncodedString(mockAdapter));
    }

    @Test
    public void testRestore() throws Exception {
        final Object parsed = value.fromEncodedString("//slash");
        assertEquals("/slash", parsed.toString());
    }

    @Test
    public void testRestoreNULLString() throws Exception {
        final Object parsed = value.fromEncodedString("/NULL");
        assertEquals("NULL", parsed.toString());
    }
}
