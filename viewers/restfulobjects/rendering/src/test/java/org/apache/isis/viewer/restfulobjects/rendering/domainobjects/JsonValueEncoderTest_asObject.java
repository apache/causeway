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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.viewer.restfulobjects.rendering.service.valuerender.JsonValueEncoderService;
import org.apache.isis.viewer.restfulobjects.rendering.service.valuerender.JsonValueEncoderServiceDefault;

public class JsonValueEncoderTest_asObject {

    @Rule public JUnitRuleMockery2 context =
            JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock private ObjectSpecification mockObjectSpec;
    @Mock private ManagedObject mockObjectAdapter;
    @Mock private SpecificationLoader specLoader;

    private JsonValueEncoderService jsonValueEncoder;

    @Before
    public void setUp() throws Exception {

        context.checking(new Expectations() {
            {
                allowing(mockObjectAdapter).getSpecification();
                will(returnValue(mockObjectSpec));
            }
        });

        jsonValueEncoder = JsonValueEncoderServiceDefault.forTesting(specLoader);

    }

    @Test(expected = Exception.class)
    public void whenAdapterIsNull() throws Exception {
        jsonValueEncoder.asObject(null, null);
    }

}
