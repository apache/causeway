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

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.config.internal._Config;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueSemanticsProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BigDecimalValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {

    private BigDecimalValueSemanticsProvider value;
    private BigDecimal bigDecimal;
    private FacetHolder holder;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        _Config.put("isis.value.format.decimal", null);

        bigDecimal = new BigDecimal("34132.199");
        allowMockAdapterToReturn(bigDecimal);
        holder = new FacetHolderImpl();

        setValue(value = new BigDecimalValueSemanticsProvider(holder, mockServicesInjector));
    }

    @Test
    public void testParseValidString() throws Exception {
        final Object newValue = value.parseTextEntry(null, "2142342334");
        assertEquals(new BigDecimal(2142342334L), newValue);
    }

    @Test
    public void testParseInvalidString() throws Exception {
        try {
            value.parseTextEntry(null, "214xxx2342334");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    @Test
    public void testTitleOf() {
        assertEquals("34,132.199", value.displayTitleOf(bigDecimal));
    }

    @Test
    public void testEncode() {
        assertEquals("34132.199", value.toEncodedString(bigDecimal));
    }

    @Test
    public void testDecode() throws Exception {
        final Object newValue = value.fromEncodedString("4322.89991");
        assertEquals(new BigDecimal("4322.89991"), newValue);
    }

}
