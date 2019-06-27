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

import org.apache.isis.config.internal._Config;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.metamodel.facets.value.doubles.DoubleWrapperValueSemanticsProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DoubleValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {

    private Double doubleObj;

    private FacetHolder holder;

    @Before
    public void setUpObjects() throws Exception {
        
        _Config.put("isis.value.format.double", null);

        holder = new FacetHolderImpl();
        setValue(new DoubleWrapperValueSemanticsProvider(holder));

        doubleObj = new Double(32.5d);
        allowMockAdapterToReturn(doubleObj);
    }

    @Test
    public void testValue() {
        assertEquals("32.5", getValue().displayTitleOf(doubleObj));
    }

    @Test
    public void testInvalidParse() throws Exception {
        try {
            getValue().parseTextEntry(null, "one");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    @Test
    public void testTitleOf() {
        assertEquals("35,000,000", getValue().displayTitleOf(Double.valueOf(35000000.0)));
    }

    @Test
    public void testParse() throws Exception {
        final Object newValue = getValue().parseTextEntry(null, "120.56");
        assertEquals(120.56, ((Double) newValue).doubleValue(), 0.0);
    }

    @Test
    public void testParse2() throws Exception {
        final Object newValue = getValue().parseTextEntry(null, "1,20.0");
        assertEquals(120, ((Double) newValue).doubleValue(), 0.0);
    }
}
