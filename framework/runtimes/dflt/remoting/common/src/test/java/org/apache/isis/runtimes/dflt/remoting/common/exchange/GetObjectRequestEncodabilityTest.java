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

package org.apache.isis.runtimes.dflt.remoting.common.exchange;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.isis.core.commons.encoding.EncodabilityContractTest;
import org.apache.isis.core.commons.encoding.Encodable;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class GetObjectRequestEncodabilityTest extends EncodabilityContractTest {

    private Oid mockOid;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockOid = context.mock(Oid.class);
    }

    @Override
    protected Encodable createEncodable() {
        return new GetObjectRequest(mockAuthSession, mockOid, "com.mycompany.Customer");
    }

    @Override
    @Ignore
    @Test
    public void shouldRoundTrip() throws IOException {
        super.shouldRoundTrip();
    }

    @Override
    protected void assertRoundtripped(final Object decodedEncodable, final Object originalEncodable) {
        final GetObjectRequest decoded = (GetObjectRequest) decodedEncodable;
        final GetObjectRequest original = (GetObjectRequest) originalEncodable;

        // TODO: to complete, may need to setup mock expectations
        assertThat(decoded.getId(), is(equalTo(original.getId())));
    }

}
