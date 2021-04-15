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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class OidMarshallerTest_marshall {

    private _OidMarshaller oidMarshaller;

    @Before
    public void setUp() throws Exception {
        oidMarshaller = _OidMarshaller.INSTANCE;
    }

    @Test
    public void oid() {
        final String marshal = oidMarshaller.marshal(Oid.root(LogicalTypeTestFactory.cus(),  "123"));
        assertThat(marshal, equalTo("CUS:123"));
    }

//deprecated    
//    @Test
//    public void oid_transient() {
//        final String marshal = oidMarshaller.marshal(Oid.Factory.transientOf(ObjectSpecId.of("CUS"),  "123"));
//        assertThat(marshal, equalTo("!CUS:123"));
//    }

}
