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
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;
import org.apache.isis.core.metamodel.facets.value.blobs.BlobValueSemanticsProvider;

public class BlobValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase {

    private BlobValueSemanticsProvider value;
    private Blob blob;
    private FacetHolder holder;

    @Before
    public void setUpObjects() throws Exception {
        blob = new Blob("myfile1.docx", "application", "vnd.ms-word", new byte[]{1,2,3,4});
        allowMockAdapterToReturn(blob);
        holder = FacetHolderAbstract.forTesting(metaModelContext);

        setValue(value = new BlobValueSemanticsProvider(holder));
    }

    @Test
    public void testTitleOf() {
        assertEquals("myfile1.docx", value.displayTitleOf(blob));
    }

    @Test
    public void testEncode_and_decode() {
        String encoded = value.toEncodedString(blob);
        assertEquals("myfile1.docx:application/vnd.ms-word:AQIDBA==", encoded);
        Blob decoded = value.fromEncodedString(encoded);
        assertThat(decoded.getName(), is("myfile1.docx"));
        assertThat(decoded.getMimeType().getPrimaryType(), is("application"));
        assertThat(decoded.getMimeType().getSubType(), is("vnd.ms-word"));
        assertThat(decoded.getBytes().length, is(4));
    }

}
