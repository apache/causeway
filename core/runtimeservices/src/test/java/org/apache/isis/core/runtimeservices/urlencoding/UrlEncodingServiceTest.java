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
package org.apache.isis.core.runtimeservices.urlencoding;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;

import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.applib.services.urlencoding.UrlEncodingServiceUsingBaseEncodingAbstract;

public class UrlEncodingServiceTest {

    UrlEncodingServiceWithCompression serviceWithCompression;
    UrlEncodingServiceUsingBaseEncodingAbstract serviceBaseEncoding;

    @Before
    public void setUp() throws Exception {
        serviceWithCompression = new UrlEncodingServiceWithCompression();
        serviceBaseEncoding = new UrlEncodingServiceUsingBaseEncodingAbstract(){};
    }

    @Test
    public void roundtrip() throws Exception {
        roundtrip(serviceBaseEncoding, false);
    }

    @Test
    public void roundtrip_with_compression() throws Exception {
        roundtrip(serviceWithCompression, true);
    }

    private void roundtrip(UrlEncodingService service, boolean testIsCompressing) throws Exception {

        final String original = "0-theme-entityPageContainer-entity-rows-2-rowContents-1-col-tabGroups-1-panel-tabPanel-rows-1-rowContents-1-col-fieldSets-1-memberGroup-properties-1-property-scalarTypeContainer-scalarIfRegular-associatedActionLinksBelow-additionalLinkList-additionalLinkItem-0-additionalLink";

        final String encoded = service.encodeString(original);
        final String decoded = service.decodeToString(encoded);

        Assert.assertThat(decoded, is(equalTo(original)));

        if(testIsCompressing) {
            Assert.assertThat(original.length(), is(greaterThan(encoded.length())));	
        }

    }


}