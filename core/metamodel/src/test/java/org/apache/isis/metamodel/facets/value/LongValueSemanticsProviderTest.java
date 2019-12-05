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

import org.apache.isis.metamodel.context.MetaModelContextAware;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.metamodel.facets.value.longs.LongValueSemanticsProviderAbstract;
import org.apache.isis.metamodel.facets.value.longs.LongWrapperValueSemanticsProvider;
import org.apache.isis.unittestsupport.config.internal._Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LongValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {

    private LongValueSemanticsProviderAbstract value;

    private Object longObj;
    private FacetHolder holder;

    @Before
    public void setUpObjects() throws Exception {
        longObj = Long.valueOf(367322);
        allowMockAdapterToReturn(longObj);
        holder = new FacetHolderImpl();
        ((MetaModelContextAware)holder).setMetaModelContext(super.metaModelContext);

        _Config.put("isis.value.format.long", null);

        setValue(value = new LongWrapperValueSemanticsProvider(holder));
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
    public void testOutputAsString() {
        assertEquals("367,322", value.displayTitleOf(longObj));
    }

    @Test
    public void testParse() throws Exception {
        final Object parsed = value.parseTextEntry(null, "120");
        assertEquals("120", parsed.toString());
    }

    @Test
    public void testParseWithBadlyFormattedEntry() throws Exception {
        final Object parsed = value.parseTextEntry(null, "1,20.0");
        assertEquals("120", parsed.toString());
    }

    @Test
    public void testEncode() throws Exception {
        assertEquals("367322", value.toEncodedString(longObj));
    }

    @Test
    public void test() throws Exception {
        final Object parsed = value.fromEncodedString("234");
        assertEquals("234", parsed.toString());
    }
}
