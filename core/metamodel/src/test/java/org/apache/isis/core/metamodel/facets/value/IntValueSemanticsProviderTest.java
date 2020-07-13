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

import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.facets.value.integer.IntValueSemanticsProviderAbstract;
import org.apache.isis.core.metamodel.facets.value.integer.IntWrapperValueSemanticsProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IntValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {

    private IntValueSemanticsProviderAbstract value;
    private Integer integer;
    private FacetHolder holder;

    @Before
    public void setUpObjects() throws Exception {
        integer = Integer.valueOf(32);
        allowMockAdapterToReturn(integer);

        holder = new FacetHolderImpl();
        ((MetaModelContextAware)holder).setMetaModelContext(super.metaModelContext);

        setValue(value = new IntWrapperValueSemanticsProvider(holder));
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
    public void testTitleString() {
        assertEquals("32", value.displayTitleOf(integer));
    }

    @Test
    public void testParse() throws Exception {
        final Object newValue = value.parseTextEntry(null, "120");
        assertEquals(Integer.valueOf(120), newValue);
    }

    @Test
    public void testParseOddlyFormedEntry() throws Exception {
        final Object newValue = value.parseTextEntry(null, "1,20.0");
        assertEquals(Integer.valueOf(120), newValue);
    }
}
