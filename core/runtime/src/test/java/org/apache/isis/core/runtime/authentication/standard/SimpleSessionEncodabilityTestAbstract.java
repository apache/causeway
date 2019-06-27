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

package org.apache.isis.core.runtime.authentication.standard;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.commons.encoding.EncodabilityContractTest;
import org.apache.isis.security.authentication.standard.SimpleSession;

public abstract class SimpleSessionEncodabilityTestAbstract extends EncodabilityContractTest {

    @Override
    protected void assertRoundtripped(final Object decodedEncodable, final Object originalEncodable) {
        final SimpleSession decoded = (SimpleSession) decodedEncodable;
        final SimpleSession original = (SimpleSession) originalEncodable;

        assertThat(decoded.getUserName(), is(equalTo(original.getUserName())));
        assertThat(decoded.getRoles(), is(equalTo(original.getRoles())));
    }

}
