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
package org.apache.isis.jdo.metamodel.facets.object.version;

import javax.jdo.annotations.Version;

import org.jmock.Expectations;
import org.jmock.Sequence;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.facets.all.deficiencies.DeficiencyFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import lombok.val;

public class JdoVersionAnnotationFacetFactoryTest_refineMetaModel {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private ObjectSpecification mockChildType;
    private ObjectSpecification mockParentType;
    private ObjectSpecification mockGrandParentType;

    private Visitor newValidatorVisitor;
    private MetaModelValidator validator;

    private Sequence sequence;

    @Before
    public void setUp() throws Exception {
        mockChildType = context.mock(ObjectSpecification.class, "mockChildtype");
        mockParentType = context.mock(ObjectSpecification.class, "mockParenttype");
        mockGrandParentType = context.mock(ObjectSpecification.class, "mockGrandParenttype");

        val mockMetaModelContext = context.mock(MetaModelContext.class, "mockMetaModelContext");
        
        sequence = context.sequence("inorder");

        validator = new MetaModelValidatorAbstract() {
            {
                setMetaModelContext(mockMetaModelContext);
            }
        };
        
        newValidatorVisitor = new JdoVersionAnnotationFacetFactory().newValidatorVisitor();
                
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

        newValidatorVisitor.visit(mockChildType, validator);
        
        val failures = new ValidationFailures();
        ((MetaModelValidatorAbstract)validator).collectFailuresInto(failures);
        
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

        newValidatorVisitor.visit(mockChildType, validator);

        val failures = new ValidationFailures();
        ((MetaModelValidatorAbstract)validator).collectFailuresInto(failures);
        
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

        newValidatorVisitor.visit(mockChildType, validator);

        val failures = new ValidationFailures();
        ((MetaModelValidatorAbstract)validator).collectFailuresInto(failures);
        
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
                oneOf(mockChildType).getCorrespondingClass();
                inSequence(sequence);
                will(returnValue(Child.class));

                oneOf(mockChildType).superclass();
                inSequence(sequence);
                will(returnValue(mockParentType));

                oneOf(mockParentType).getCorrespondingClass();
                inSequence(sequence);
                will(returnValue(Parent.class));
                
                oneOf(mockChildType).getIdentifier();
                inSequence(sequence);
                will(returnValue(Identifier.classIdentifier("mocked")));

                oneOf(mockChildType).getFullIdentifier();
                inSequence(sequence);
                will(returnValue("mockChildType"));

                oneOf(mockParentType).getFullIdentifier();
                inSequence(sequence);
                will(returnValue("mockParentType"));
                
                oneOf(mockChildType).getFacet(DeficiencyFacet.class);
                will(returnValue(null));


            }
        });

        newValidatorVisitor.visit(mockChildType, validator);

        val failures = new ValidationFailures();
        ((MetaModelValidatorAbstract)validator).collectFailuresInto(failures);
        
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
                will(returnValue(mockGrandParentType));

                oneOf(mockGrandParentType).getCorrespondingClass();
                inSequence(sequence);
                will(returnValue(GrandParent.class));
                
                oneOf(mockChildType).getIdentifier();
                inSequence(sequence);
                will(returnValue(Identifier.classIdentifier("mocked")));

                oneOf(mockChildType).getFullIdentifier();
                inSequence(sequence);
                will(returnValue("mockChildType"));

                oneOf(mockGrandParentType).getFullIdentifier();
                inSequence(sequence);
                will(returnValue("mockGrandParentType"));
                
                oneOf(mockChildType).getFacet(DeficiencyFacet.class);
                will(returnValue(null));

            }
        });

        newValidatorVisitor.visit(mockChildType, validator);

        val failures = new ValidationFailures();
        ((MetaModelValidatorAbstract)validator).collectFailuresInto(failures);
        
        assertThat(failures.getNumberOfFailures(), is(1));
        assertThat(failures.getMessages().iterator().next(), is("mockChildType: cannot have @Version annotated on this subclass and any of its supertypes; superclass: mockGrandParentType"));
    }

}
