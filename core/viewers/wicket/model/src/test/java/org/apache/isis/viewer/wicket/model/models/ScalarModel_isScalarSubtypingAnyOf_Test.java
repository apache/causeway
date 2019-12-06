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
package org.apache.isis.viewer.wicket.model.models;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.metamodel.MetaModelContext_forTesting;
import org.apache.isis.metamodel.context.MetaModelContext;
import org.apache.isis.metamodel.objectmanager.ObjectManager;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.webapp.context.memento.ObjectMementoService;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import lombok.val;

public class ScalarModel_isScalarSubtypingAnyOf_Test {

    @Rule public JUnitRuleMockery2 context = 
            JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock ObjectSpecification mockObjectSpecification;
    @Mock EntityModel mockEntityModel;
    @Mock ObjectMementoService mockObjectAdapterMementoSupport;
    @Mock ObjectManager mockObjectManager; 
    
    MetaModelContext metaModelContext;

    public static class A {}
    public static class B extends A {}
    public static class C extends B {}

    @Before
    public void setup() {
        
        metaModelContext = MetaModelContext_forTesting.builder()
                .objectManager(mockObjectManager)
                .singleton(mockObjectAdapterMementoSupport)
                .build();
        
        val commonContext = IsisWebAppCommonContext.of(metaModelContext);
        
        context.checking(new Expectations() {{
            
            allowing(mockEntityModel).getCommonContext();
            will(returnValue(commonContext));


        }});
        
    }
    
    @Test
    public void when_super() {
        assertThat(newScalarModelFor(A.class).isScalarTypeSubtypeOf(B.class), is(equalTo(false)));
    }

    @Test
    public void when_same() {
        assertThat(newScalarModelFor(B.class).isScalarTypeSubtypeOf(B.class), is(equalTo(true)));
    }

    @Test
    public void when_sub() {
        assertThat(newScalarModelFor(C.class).isScalarTypeSubtypeOf(B.class), is(equalTo(true)));
    }

    private ScalarModel newScalarModelFor(final Class<?> result) {
        val scalarModel = new ScalarModel(mockEntityModel, null) {
            private static final long serialVersionUID = 1L;

            @Override public ObjectSpecification getTypeOfSpecification() {
                return mockObjectSpecification;
            }
        };
        context.checking(new Expectations() {{
            allowing(mockObjectSpecification).getCorrespondingClass();
            will(returnValue(result));
        }});
        return scalarModel;
    }
}