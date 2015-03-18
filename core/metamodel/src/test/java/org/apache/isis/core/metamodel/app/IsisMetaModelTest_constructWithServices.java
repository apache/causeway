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

package org.apache.isis.core.metamodel.app;

import java.util.List;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IsisMetaModelTest_constructWithServices {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private RuntimeContext mockContext;

    @Mock
    private ProgrammingModel mockProgrammingModel;

    @Mock
    private SomeRepo mockService1;

    @Mock
    private SomeOtherRepo mockService2;

    private IsisMetaModel metaModel;

    private static class SomeRepo {}
    private static class SomeOtherRepo {}

    @Before
    public void setUp() throws Exception {
        context.ignoring(mockProgrammingModel);
    }

    @Test
    public void shouldSucceedWithoutThrowingAnyExceptions() {
        metaModel = new IsisMetaModel(mockContext, mockProgrammingModel);
    }

    @Test
    public void shouldBeAbleToRegisterServices() {
        metaModel = new IsisMetaModel(mockContext, mockProgrammingModel, mockService1, mockService2);
        final List<Object> services = metaModel.getServices();
        assertThat(services.size(), is(3));
        assertThat(services, IsisMatchers.containsObjectOfType(SomeRepo.class));
        assertThat(services, IsisMatchers.containsObjectOfType(SomeOtherRepo.class));
    }


}
