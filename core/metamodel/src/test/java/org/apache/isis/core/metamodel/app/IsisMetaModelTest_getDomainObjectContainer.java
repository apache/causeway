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

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.unittestsupport.jmocking.IsisActions;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class IsisMetaModelTest_getDomainObjectContainer {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private RuntimeContext mockContext;

    @Mock
    private ProgrammingModel mockProgrammingModel;
    
    private IsisMetaModel metaModel;


    @Before
    public void setUp() {
        metaModel = new IsisMetaModel(mockContext, mockProgrammingModel);
        expectingMetaModelToBeInitialized();
        metaModel.init();
    }
    
    private void expectingMetaModelToBeInitialized() {
        context.checking(new Expectations() {
            {
                allowing(mockContext).injectInto(with(any(Object.class)));
                will(IsisActions.injectInto());
                
                ignoring(mockContext);
                
                ignoring(mockProgrammingModel);
            }
        });
    }

}
