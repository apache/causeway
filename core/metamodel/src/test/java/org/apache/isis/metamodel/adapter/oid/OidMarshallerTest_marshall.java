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
package org.apache.isis.metamodel.adapter.oid;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.isis.metamodel.adapter.oid.Oid.Factory;
import org.apache.isis.metamodel.spec.ObjectSpecId;

public class OidMarshallerTest_marshall {

    private Oid_Marshaller oidMarshaller;

    @Before
    public void setUp() throws Exception {
        oidMarshaller = Oid_Marshaller.INSTANCE;
    }

    @Test
    public void rootOid() {
        final String marshal = oidMarshaller.marshal(Factory.persistentOf(ObjectSpecId.of("CUS"),  "123"));
        assertThat(marshal, equalTo("CUS:123"));
    }

    @Test
    public void rootOid_transient() {
        final String marshal = oidMarshaller.marshal(Factory.transientOf(ObjectSpecId.of("CUS"),  "123"));
        assertThat(marshal, equalTo("!CUS:123"));
    }

}
