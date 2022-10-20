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
package org.apache.causeway.applib.jaxbadapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.val;

public abstract class JaxbXmlAdaptersContractTest<T> {

    public static final String sampleComplexString = "Hello World!?{[()]}§$%&=´`*+~#',;.:-_|@€µ<>^°\"";
    public static final byte[] sampleAllTheBytes = new byte[256];
    {
        for(int i=0; i<=255; ++i) {
            sampleAllTheBytes[i]=(byte)i;
        }
    }

    private final T[] sampleValues;
    private final XmlAdapter<String, T> xmlAdapter;

    @SafeVarargs
    protected JaxbXmlAdaptersContractTest(XmlAdapter<String, T> xmlAdapter, T... sampleValues) {
        this.sampleValues = sampleValues;
        this.xmlAdapter = xmlAdapter;
    }

    @Test
    final void can_roundtrip() throws Exception {

        XmlAdapter<String, T> xmlAdapter = this.xmlAdapter;
        for(T sampleValue : this.sampleValues) {

            val xml = xmlAdapter.marshal(sampleValue);
            val recovered = xmlAdapter.unmarshal(xml);

            if(sampleValue instanceof byte[]) {
                Assertions.assertArrayEquals((byte[])sampleValue, (byte[])recovered);
            } else {
                Assertions.assertEquals(sampleValue, recovered);
            }
        }

        val nullXml = xmlAdapter.marshal(null);
        val nullRecovered = xmlAdapter.unmarshal(nullXml);

        Assertions.assertNull(nullRecovered);
    }
}
