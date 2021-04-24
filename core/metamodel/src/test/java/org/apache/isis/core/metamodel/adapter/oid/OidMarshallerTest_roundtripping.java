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

package org.apache.isis.core.metamodel.adapter.oid;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import lombok.val;

public class OidMarshallerTest_roundtripping {

    @Test
    public void oid() {
        val oid = Oid.of(LogicalTypeTestFactory.cus(), "123");

        final String enString = oid.stringify();
        final Oid deString = Oid.parse(enString);
        assertThat(deString, is(oid));
    }
    
    @Test
    public void oid_withLegacyVersionIgnored() {
        val oid = Oid.of(LogicalTypeTestFactory.cus(), "123");

        final String enString = oid.stringify();
        final Oid deString = Oid.parse(enString + "^" + 90807L);
        assertThat(deString, is(oid));
    }
    
}
