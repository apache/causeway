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
package org.apache.isis.persistence.jdo.metamodel.facets.object.version;

import javax.jdo.annotations.Version;

import org.jmock.Expectations;
import org.jmock.Sequence;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.persistence.jdo.metamodel.testing.AbstractFacetFactoryTest;

import lombok.val;

public class JdoVersionAnnotationFacetFactoryTest_refineMetaModel {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private ObjectSpecification mockChildType;
    private ObjectSpecification mockParentType;
    private ObjectSpecification mockGrandParentType;
    private ServiceInjector mockServicesInjector;

    private Visitor newValidatorVisitor;
    private MetaModelContext metaModelContext;

    private Sequence sequence;

    @Before
    public void setUp() throws Exception {
        mockChildType = context.mock(ObjectSpecification.class, "mockChildtype");
        mockParentType = context.mock(ObjectSpecification.class, "mockParenttype");
        mockGrandParentType = context.mock(ObjectSpecification.class, "mockGrandParenttype");

        val configuration = new IsisConfiguration(null);

        val programmingModel = new ProgrammingModelAbstract(mockServicesInjector) {};
        programmingModel.init(new ProgrammingModelInitFilterDefault(), metaModelContext);
        
        metaModelContext = MetaModelContext_forTesting.builder()
                .configuration(configuration)
                .programmingModel(programmingModel)
                .build();

        sequence = context.sequence("inorder");
        
        val facetFactory = new JdoVersionAnnotationFacetFactory();
        facetFactory.setJdoFacetContext(AbstractFacetFactoryTest.jdoFacetContextForTesting());
        
        newValidatorVisitor = facetFactory.newValidatorVisitor();
    }

    @Test
    public void whenNoFacet() {

        class Child {}

        context.checking(new Expectations() {
            {
                oneOf(mockChildType).getCorrespondingClass();
                will(returnValue(Child.class));
            }
        });

        newValidatorVisitor.visit(mockChildType);
        
        val failures = metaModelContext.getSpecificationLoader().getValidationResult();
        assertThat(failures.getNumberOfFailures(), is(0));
    }

    @Test
    public void whenHasFacetNoSuperType() {

        @Version
        class Child {}

        context.checking(new Expectations() {
            {
                oneOf(mockChildType).getCorrespondingClass();
                inSequence(sequence);
                will(returnValue(Child.class));

                oneOf(mockChildType).superclass();
                inSequence(sequence);
                will(returnValue(null));
            }
        });

        newValidatorVisitor.visit(mockChildType);

        val failures = metaModelContext.getSpecificationLoader().getValidationResult();
        assertThat(failures.getNumberOfFailures(), is(0));
    }

    @Test
    public void whenHasFacetWithSuperTypeHasNoFacet() {

        class Parent {}

        @Version
        class Child extends Parent {}

        context.checking(new Expectations() {
            {
                oneOf(mockChildType).getCorrespondingClass();
                inSequence(sequence);
                will(returnValue(Child.class));

                oneOf(mockChildType).superclass();
                inSequence(sequence);
                will(returnValue(mockParentType));

                oneOf(mockParentType).getCorrespondingClass();
                inSequence(sequence);
                will(returnValue(Parent.class));

                oneOf(mockParentType).superclass();
                inSequence(sequence);
                will(returnValue(null));
            }
        });

        newValidatorVisitor.visit(mockChildType);

        val failures = metaModelContext.getSpecificationLoader().getValidationResult();
        assertThat(failures.getNumberOfFailures(), is(0));
    }


    @Test
    public void whenHasFacetWithParentTypeHasFacet() {

        @Version
        class Parent {}

        @Version
        class Child extends Parent {}

        context.checking(new Expectations() {
            {
                allowing(mockChildType).getCorrespondingClass();
                inSequence(sequence);
                will(returnValue(Child.class));

                allowing(mockChildType).superclass();
                inSequence(sequence);
                will(returnValue(mockParentType));

                allowing(mockParentType).getCorrespondingClass();
                inSequence(sequence);
                will(returnValue(Parent.class));
                
                allowing(mockChildType).getIdentifier();
                inSequence(sequence);
                will(returnValue(Identifier.classIdentifier(TypeIdentifierTestFactory.customer())));

                allowing(mockChildType).getFullIdentifier();
                inSequence(sequence);
                will(returnValue("mockChildType"));

                allowing(mockParentType).getFullIdentifier();
                inSequence(sequence);
                will(returnValue("mockParentType"));
                
                allowing(mockParentType).getSpecificationLoader();
                inSequence(sequence);
                will(returnValue(metaModelContext.getSpecificationLoader()));
                
                allowing(mockChildType).getSpecificationLoader();
                inSequence(sequence);
                will(returnValue(metaModelContext.getSpecificationLoader()));
                
            }
        });

        newValidatorVisitor.visit(mockChildType);

        val failures = metaModelContext.getSpecificationLoader().getValidationResult();
        
        assertThat(failures.getNumberOfFailures(), is(1));
        assertThat(failures.getMessages().iterator().next(), is("mockChildType: cannot have @Version annotated on this subclass and any of its supertypes; superclass: mockParentType"));
    }


    @Test
    public void whenHasFacetWithGrandParentTypeHasFacet() {

        @Version
        class GrandParent {}

        class Parent extends GrandParent {}

        @Version
        class Child extends Parent {}


        context.checking(new Expectations() {
            {
                allowing(mockChildType).getCorrespondingClass();
                inSequence(sequence);
                will(returnValue(Child.class));

                allowing(mockChildType).superclass();
                inSequence(sequence);
                will(returnValue(mockParentType));

                allowing(mockParentType).getCorrespondingClass();
                inSequence(sequence);
                will(returnValue(Parent.class));

                allowing(mockParentType).superclass();
                inSequence(sequence);
                will(returnValue(mockGrandParentType));

                allowing(mockGrandParentType).getCorrespondingClass();
                inSequence(sequence);
                will(returnValue(GrandParent.class));
                
                allowing(mockChildType).getIdentifier();
                inSequence(sequence);
                will(returnValue(Identifier.classIdentifier(TypeIdentifierTestFactory.customer())));

                allowing(mockChildType).getFullIdentifier();
                inSequence(sequence);
                will(returnValue("mockChildType"));

                allowing(mockGrandParentType).getFullIdentifier();
                inSequence(sequence);
                will(returnValue("mockGrandParentType"));
                
                allowing(mockParentType).getSpecificationLoader();
                inSequence(sequence);
                will(returnValue(metaModelContext.getSpecificationLoader()));
                
                allowing(mockChildType).getSpecificationLoader();
                inSequence(sequence);
                will(returnValue(metaModelContext.getSpecificationLoader()));
                
            }
        });

        newValidatorVisitor.visit(mockChildType);

        val failures = metaModelContext.getSpecificationLoader().getValidationResult();
        
        assertThat(failures.getNumberOfFailures(), is(1));
        assertThat(failures.getMessages().iterator().next(), is("mockChildType: cannot have @Version annotated on this subclass and any of its supertypes; superclass: mockGrandParentType"));
    }

}
