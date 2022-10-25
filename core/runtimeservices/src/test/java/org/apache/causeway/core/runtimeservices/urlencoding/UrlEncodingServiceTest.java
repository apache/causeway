/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.causeway.core.runtimeservices.urlencoding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.services.urlencoding.UrlEncodingService;

class UrlEncodingServiceTest {

    UrlEncodingServiceWithCompression serviceWithCompression;
    UrlEncodingService serviceBaseEncoding;

    @BeforeEach
    public void setUp() throws Exception {
        serviceWithCompression = new UrlEncodingServiceWithCompression();
        serviceBaseEncoding = UrlEncodingService.forTestingNoCompression();
    }

    @Test
    public void roundtrip() throws Exception {
        roundtrip(serviceBaseEncoding, false);
    }

    @Test
    public void roundtrip_with_compression() throws Exception {
        roundtrip(serviceWithCompression, true);
    }

    private void roundtrip(final UrlEncodingService service, final boolean testIsCompressing) throws Exception {

        final String original = "0-theme-entityPageContainer-entity-rows-2-rowContents-1-col-tabGroups-1-panel-tabPanel-rows-1-rowContents-1-col-fieldSets-1-memberGroup-properties-1-property-scalarTypeContainer-scalarIfRegular-associatedActionLinksBelow-additionalLinkList-additionalLinkItem-0-additionalLink";

        final String encoded = service.encodeString(original);
        final String decoded = service.decodeToString(encoded);

        assertEquals(original, decoded);

        if(testIsCompressing) {
            assertTrue(original.length() > encoded.length());
        }

    }


}