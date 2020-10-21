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

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.facets.value.biginteger.BigIntegerValueSemanticsProvider;

public class BigIntValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {

    private BigInteger bigInt;
    private FacetHolder holder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        bigInt = new BigInteger("132199");
        allowMockAdapterToReturn(bigInt);

        holder = new FacetHolderImpl();
        ((MetaModelContextAware)holder).setMetaModelContext(super.metaModelContext);
        
        setValue(new BigIntegerValueSemanticsProvider(holder));
    }

    @Test
    public void testParseValidString() throws Exception {
        final Object newValue = getValue().parseTextEntry(null, "2142342334");
        assertEquals(new BigInteger("2142342334"), newValue);
    }

    @Test
    public void testParseInvalidString() throws Exception {
        try {
            getValue().parseTextEntry(null, "214xxx2342334");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    @Test
    public void testTitle() throws Exception {
        assertEquals("132,199", getValue().displayTitleOf(bigInt));
    }

    @Test
    public void testEncode() throws Exception {
        assertEquals("132199", getValue().toEncodedString(bigInt));
    }

    @Test
    public void testDecode() throws Exception {
        final Object newValue = getValue().fromEncodedString("432289991");
        assertEquals(new BigInteger("432289991"), newValue);
    }
    
    // -- HELPER
    
    private ValueSemanticsProviderAndFacetAbstract<BigInteger> getValue() {
        return super.getValue(BigInteger.class);
    }

}
