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

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.core.metamodel.valuesemantics.BlobValueSemantics;

class BlobValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<Blob> {

    private Blob blob;

    @BeforeEach
    public void setUpObjects() throws Exception {
        super.setUp();

        blob = new Blob("myfile1.docx", "application", "vnd.ms-word", new byte[]{1,2,3,4});
        allowMockAdapterToReturn(blob);
        setSemantics(new BlobValueSemantics());
    }

    @Test
    public void testTitleOf() {
        assertEquals("myfile1.docx", getRenderer().titlePresentation(null, blob));
    }

    @Test
    @Override
    public void testParseNull() throws Exception {
        // disabled, blob has no parser
    }

    @Test
    @Override
    public void testParseEmptyString() throws Exception {
        // disabled, blob has no parser
    }

    @Override
    protected Blob getSample() {
        return blob;
    }

    @Override
    protected void assertValueEncodesToJsonAs(final Blob a, final String json) {
        assertEquals(
                "{\"name\":\"myfile1.docx\",\"mimeType\":\"application/vnd.ms-word\",\"bytes\":\"AQIDBA==\"}",
                json);
    }

}
