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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.core.metamodel.valuesemantics.StringValueSemantics;

class StringValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<String> {

    private StringValueSemantics value;

    private String string;

    @BeforeEach
    void setUpObjects() throws Exception {
        string = "text entry";
        setSemantics(value = new StringValueSemantics());
    }

    @Test
    void titleOf() {
        assertEquals("text entry", value.titlePresentation(null, string));
    }

    @Test
    void parse() throws Exception {
        final Object parsed = value.parseTextRepresentation(null, "tRUe");
        assertEquals("tRUe", parsed.toString());
    }

    @Override
    protected String getSample() {
        return string;
    }

    @Override
    protected void assertValueEncodesToJsonAs(final String a, final String json) {
        assertEquals("text entry", json);
    }
}
