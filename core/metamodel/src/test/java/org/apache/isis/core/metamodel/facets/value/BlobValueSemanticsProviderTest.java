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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.core.metamodel.facets.object.value.ValueSerializer.Format;
import org.apache.isis.core.metamodel.valuesemantics.BlobValueSemantics;

public class BlobValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<Blob> {

    private Blob blob;

    @Before
    public void setUpObjects() throws Exception {
        super.setUp();

        blob = new Blob("myfile1.docx", "application", "vnd.ms-word", new byte[]{1,2,3,4});
        allowMockAdapterToReturn(blob);
        setSemantics(new BlobValueSemantics());
    }

    @Test
    public void testTitleOf() {
        assertEquals("myfile1.docx", getRenderer().simpleTextPresentation(null, blob));
    }

    @Test
    public void testEncode_and_decode() {
        String encoded = getValueSerializer().toEncodedString(Format.JSON, blob);
        assertEquals(
                "{\"name\":\"myfile1.docx\",\"mimeType\":\"application/vnd.ms-word\",\"bytes\":\"AQIDBA==\"}",
                encoded);
        Blob decoded = getValueSerializer().fromEncodedString(Format.JSON, encoded);
        assertThat(decoded.getName(), is("myfile1.docx"));
        assertThat(decoded.getMimeType().getPrimaryType(), is("application"));
        assertThat(decoded.getMimeType().getSubType(), is("vnd.ms-word"));
        assertThat(decoded.getBytes().length, is(4));
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

}
