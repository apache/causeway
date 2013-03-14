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

package org.apache.isis.objectstore.nosql;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.objectstore.nosql.keys.KeyCreatorDefault;

public class NoSqlKeyCreatorTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private OidMarshaller mockOidMarshaller;
    @Mock
    private SpecificationLoaderSpi mockSpecificationLoader;
    @Mock
    private ObjectSpecification mockSpecification;

    private final RootOidDefault oid = RootOidDefault.create(ObjectSpecId.of("ERP"), "3");
    private final String oidStr = oid.enString(new OidMarshaller());

    private KeyCreatorDefault keyCreatorDefault;

    
    @Before
    public void setUp() throws Exception {
        keyCreatorDefault = new KeyCreatorDefault() {
            @Override
            protected OidMarshaller getOidMarshaller() {
                return mockOidMarshaller;
            }
            @Override
            protected SpecificationLoaderSpi getSpecificationLoader() {
                return mockSpecificationLoader;
            }
        };
    }

    @Test
    public void unmarshal() throws Exception {
        context.checking(new Expectations() {

            {
                one(mockOidMarshaller).unmarshal(oidStr, RootOid.class);
                will(returnValue(oid));
            }
        });
        assertEquals(oid, keyCreatorDefault.unmarshal(oidStr));
    }

    @Test
    public void specification() throws Exception {
        context.checking(new Expectations() {
            {
                one(mockOidMarshaller).unmarshal(oidStr, TypedOid.class);
                will(returnValue(oid));
                one(mockSpecificationLoader).lookupBySpecId(oid.getObjectSpecId());
                will(returnValue(mockSpecification));
            }
        });
        final ObjectSpecification spec = keyCreatorDefault.specificationFromOidStr(oidStr);
        assertEquals(mockSpecification, spec);
    }
}
