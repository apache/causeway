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

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.causeway.core.metamodel.valuesemantics.BigIntegerValueSemantics;

class BigIntValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<BigInteger> {

    private BigIntegerValueSemantics value;
    private BigInteger bigInt;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        bigInt = new BigInteger("132199");
        allowMockAdapterToReturn(bigInt);
        setSemantics(value = new BigIntegerValueSemantics());
    }

    @Test
    public void testParseValidString() throws Exception {
        final Object newValue = value.parseTextRepresentation(null, "2142342334");
        assertEquals(new BigInteger("2142342334"), newValue);
    }

    @Test
    public void testParseInvalidString() throws Exception {
        try {
            value.parseTextRepresentation(null, "214xxx2342334");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    @Test
    public void testTitle() throws Exception {
        assertEquals("132,199", value.titlePresentation(null, bigInt));
    }

    @Override
    protected BigInteger getSample() {
        return bigInt;
    }

    @Override
    protected void assertValueEncodesToJsonAs(final BigInteger a, final String json) {
        assertEquals("132199", json);
    }

}
