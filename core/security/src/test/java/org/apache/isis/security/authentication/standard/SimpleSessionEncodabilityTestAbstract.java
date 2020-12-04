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

package org.apache.isis.security.authentication.standard;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.core.security.authentication.AuthenticationSessionAbstract;
import org.apache.isis.security.EncodabilityContractTest;

public abstract class SimpleSessionEncodabilityTestAbstract extends EncodabilityContractTest {

    @Override
    protected void assertRoundtripped(final Object decodedEncodable, final Object originalEncodable) {
        final AuthenticationSessionAbstract decoded = (AuthenticationSessionAbstract) decodedEncodable;
        final AuthenticationSessionAbstract original = (AuthenticationSessionAbstract) originalEncodable;

        assertThat(decoded.getUser(), is(equalTo(original.getUser()))); // redundant shortcut
        
        assertThat(decoded.getExecutionContext().getTimeZone(), is(equalTo(original.getExecutionContext().getTimeZone())));
        assertThat(decoded.getExecutionContext().getLocale(), is(equalTo(original.getExecutionContext().getLocale())));
        assertThat(decoded.getExecutionContext().getUser(), is(equalTo(original.getExecutionContext().getUser())));
        assertThat(decoded.getExecutionContext().getClock(), is(equalTo(original.getExecutionContext().getClock())));
        
        assertThat(decoded.getExecutionContext(), is(equalTo(original.getExecutionContext())));
    }

}
